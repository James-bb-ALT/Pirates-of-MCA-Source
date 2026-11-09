package dev.arandu.piratesmca;

import java.util.*;
import com.stevekung.fishofthieves.structure.SeapostPieces;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.registry.EntitiesMCA;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;

/** Real Fish template -> vanilla villagers -> MCA SpawnQueue -> pirate behavior. */
public class SpawnIsolationGameTests {
    private static void expect(GameTestHelper h,boolean value,String message){h.assertTrue(value,Component.literal(message));}
    private static List<VillagerEntityMCA> tagged(GameTestHelper h,String tag){
        return h.getLevel().getEntitiesOfClass(VillagerEntityMCA.class,new AABB(h.absolutePos(BlockPos.ZERO)).inflate(256),n->n.entityTags().contains(tag));
    }
    @GameTest(maxTicks=200,padding=96)
    public void realSeapostTemplateSpawnsTwoAdultsInOneCrew(GameTestHelper h) {
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(8,2,8));
        var piece=new SeapostPieces.SeapostPiece(level.getStructureManager(),Identifier.fromNamespaceAndPath("fishofthieves","seapost"),pos,Rotation.NONE,0);
        // Fish moves the template to sea level in postProcess; the clipping box
        // must cover that height and the origin must be read after this move.
        var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(pos.getX()-48,-64,pos.getZ()-48,pos.getX()+48,319,pos.getZ()+48);
        piece.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),level.getRandom(),clip,new ChunkPos(pos.getX()>>4,pos.getZ()>>4),pos);
        var box=piece.getBoundingBox();String origin="pmca_origin_"+box.minX()+"_"+box.minY()+"_"+box.minZ();
        for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)level.setChunkForced(x,z,true);
        h.runAtTickTime(100,()->{
            try {
            var npcs=tagged(h,origin);expect(h,npcs.size()==2,"TC-SPAWN-10 real seapost should produce two MCA pirates, got "+npcs.size());
            var ledger=CrewRuntime.ledger(level);UUID crew=null;
            for(var npc:npcs){
                System.out.println("QA_SEAPOST_NPC id="+npc.getUUID()+" ticks="+npc.tickCount+" profession="+npc.getProfessionId()+" age="+npc.getAgeState()+" pos="+npc.position()+" tags="+npc.entityTags());
                expect(h,CrewRuntime.isPirate(npc),"Seapost villager was not converted to pirate; ticks="+npc.tickCount+" profession="+npc.getProfessionId()+" tags="+npc.entityTags());
                expect(h,npc.getAgeState()==AgeState.ADULT && !npc.isBaby(),"Seapost pirate is a child");
                expect(h,npc.getCustomName()!=null && !npc.getName().getString().isBlank() && !npc.getName().getString().equalsIgnoreCase("Unnamed"),"Seapost pirate has no name");
                expect(h,!npc.getSkin().isBlank() && !npc.getClothes().isBlank(),"Seapost pirate has missing appearance");
                expect(h,npc.getClothes().startsWith("piratesmca:"),"Seapost pirate retained ordinary villager clothing: "+npc.getClothes());
                expect(h,npc.getGenetics().getGender().name().equals(npc.entityTags().contains("pmca_gender_male")?"MALE":"FEMALE"),"Seapost gender tag and genetics disagree");
                var member=ledger.member(npc.getUUID());expect(h,member!=null,"Seapost pirate missing ledger member");
                if(crew==null)crew=member.crew;else expect(h,crew.equals(member.crew),"One seapost split into separate crews");
            }
            expect(h,ledger.crew(crew).captain!=null && ledger.crew(crew).members.size()==2,"Seapost crew missing captain or members");
            System.out.println("QA_REAL_SEAPOST count=2 crew="+crew+" origin="+origin);h.succeed();
            } finally {
                for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)level.setChunkForced(x,z,false);
            }
        });
    }
    @GameTest(maxTicks=160,padding=32)
    public void ordinaryVanillaVillagersConvertWithoutBecomingPirates(GameTestHelper h) {
        var level=h.getLevel();String tag="qa_ordinary_"+UUID.randomUUID();
        for(int i=0;i<12;i++){
            var npc=EntityTypes.VILLAGER.create(level,EntitySpawnReason.STRUCTURE);
            var pos=h.absolutePos(new BlockPos(1+i%4,2,1+i/4));npc.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
            npc.setNoAi(true);npc.addTag(tag);npc.setAge(i%3==0?-24000:0);
            npc.setVillagerData(npc.getVillagerData().withProfession(level.registryAccess(),i%2==0?VillagerProfession.FISHERMAN:VillagerProfession.NONE));
            level.addFreshEntity(npc);
        }
        h.runAtTickTime(100,()->{
            var npcs=tagged(h,tag);expect(h,npcs.size()==12,"MCA conversion did not complete for ordinary villagers: "+npcs.size());
            long children=npcs.stream().filter(VillagerEntityMCA::isBaby).count();expect(h,children==4,"Ordinary children were aged into adult pirates");
            for(var npc:npcs){
                expect(h,!npc.entityTags().contains(PiratesOfMCA.MARKER) && npc.getProfession()!=PirateProfession.PIRATE,"Ordinary village spawn became pirate");
                expect(h,!npc.getClothes().startsWith("piratesmca:"),"Ordinary village spawn selected pirate outfit");
                expect(h,CrewRuntime.ledger(level).member(npc.getUUID())==null,"Ordinary villager entered crew ledger");
            }
            System.out.println("QA_ORDINARY_CONVERSION adults=8 children=4 pirates=0");h.succeed();
        });
    }
    @GameTest(maxTicks=120,padding=32)
    public void ordinaryMcaVillagersKeepProfessionAndAgeBesidePirate(GameTestHelper h) {
        var level=h.getLevel();var pirate=h.spawn(EntitiesMCA.MALE_VILLAGER,new BlockPos(2,2,2));
        pirate.addTag(PiratesOfMCA.MARKER);pirate.addTag("pmca_adult_required");pirate.setNoAi(true);
        var ordinary=new ArrayList<VillagerEntityMCA>();
        for(int i=0;i<24;i++){
            var npc=h.spawn(i%2==0?EntitiesMCA.MALE_VILLAGER:EntitiesMCA.FEMALE_VILLAGER,new BlockPos(3+i%4,2,2+i/4));
            npc.setNoAi(true);npc.setAge(i%3==0?-24000:0);npc.setProfession(BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(i%2==0?VillagerProfession.FISHERMAN:VillagerProfession.NONE).value());
            npc.initialize(EntitySpawnReason.SPAWN_ITEM_USE);ordinary.add(npc);
        }
        h.runAtTickTime(80,()->{
            expect(h,CrewRuntime.isPirate(pirate),"Tagged control pirate did not activate");
            for(int i=0;i<ordinary.size();i++){
                var npc=ordinary.get(i);expect(h,!npc.entityTags().contains(PiratesOfMCA.MARKER) && !CrewRuntime.isPirate(npc),"Nearby pirate contaminated ordinary MCA villager");
                expect(h,npc.isBaby()==(i%3==0),"Nearby pirate changed ordinary child's age");
                expect(h,npc.getProfession()==BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(i%2==0?VillagerProfession.FISHERMAN:VillagerProfession.NONE).value(),"Ordinary MCA profession changed");
                expect(h,!npc.getClothes().startsWith("piratesmca:"),"Ordinary MCA clothing became pirate");
                expect(h,CrewRuntime.ledger(level).member(npc.getUUID())==null,"Ordinary MCA villager entered crew ledger");
            }
            System.out.println("QA_NEARBY_ISOLATION common=24 pirates=0 children=8");h.succeed();
        });
    }
}
