package dev.arandu.piratesmca;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Native SavedData envelope; the domain stays independent of Minecraft for its regression suite.
 * Atomic writes are owned here because vanilla SavedDataStorage writes directly to its destination.
 * Registered instances stay clean in vanilla's cache; ledger mutations flush through this adapter.
 */
public final class NativeCrewData extends SavedData {
    private static final Logger LOG=LoggerFactory.getLogger("PiratesOfMCA/Persistence");
    public static final int VERSION=4;
    private static final Codec<CrewLedger> LEDGER_CODEC=Codec.unboundedMap(Codec.STRING,Codec.STRING).comapFlatMap(state->{
        try{return DataResult.success(CrewLedger.fromState(state));}
        catch(RuntimeException invalid){return DataResult.error(()->"Invalid crew state: "+invalid.getMessage());}
    },CrewLedger::exportState);
    public static final Codec<NativeCrewData> CODEC=RecordCodecBuilder.create(instance->instance.group(
        Codec.intRange(3,VERSION).fieldOf("version").forGetter(data->VERSION),
        Codec.STRING.optionalFieldOf("legacy_sha256","").forGetter(data->data.legacyHash),
        LEDGER_CODEC.fieldOf("ledger").forGetter(data->data.ledger)
    ).apply(instance,(version,hash,ledger)->new NativeCrewData(ledger,hash,version)));
    public static final Identifier ID=Identifier.fromNamespaceAndPath(PiratesOfMCA.ID,"crews");
    public static final SavedDataType<NativeCrewData> TYPE=new SavedDataType<>(ID,()->new NativeCrewData(CrewLedger.fromState(Map.of("schema","2")),""),CODEC,null);
    private final CrewLedger ledger;
    private final String legacyHash;
    private int loadedVersion;
    private Path file;
    private HolderLookup.Provider registries;

