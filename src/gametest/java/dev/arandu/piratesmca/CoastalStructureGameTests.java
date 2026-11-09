package dev.arandu.piratesmca;

import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.registry.EntitiesMCA;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.*;
import net.minecraft.world.phys.AABB;

/** Fixture pieces run through the actual StructureStart wrapper and entity-add hook. */
public class CoastalStructureGameTests {
    static final class VillagerPiece extends StructurePiece {
        final String tag; final BlockPos pos;
        VillagerPiece(BlockPos pos,String tag){super((context,nbt)->{throw new UnsupportedOperationException();},0,new BoundingBox(pos.getX(),pos.getY(),pos.getZ(),pos.getX()+3,pos.getY()+3,pos.getZ()+3));this.pos=pos;this.tag=tag;}
        protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag){}
        public void postProcess(WorldGenLevel level,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox box,ChunkPos chunk,BlockPos pivot) {
            // One vanilla NPC (MCA replacement) and one direct MCA NPC, as used
            // by independent structure mods. No pirate tags are set by the fixture.
            for(int i=0;i<2;i++) {
                var npc=i==0?EntityTypes.VILLAGER.create(level.getLevel(),EntitySpawnReason.STRUCTURE):EntitiesMCA.FEMALE_VILLAGER.create(level.getLevel(),EntitySpawnReason.STRUCTURE);
                npc.snapTo(pos.getX()+.5+i,pos.getY(),pos.getZ()+.5);npc.setNoAi(true);npc.setAge(0);npc.addTag(tag);level.addFreshEntity(npc);
            }
        }
    }
    private static void expect(GameTestHelper h,boolean value,String message){h.assertTrue(value,Component.literal(message));}
    private static StructureStart start(GameTestHelper h,BlockPos pos,String tag){
        var structure=h.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.withDefaultNamespace("shipwreck"))).value();
        return new StructureStart(structure,new ChunkPos(pos.getX()>>4,pos.getZ()>>4),0,new PiecesContainer(List.of(new VillagerPiece(pos,tag))));
    }
    private static void run(GameTestHelper h,String biome,boolean expected) {
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,2,1));String tag="qa_coast_"+UUID.randomUUID();
        // Biome sampling is zoomed/noisy, so cover neighboring quart cells too.
        var min=pos.offset(-8,-8,-8);var end=pos.offset(48,8,8);var forced=new ArrayList<ChunkPos>();
        for(int x=min.getX()>>4;x<=end.getX()>>4;x++)for(int z=min.getZ()>>4;z<=end.getZ()>>4;z++){level.getChunk(x,z);level.setChunkForced(x,z,true);forced.add(new ChunkPos(x,z));}
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withLevel(level),"fillbiome "+min.getX()+" "+min.getY()+" "+min.getZ()+" "+end.getX()+" "+end.getY()+" "+end.getZ()+" "+biome);
        expect(h,level.getBiome(pos).is(ResourceKey.create(Registries.BIOME,Identifier.parse(biome))),"Fixture biome was not installed: "+biome);
        var first=start(h,pos,tag);var second=start(h,pos.offset(32,0,0),tag);
        for(var s:List.of(first,second)) {
            var c=s.getChunkPos();level.setChunkForced(c.x(),c.z(),true);
            s.placeInChunk(level,level.structureManager(),level.getChunkSource().getGenerator(),level.getRandom(),s.getBoundingBox(),c);
        }
        // A failed/nested generator must not contaminate the next ordinary spawn.
        try{CoastalStructureSpawns.during(level,first,()->{throw new IllegalStateException("fixture abort");});}catch(IllegalStateException intended){}
        var ordinary=h.spawn(EntitiesMCA.MALE_VILLAGER,new BlockPos(2,2,2));ordinary.setNoAi(true);ordinary.setAge(-24000);
        h.runAtTickTime(80,()->{
            try {
                var npcs=level.getEntitiesOfClass(VillagerEntityMCA.class,new AABB(pos).inflate(64),n->n.entityTags().contains(tag));
                expect(h,npcs.size()==4,"Expected four structure NPCs: "+npcs.size());
                Set<UUID> crews=new HashSet<>();
                for(var npc:npcs){
                    expect(h,CrewRuntime.isPirate(npc)==expected,"Biome "+biome+" pirate="+CrewRuntime.isPirate(npc)+" tags="+npc.entityTags());
                    if(expected){var m=CrewRuntime.ledger(level).member(npc.getUUID());expect(h,m!=null && !npc.isBaby(),"Coastal pirate not an adult ledger member");crews.add(m.crew);}
                    else expect(h,!npc.entityTags().contains(PiratesOfMCA.MARKER),"Inland structure NPC was marked");
                }
                if(expected){expect(h,crews.size()==2,"Separate structures merged crews");for(var id:crews)expect(h,CrewRuntime.ledger(level).crew(id).members.size()==2,"One structure split its crew");}
                expect(h,!ordinary.entityTags().contains(PiratesOfMCA.MARKER) && ordinary.isBaby(),"Coastal biome or context leak converted an ordinary child");
                System.out.println("QA_COASTAL_STRUCTURE biome="+biome+" count=4 pirates="+(expected?4:0)+" crews="+crews.size()+" ordinary_child_preserved=true");h.succeed();
            }finally{for(var c:forced)level.setChunkForced(c.x(),c.z(),false);}
        });
    }
    @GameTest(maxTicks=140,padding=64) public void oceanStructuresBecomePirates(GameTestHelper h){run(h,"minecraft:ocean",true);}
    @GameTest(maxTicks=140,padding=64) public void beachStructuresBecomePirates(GameTestHelper h){run(h,"minecraft:beach",true);}
    @GameTest(maxTicks=140,padding=64) public void moddedCommonTagCoastBecomesPirates(GameTestHelper h){run(h,"piratesmca-tests:coast",true);}
    @GameTest(maxTicks=140,padding=64) public void inlandStructuresRemainOrdinary(GameTestHelper h){run(h,"minecraft:plains",false);}
    @GameTest(maxTicks=140,padding=64) public void fishTropicalIslandStructuresBecomePirates(GameTestHelper h){run(h,"fishofthieves:tropical_island",true);}
}
