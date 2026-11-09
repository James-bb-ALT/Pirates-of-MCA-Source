package dev.arandu.piratesmca;

import java.nio.file.*;
import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;

public class NativePersistenceGameTests {
    private static void expect(GameTestHelper h,boolean condition,String message){h.assertTrue(condition,Component.literal(message));}
    @GameTest
    public void versionThreeUpgradesWithoutChangingCrewOrLosingOriginal(GameTestHelper h) throws Exception {
        var folder=Files.createTempDirectory("pmca-v3-upgrade-");var target=folder.resolve("crews.dat");var legacy=folder.resolve("missing.properties");
        var data=NativeCrewData.loadOrImport(target,legacy,h.getLevel().registryAccess());var c=data.ledger().getOrCreate(UUID.randomUUID(),"OLD_SHIP");
        var member=data.ledger().add(UUID.randomUUID(),c.id,"friendly");data.ledger().save();var expected=data.ledger().exportState();
        var root=NbtIo.readCompressed(target,NbtAccounter.unlimitedHeap());root.getCompoundOrEmpty("data").putInt("version",3);NbtIo.writeCompressed(root,target);byte[] original=Files.readAllBytes(target);
        var upgraded=NativeCrewData.loadOrImport(target,legacy,h.getLevel().registryAccess());
        expect(h,expected.equals(upgraded.ledger().exportState()) && upgraded.ledger().member(member.id)!=null,"FLAG-05 upgrade changed existing crew state");
        expect(h,Arrays.equals(original,Files.readAllBytes(folder.resolve("crews.dat.pre-v4.bak"))),"FLAG-05 original v3 snapshot not preserved");
        expect(h,NbtIo.readCompressed(target,NbtAccounter.unlimitedHeap()).getCompoundOrEmpty("data").getIntOr("version",0)==4,"Upgrade did not write v4 envelope");h.succeed();
    }
    @GameTest
    public void registeredNativeStorageOwnsRuntimeLedger(GameTestHelper h) {
        var ledger=CrewRuntime.ledger(h.getLevel());
        var saved=h.getLevel().getServer().overworld().getDataStorage().get(NativeCrewData.TYPE);
        expect(h,saved!=null && saved.ledger()==ledger,"TC-SAVE-04 runtime must use registered native SavedData");h.succeed();
    }
    @GameTest
    public void legacyImportPreservesOriginalAndNativeDataWinsOnReload(GameTestHelper h) throws Exception {
        var folder=Files.createTempDirectory("pmca-native-import-");var legacy=folder.resolve("crews_v2.properties");var target=folder.resolve("crews.dat");
        var old=new CrewLedger(legacy);var c=old.getOrCreate(UUID.randomUUID(),"LEGACY_SHIP");var m=old.add(UUID.randomUUID(),c.id,"greedy");
        old.captain(c,UUID.randomUUID(),true);old.officer(c,m.id,CrewLedger.Role.FIRST_MATE);
        var directive=new CrewLedger.Directive("overworld",1.5,64,-2.25,UUID.randomUUID());old.order(m,CrewLedger.Order.ATTACK,100,directive);old.save();
        byte[] original=Files.readAllBytes(legacy);
        var data=NativeCrewData.loadOrImport(target,legacy,h.getLevel().registryAccess());
        expect(h,Arrays.equals(original,Files.readAllBytes(legacy)) && Arrays.equals(original,Files.readAllBytes(folder.resolve("crews_v2.properties.migrated-v3.bak"))),"TC-SAVE-01 migration changed original legacy state");
        expect(h,data.legacyHash().length()==64 && directive.equals(data.ledger().member(m.id).directive),"Native import lost order metadata or source fingerprint");
        UUID added=UUID.randomUUID();data.ledger().add(added,c.id,"friendly");data.ledger().save();
        var reload=NativeCrewData.loadOrImport(target,legacy,h.getLevel().registryAccess());
        expect(h,reload.ledger().member(added)!=null,"Native reload incorrectly reimported stale legacy file");
        expect(h,Arrays.equals(original,Files.readAllBytes(legacy)),"Native saves must leave legacy source untouched");h.succeed();
    }
    @GameTest
    public void failedNativeWriteKeepsSnapshotAndDirtyState(GameTestHelper h) throws Exception {
        var folder=Files.createTempDirectory("pmca-native-failure-");var target=folder.resolve("crews.dat");
        var data=NativeCrewData.loadOrImport(target,folder.resolve("missing.properties"),h.getLevel().registryAccess());
        byte[] previous=Files.readAllBytes(target);var c=data.ledger().getOrCreate(UUID.randomUUID(),"NEW_SHIP");
        Files.createDirectory(folder.resolve("crews.dat.tmp"));boolean failed=false;
        try{data.ledger().save();}catch(java.io.UncheckedIOException expected){failed=true;}
        expect(h,failed && data.ledger().hasChanges() && Arrays.equals(previous,Files.readAllBytes(target)),"TC-SAVE-02 failed native write lost state or previous snapshot");
        Files.delete(folder.resolve("crews.dat.tmp"));data.ledger().save();
        expect(h,NativeCrewData.read(target,h.getLevel().registryAccess()).ledger().crew(c.id)!=null,"Native save retry lost crew");h.succeed();
    }
    @GameTest
    public void corruptNativeDataRecoversPreviousSnapshotExplicitly(GameTestHelper h) throws Exception {
        var folder=Files.createTempDirectory("pmca-native-recovery-");var target=folder.resolve("crews.dat");
        var data=NativeCrewData.loadOrImport(target,folder.resolve("missing.properties"),h.getLevel().registryAccess());
        var c=data.ledger().getOrCreate(UUID.randomUUID(),"SAVED_SHIP");data.ledger().save();
        UUID newest=UUID.randomUUID();data.ledger().add(newest,c.id,"friendly");data.ledger().save();
        Files.write(target,new byte[]{1,2,3});var recovered=NativeCrewData.readWithRecovery(target,h.getLevel().registryAccess());
        expect(h,recovered.ledger().crew(c.id)!=null && recovered.ledger().member(newest)==null,"Recovery did not select the previous complete snapshot");
        try(var files=Files.list(folder)){expect(h,files.anyMatch(p->p.getFileName().toString().startsWith("crews.dat.invalid-")),"Invalid original was not preserved for recovery review");}
        expect(h,NativeCrewData.read(target,h.getLevel().registryAccess()).ledger().crew(c.id)!=null,"Recovered native file cannot be loaded");h.succeed();
    }
    @GameTest
    public void futureSchemaCannotBeSilentlyDowngradedFromBackup(GameTestHelper h) throws Exception {
        var folder=Files.createTempDirectory("pmca-native-version-");var target=folder.resolve("crews.dat");
        var data=NativeCrewData.loadOrImport(target,folder.resolve("missing.properties"),h.getLevel().registryAccess());
        data.ledger().getOrCreate(UUID.randomUUID(),"SAVED_SHIP");data.ledger().save();
        var root=NbtIo.readCompressed(target,NbtAccounter.unlimitedHeap());root.getCompoundOrEmpty("data").putInt("version",99);NbtIo.writeCompressed(root,target);
        byte[] future=Files.readAllBytes(target);boolean rejected=false;
        try{NativeCrewData.readWithRecovery(target,h.getLevel().registryAccess());}catch(NativeCrewData.UnsupportedSchema expected){rejected=true;}
        expect(h,rejected && Arrays.equals(future,Files.readAllBytes(target)),"Unsupported schema was overwritten by an older backup");h.succeed();
    }
}