    NativeCrewData(CrewLedger ledger,String legacyHash){this(ledger,legacyHash,VERSION);}
    private NativeCrewData(CrewLedger ledger,String legacyHash,int version){this.ledger=Objects.requireNonNull(ledger);this.legacyHash=Objects.requireNonNull(legacyHash);this.loadedVersion=version;}
    public CrewLedger ledger(){return ledger;}
    public String legacyHash(){return legacyHash;}
    public static Path path(MinecraftServer server){return ID.withSuffix(".dat").resolveAgainst(server.getWorldPath(LevelResource.ROOT).resolve("data"));}
    public static NativeCrewData loadWorld(MinecraftServer server) {
        Path target=path(server),legacy=server.getWorldPath(LevelResource.ROOT).resolve("data/piratesmca/crews_v2.properties");
        NativeCrewData data=loadOrImport(target,legacy,server.registryAccess());
        server.overworld().getDataStorage().set(TYPE,data);data.setDirty(false);
        return data;
    }
    static NativeCrewData loadOrImport(Path target,Path legacy,HolderLookup.Provider registries) {
        NativeCrewData data;
        if(Files.exists(target))data=readWithRecovery(target,registries);
        else {
            CrewLedger ledger=new CrewLedger(legacy);
            String hash="";
            if(Files.isRegularFile(legacy))try {
                hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(legacy)));
                Path backup=legacy.resolveSibling(legacy.getFileName()+".migrated-v3.bak");
                if(!Files.exists(backup))Files.copy(legacy,backup);
                LOG.info("Importing legacy crew state {} (SHA256 {}); original and backup preserved",legacy,hash);
            } catch(IOException e){throw new UncheckedIOException("Cannot back up legacy crew state: "+legacy,e);}
              catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
            data=new NativeCrewData(ledger,hash);data.bind(target,registries);data.persist();
        }
        data.bind(target,registries);
        if(data.loadedVersion<VERSION){
            Path original=target.resolveSibling(target.getFileName()+".pre-v"+VERSION+".bak");
            try{if(!Files.exists(original))Files.copy(target,original);}
            catch(IOException failedBackup){throw new UncheckedIOException("Cannot preserve pre-upgrade crew data: "+target,failedBackup);}
            LOG.info("Upgrading crew SavedData {} -> {}; original retained at {}",data.loadedVersion,VERSION,original);data.persist();
        }
        return data;
    }
    void bind(Path target,HolderLookup.Provider registries) {
        this.file=target.toAbsolutePath().normalize();this.registries=Objects.requireNonNull(registries);
        ledger.bindPersistence(this::persist);setDirty(false);
    }
    static final class UnsupportedSchema extends IllegalStateException {
        UnsupportedSchema(String message){super(message);}
    }
    static NativeCrewData read(Path path,HolderLookup.Provider registries) throws IOException {
        CompoundTag root=NbtIo.readCompressed(path,NbtAccounter.create(128L*1024*1024));
        CompoundTag data=root.getCompound("data").orElseThrow(()->new IOException("Missing SavedData envelope: "+path));
        int version=data.getInt("version").orElseThrow(()->new IOException("Missing native crew schema: "+path));
        if(version<3 || version>VERSION)throw new UnsupportedSchema("Unsupported native crew schema "+version+"; original preserved: "+path);
        String ledgerSchema=data.getCompoundOrEmpty("ledger").getStringOr("schema","");
        if(!ledgerSchema.isEmpty() && !ledgerSchema.equals("2"))throw new UnsupportedSchema("Unsupported domain schema; original preserved: "+path);
        try{return CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),data).getOrThrow();}
        catch(RuntimeException invalid){throw new IOException("Invalid native crew data; original preserved: "+path,invalid);}
    }
    static NativeCrewData readWithRecovery(Path path,HolderLookup.Provider registries) {
        try{return read(path,registries);}
        catch(UnsupportedSchema unsupported){throw unsupported;}
        catch(IOException | RuntimeException invalid) {
            Path backup=path.resolveSibling(path.getFileName()+".bak");
            NativeCrewData recovered;
            try {
                recovered=read(backup,registries);
                String fingerprint=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
                Path archived=path.resolveSibling(path.getFileName()+".invalid-"+fingerprint.substring(0,16));
                if(!Files.exists(archived))Files.copy(path,archived);
                Path temporary=path.resolveSibling(path.getFileName()+".recovery.tmp");
                Files.copy(backup,temporary,StandardCopyOption.REPLACE_EXISTING);force(temporary);replace(temporary,path);
                LOG.error("Recovered crew SavedData from {}. Invalid original archived as {}",backup,archived,invalid);
                return recovered;
            } catch(IOException | NoSuchAlgorithmException | RuntimeException failedRecovery) {
                failedRecovery.addSuppressed(invalid);
                throw new IllegalStateException("Crew SavedData and backup cannot be loaded; originals preserved: "+path,failedRecovery);
            }
        }
    }
    /** Called only on the server thread. A failed write retains domain dirty state for an explicit retry. */
    void persist() {
        if(file==null || registries==null)throw new IllegalStateException("Unbound native crew storage");
        try {
            Files.createDirectories(file.getParent());
            if(Files.exists(file)) {
                read(file,registries); // Never overwrite a good backup with externally corrupted data.
                Path backup=file.resolveSibling(file.getFileName()+".bak"),backupTemp=file.resolveSibling(file.getFileName()+".bak.tmp");
                Files.copy(file,backupTemp,StandardCopyOption.REPLACE_EXISTING);force(backupTemp);replace(backupTemp,backup);
            }
            CompoundTag root=new CompoundTag();
            root.putInt("DataVersion",SharedConstants.getCurrentVersion().dataVersion().version());
            root.put("data",CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),this).getOrThrow());
            Path temporary=file.resolveSibling(file.getFileName()+".tmp");
            NbtIo.writeCompressed(root,temporary);force(temporary);replace(temporary,file);
            loadedVersion=VERSION;ledger.acknowledgePersisted();setDirty(false);
        } catch(IOException e){throw new UncheckedIOException("Failed native crew save; original/backup preserved: "+file,e);}
    }
    private static void force(Path file) throws IOException {try(var channel=FileChannel.open(file,StandardOpenOption.WRITE)){channel.force(true);}}
    private static void replace(Path source,Path target) throws IOException {
        try{Files.move(source,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        catch(AtomicMoveNotSupportedException unavailable){Files.move(source,target,StandardCopyOption.REPLACE_EXISTING);}
    }
}
