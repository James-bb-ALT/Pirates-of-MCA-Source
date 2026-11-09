package dev.arandu.piratesmca;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.resources.SkinVisualIds;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

public class PirateGameTests {
    private static void expect(GameTestHelper h, boolean condition, String message) {
        h.assertTrue(condition, Component.literal(message));
    }
    private static VillagerEntityMCA pirate(GameTestHelper h, boolean female) {
        var npc=h.spawn(female?EntitiesMCA.FEMALE_VILLAGER:EntitiesMCA.MALE_VILLAGER,new BlockPos(2,2,2));
        npc.addTag(PiratesOfMCA.MARKER);
        npc.addTag(female?"pmca_gender_female":"pmca_gender_male");
        npc.addTag("pmca_adult_required");
        npc.addTag("pmca_bruiser");
        npc.setAge(0);
        npc.setNoAi(true);
        return npc;
    }
    @GameTest(maxTicks=80)
    public void maleIdentity(GameTestHelper h) { identity(h,false); }
    @GameTest(maxTicks=80)
    public void femaleIdentity(GameTestHelper h) { identity(h,true); }
    @GameTest(maxTicks=80)
    public void numericAgeIsNotHumanYears(GameTestHelper h) {
        var a=pirate(h,false);var b=pirate(h,true);
        a.setAge(30);b.setAge(40);
        h.runAtTickTime(40,()->{
            expect(h,a.getAgeState()==AgeState.ADULT && b.getAgeState()==AgeState.ADULT,"TC-SPAWN-04 numeric vanilla Age must normalize tagged adults");
            expect(h,!a.isBaby() && !b.isBaby(),"Tagged adults cannot remain babies");
            h.succeed();
        });
    }
    private void identity(GameTestHelper h, boolean female) {
        var npc=pirate(h,female);
        h.runAtTickTime(40, () -> {
            expect(h,npc.getAgeState()==AgeState.ADULT && !npc.isBaby(),"TC-SPAWN-03 adult");
            expect(h,npc.getGenetics().getGender()==(female?Gender.FEMALE:Gender.MALE),"TC-SPAWN-01/02 gender");
            expect(h,SkinVisualIds.isBodySkin(npc.getSkin(),npc.getGenetics().getGender()),"TC-SPAWN-07 body skin");
            expect(h,SkinVisualIds.isClothing(npc.getClothes()),"TC-SPAWN-06 registered outfit");
            expect(h,npc.getCustomName()!=null && !npc.getCustomName().getString().isBlank(),"BUG-08 name");
            expect(h,npc.getProfession()==PirateProfession.PIRATE && npc.isGuard(),"TC-AI-01 guard profession");
            var m=CrewRuntime.ledger(h.getLevel()).member(npc.getUUID());
            expect(h,m!=null && CrewRuntime.ledger(h.getLevel()).isFreeSolo(m),"TC-SPAWN-11 free summon");
            System.out.println("IDENTITY "+npc.getUUID()+" "+npc.getGenetics().getGender()+" "+npc.getAgeState()+" skin="+npc.getSkin()+" clothes="+npc.getClothes());
            h.succeed();
        });
    }
    @GameTest
    public void commandRegistration(GameTestHelper h) {
        var dispatcher=h.getLevel().getServer().getCommands().getDispatcher();
        expect(h,dispatcher.getRoot().getChild("pirates")!=null,"TC-BOOT-08 command callback");
        h.succeed();
    }
    @GameTest(maxTicks=80)
    public void attackDirectiveCannotHitAnotherTarget(GameTestHelper h) {
        var npc=pirate(h,false);
        var target=h.spawn(net.minecraft.world.entity.EntityTypes.SKELETON,new BlockPos(3,2,2));
        var decoy=h.spawn(net.minecraft.world.entity.EntityTypes.SKELETON,new BlockPos(2,2,3));
        target.setNoAi(true);decoy.setNoAi(true);
        target.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_HELMET));
        decoy.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_HELMET));
        h.runAtTickTime(40,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);var m=l.member(npc.getUUID());var c=l.crew(m.crew);
            l.order(m,CrewLedger.Order.ATTACK,level.getGameTime(),new CrewLedger.Directive(level.dimension().toString(),npc.getX(),npc.getY(),npc.getZ(),target.getUUID()));
            expect(h,!CrewRuntime.mayAttack(npc,decoy),"TC-CREW-08 ATTACK must not substitute a nearby monster");
            float decoyHealth=decoy.getHealth();expect(h,!npc.doHurtTarget(level,decoy),"Guard attack bypassed explicit target");
            expect(h,decoyHealth==decoy.getHealth(),"Unselected monster received damage");
            target.invulnerableTime=0;target.setHealth(target.getMaxHealth());
            expect(h,npc.doHurtTarget(level,target),"Selected hostile must be attackable");
            expect(h,target.getHealth()<target.getMaxHealth(),"Selected hostile did not receive damage");
            target.discard();CrewRuntime.doOrder(level,npc,m,c,level.getGameTime());
            expect(h,m.order==CrewLedger.Order.NONE && m.directive==null,"Unavailable target must end order instead of hunting another NPC");
            h.succeed();
        });
    }
    @GameTest(maxTicks=80,padding=24)
    public void guardAndHoldRespectTheirAnchor(GameTestHelper h) {
        var npc=pirate(h,false);
        var outside=h.spawn(net.minecraft.world.entity.EntityTypes.SKELETON,new BlockPos(18,2,2));outside.setNoAi(true);
        h.runAtTickTime(40,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);var m=l.member(npc.getUUID());var c=l.crew(m.crew);
            var anchor=new CrewLedger.Directive(level.dimension().toString(),npc.getX(),npc.getY(),npc.getZ(),null);
            l.order(m,CrewLedger.Order.GUARD,level.getGameTime(),anchor);
            expect(h,!CrewRuntime.mayAttack(npc,outside),"TC-CREW-08 GUARD cannot hunt outside its defended point");
            l.order(m,CrewLedger.Order.HOLD,level.getGameTime(),anchor);
            CrewRuntime.doOrder(level,npc,m,c,level.getGameTime());
            expect(h,anchor.equals(m.directive) && m.order==CrewLedger.Order.HOLD,"HOLD must retain its original point");
            expect(h,!CrewRuntime.mayAttack(npc,outside),"HOLD must suppress the guard brain's chase");
            CrewRuntime.doOrder(level,npc,m,c,level.getGameTime()+12001);
            expect(h,m.order==CrewLedger.Order.NONE,"Orders must expire");h.succeed();
        });
    }
    @GameTest(maxTicks=80)
    public void firstMateActsWithoutUsurpingCaptain(GameTestHelper h) {
        var mate=pirate(h,false);var sailor=pirate(h,true);
        h.runAtTickTime(40,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_ACTING");
            l.transfer(l.member(mate.getUUID()),c);l.transfer(l.member(sailor.getUUID()),c);
            var absentPlayer=java.util.UUID.randomUUID();l.captain(c,absentPlayer,true);l.officer(c,mate.getUUID(),CrewLedger.Role.FIRST_MATE);
            expect(h,CrewRuntime.effectiveCommander(level,c)==mate,"TC-CREW-09 loaded first mate must manage routine orders when player is offline");
            expect(h,absentPlayer.equals(c.captain) && c.playerCaptain && mate.getUUID().equals(c.actingCaptain),"Acting authority must not replace player captaincy");
            expect(h,l.member(mate.getUUID()).role==CrewLedger.Role.FIRST_MATE,"Acting captain must retain officer role");
            l.captain(c,sailor.getUUID(),false);
            expect(h,CrewRuntime.effectiveCommander(level,c)==sailor && c.actingCaptain==null,"Available definitive captain must end interim authority");
            h.succeed();
        });
    }
    @GameTest(maxTicks=280,padding=24)
    public void activeSailorFollowsThenHoldsPosition(GameTestHelper h) {
        for(int x=0;x<16;x++)for(int z=0;z<8;z++)h.setBlock(x,1,z,net.minecraft.world.level.block.Blocks.STONE);
        for(int x:new int[]{0,15})for(int z:new int[]{0,7}) {
            var position=h.absolutePos(new BlockPos(x,1,z));h.getLevel().setChunkForced(position.getX()>>4,position.getZ()>>4,true);
        }
        var captain=pirate(h,false);var sailor=pirate(h,true);sailor.setNoAi(false);
        var destination=h.absolutePos(new BlockPos(11,2,2));captain.setPos(destination.getX()+.5,destination.getY(),destination.getZ()+.5);
        double[] start={0};CrewLedger.Directive[] held={null};
        h.runAtTickTime(40,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_NAVIGATION");
            l.transfer(l.member(captain.getUUID()),c);l.transfer(l.member(sailor.getUUID()),c);l.captain(c,captain.getUUID(),false);
            l.order(l.member(sailor.getUUID()),CrewLedger.Order.FOLLOW,level.getGameTime(),new CrewLedger.Directive(level.dimension().toString(),sailor.getX(),sailor.getY(),sailor.getZ(),null));
            start[0]=sailor.distanceToSqr(captain);
        });
        h.runAtTickTime(180,()->{
            expect(h,sailor.distanceToSqr(captain)<start[0]-9,"TC-CREW-08 FOLLOW did not navigate toward the NPC captain with active MCA AI");
            held[0]=new CrewLedger.Directive(h.getLevel().dimension().toString(),sailor.getX(),sailor.getY(),sailor.getZ(),null);
            CrewRuntime.ledger(h.getLevel()).order(CrewRuntime.ledger(h.getLevel()).member(sailor.getUUID()),CrewLedger.Order.HOLD,h.getLevel().getGameTime(),held[0]);
            captain.setPos(captain.getX()+8,captain.getY(),captain.getZ());
        });
        h.runAtTickTime(260,()->{
            expect(h,sailor.distanceToSqr(held[0].x(),held[0].y(),held[0].z())<=4,"TC-CREW-08 HOLD drifted away from its saved anchor");
            h.succeed();
        });
    }
    @GameTest(maxTicks=100,padding=40)
    public void witnessedCrimeSpreadsOnlyByLoadedEncounter(GameTestHelper h) {
        var captain=pirate(h,false);var victim=pirate(h,true);var witness=pirate(h,false);var distant=pirate(h,true);
        for(var n:java.util.List.of(captain,victim,witness,distant))n.setNoGravity(true);
        var far=h.absolutePos(new BlockPos(32,2,2));distant.setPos(far.getX(),far.getY(),far.getZ());
        h.getLevel().setChunkForced(far.getX()>>4,far.getZ()>>4,true);
        java.util.UUID[] crewId={null},crimeId={null};
        h.runAtTickTime(40,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_RUMOR");
            crewId[0]=c.id;
            for(var n:java.util.List.of(captain,victim,witness,distant))l.transfer(l.member(n.getUUID()),c);
            l.captain(c,captain.getUUID(),false);
            victim.invulnerableTime=0;victim.hurtServer(level,level.damageSources().mobAttack(captain),1000f);
            expect(h,!victim.isAlive(),"Murder fixture must produce real death");
            expect(h,c.socialEvents.stream().noneMatch(e->e.kind.equals("CAPTAIN_ASSAULT")),"Fatal hit must not create a second assault after the death callback");
            var crime=c.socialEvents.stream().filter(e->e.kind.equals("MURDER")).findFirst().orElseThrow();
            crimeId[0]=crime.id;
            var informed=l.member(witness.getUUID());var uninformed=l.member(distant.getUUID());
            expect(h,informed.witnessedEvents.contains(crime.id),"TC-SOC-07 nearby witness did not learn the murder");
            expect(h,!uninformed.witnessedEvents.contains(crime.id) && uninformed.resentment==0,"TC-SOC-07 distant member learned the crime telepathically");
            captain.setPos(far.getX(),far.getY(),far.getZ());distant.setPos(witness.getX()+1,witness.getY(),witness.getZ());
        });
        h.runAtTickTime(45,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);var c=l.crew(crewId[0]);
            var informed=l.member(witness.getUUID());var uninformed=l.member(distant.getUUID());
            var first=witness.getUUID().compareTo(distant.getUUID())<0?witness:distant;
            var second=first==witness?distant:witness;
            System.out.println("QA_RUMOR_FIXTURE first="+first.getUUID()+" pos="+first.position()+" second="+second.getUUID()+" pos="+second.position()+" distance="+first.distanceToSqr(second)+" los="+first.getSensing().hasLineOfSight(second)+" pirate="+CrewRuntime.isPirate(second)+" crew="+l.member(second.getUUID()).crew+" crime="+crimeId[0]);
            CrewRuntime.socialMeeting(level,first,l.member(first.getUUID()),c,level.getGameTime());
            System.out.println("QA_RUMOR_RESULT sender="+informed.witnessedEvents.contains(crimeId[0])+" receiver="+uninformed.witnessedEvents.contains(crimeId[0])+" resentment="+informed.resentment+"/"+uninformed.resentment+" alive="+informed.alive+"/"+uninformed.alive);
            expect(h,uninformed.witnessedEvents.contains(crimeId[0]) && uninformed.resentment>0 && uninformed.resentment<informed.resentment,"TC-SOC-07 rumor must spread at the meeting with lower credibility");
            int resentment=uninformed.resentment;CrewRuntime.socialMeeting(level,first,l.member(first.getUUID()),c,level.getGameTime()+100);
            expect(h,uninformed.resentment==resentment,"Same rumor applied twice");h.succeed();
        });
    }
    @GameTest(maxTicks=200)
    public void conspiracyDoesNotRequireMatchingWorldAndEntityClock(GameTestHelper h) {
        var captain=pirate(h,false);var a=pirate(h,true);var b=pirate(h,false);
        java.util.UUID[] crew={null};
        h.runAtTickTime(40,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_CLOCK_PHASE");crew[0]=c.id;
            for(var npc:java.util.List.of(captain,a,b))l.transfer(l.member(npc.getUUID()),c);
            l.captain(c,captain.getUUID(),false);
            for(var npc:java.util.List.of(a,b)){var m=l.member(npc.getUUID());m.resentment=90;m.fear=10;m.ambition=90;}
            for(var npc:java.util.List.of(captain,a,b))npc.tickCount=(int)Math.floorMod(level.getGameTime(),20)+1;
        });
        h.runAtTickTime(180,()->{
            var c=CrewRuntime.ledger(h.getLevel()).crew(crew[0]);
            expect(h,c.phase==CrewLedger.Phase.ULTIMATUM,"TC-SOC-03 eligible conspiracy never advances when entity tick phase differs from world clock");
            expect(h,c.ultimatumRemaining>0,"Ultimatum must retain its loaded-time warning window");h.succeed();
        });
    }
    @GameTest(maxTicks=100,padding=20)
    public void playerYieldStopsAuthorityAndCrewDamage(GameTestHelper h) {
        var a=pirate(h,false);var b=pirate(h,true);var player=h.makeMockServerPlayerInLevel();
        player.setPos(a.getX()+2,a.getY(),a.getZ());player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD,7));
        h.runAtTickTime(40,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_YIELD");
            l.transfer(l.member(a.getUUID()),c);l.transfer(l.member(b.getUUID()),c);l.captain(c,player.getUUID(),true);c.phase=CrewLedger.Phase.REBELLION;
            float health=player.getHealth();int emeralds=player.getInventory().countItem(net.minecraft.world.item.Items.EMERALD);
            try {
                int result=level.getServer().getCommands().getDispatcher().execute("pirates captain yield",player.createCommandSourceStack().withPermission(net.minecraft.server.permissions.PermissionSet.NO_PERMISSIONS));
                expect(h,result==1,"TC-CREW-10 actual yield command must succeed during rebellion");
            } catch(com.mojang.brigadier.exceptions.CommandSyntaxException e){throw new AssertionError(e);}
            expect(h,!c.playerCaptain && c.captain!=null && CrewRuntime.playerCrew(level,player)==null,"Yield must remove player authority and elect one successor");
            expect(h,player.getHealth()==health && player.getInventory().countItem(net.minecraft.world.item.Items.EMERALD)==emeralds,"Yield changed player health or inventory");
            var captain=c.captain.equals(a.getUUID())?a:b;
            expect(h,!captain.doHurtTarget(level,player),"Crew hurt a surrendered captain during safe exit");
            expect(h,CrewRuntime.issueOrder(level,player,CrewLedger.Order.FOLLOW,true)==0,"Former captain retained command authority");
            captain.invulnerableTime=0;captain.hurtServer(level,level.damageSources().playerAttack(player),1);
            expect(h,c.surrenderProtectionUntil==0,"A new real attack must revoke safe exit");
            h.succeed();
        });
    }
    @GameTest(maxTicks=140,padding=40)
    public void woundedPirateRetreatsFromVisibleMonster(GameTestHelper h) {
        record TestChunk(int x,int z){}
        var chunks=new java.util.HashSet<TestChunk>();
        for(int x=0;x<32;x++)for(int z=0;z<32;z++) {
            h.setBlock(x,1,z,net.minecraft.world.level.block.Blocks.STONE);var p=h.absolutePos(new BlockPos(x,1,z));chunks.add(new TestChunk(p.getX()>>4,p.getZ()>>4));
        }
        for(var chunk:chunks)h.getLevel().setChunkForced(chunk.x(),chunk.z(),true);
        var npc=pirate(h,false);npc.setNoAi(false);var point=h.absolutePos(new BlockPos(12,2,16));npc.setPos(point.getX()+.5,point.getY(),point.getZ()+.5);
        net.minecraft.world.entity.monster.Monster[] threat={null};
        double[] start={0};float[] monsterHealth={0};
        h.runAtTickTime(40,()->{
            npc.setPos(point.getX()+.5,point.getY(),point.getZ()+.5);npc.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);npc.getNavigation().stop();
            npc.setHealth(npc.getMaxHealth()*.25f);
            var monster=h.spawn(net.minecraft.world.entity.EntityTypes.ZOMBIE,new BlockPos(14,2,16));monster.setNoAi(true);
            monster.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET));threat[0]=monster;
            start[0]=npc.distanceToSqr(monster);monsterHealth[0]=monster.getHealth();
            expect(h,monster.isAlive() && monsterHealth[0]==monster.getMaxHealth(),"Retreat fixture requires a living healthy threat introduced after the wound");
            System.out.println("QA_RETREAT_START npc_health="+npc.getHealth()+" threat_health="+monster.getHealth()+" threat_alive="+monster.isAlive()+" distance="+start[0]);
        });
        h.runAtTickTime(100,()->{
            var monster=threat[0];
            System.out.println("QA_RETREAT_END npc_health="+npc.getHealth()+" threat_health="+monster.getHealth()+" threat_alive="+monster.isAlive()+" distance="+npc.distanceToSqr(monster)+" retreating="+CrewRuntime.ledger(h.getLevel()).member(npc.getUUID()).retreating+" nav_done="+npc.getNavigation().isDone()+" goal="+CrewRuntime.retreatGoal(h.getLevel(),npc.getUUID())+" pos="+npc.position()+" activity="+npc.getBrain().getActiveNonCoreActivity()+" walk="+npc.getBrain().getMemoryInternal(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET));
            expect(h,monster.isAlive() && npc.getHealth()<npc.getMaxHealth()*.5f && CrewRuntime.ledger(h.getLevel()).member(npc.getUUID()).retreating,"Retreat scenario lost its live threat or injured precondition");
            expect(h,npc.isAlive() && npc.distanceToSqr(monster)>start[0]+9,"TC-AI-02 wounded pirate did not navigate away from visible monster");
            expect(h,monster.getHealth()==monsterHealth[0],"Wounded pirate continued attacking instead of retreating");
            h.succeed();
        });
    }
    @GameTest(maxTicks=80)
    public void survivalClaimRequiresVacancyAndSupport(GameTestHelper h) {
        var a=pirate(h,false);var b=pirate(h,true);
        var player=h.makeMockServerPlayerInLevel();
        player.setPos(a.getX()+1,a.getY(),a.getZ());
        h.runAtTickTime(40,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);
            var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_CLAIM");
            l.transfer(l.member(a.getUUID()),c);l.transfer(l.member(b.getUUID()),c);
            l.captain(c,a.getUUID(),false);
            a.getVillagerBrain().getMemoriesForPlayer(player).setHearts(60);
            b.getVillagerBrain().getMemoriesForPlayer(player).setHearts(60);
            var source=player.createCommandSourceStack().withPermission(net.minecraft.server.permissions.PermissionSet.NO_PERMISSIONS);
            try {
                var dispatcher=level.getServer().getCommands().getDispatcher();
                expect(h,dispatcher.execute("pirates captain claim",source)==0,"TC-CREW-04 claim must not overthrow an active captain");
                l.depose(c,level.getGameTime());
                a.getVillagerBrain().getMemoriesForPlayer(player).setHearts(0);
                b.getVillagerBrain().getMemoriesForPlayer(player).setHearts(0);
                expect(h,dispatcher.execute("pirates captain claim",source)==0,"TC-CREW-04 no support must fail");
                a.getVillagerBrain().getMemoriesForPlayer(player).setHearts(40);
                expect(h,dispatcher.execute("pirates captain claim",source)==1,"TC-CREW-04 half of two adults with 40 hearts must succeed");
                expect(h,c.playerCaptain && player.getUUID().equals(c.captain),"Claim must grant a single player captaincy");
            } catch(com.mojang.brigadier.exceptions.CommandSyntaxException e) {throw new AssertionError(e);}
            h.succeed();
        });
    }
    @GameTest
    public void seapostMixinTargets(GameTestHelper h) throws Exception {
        Class.forName("com.stevekung.fishofthieves.structure.SeapostPieces$SeapostPiece");
        h.succeed();
    }
    @GameTest(maxTicks=80)
    public void unloadCleansIndex(GameTestHelper h) {
        var npc=pirate(h,false);
        h.runAtTickTime(40,()->{
            int before=CrewRuntime.loadedCount(h.getLevel());
            npc.discard();
            expect(h,CrewRuntime.loadedCount(h.getLevel())==before-1,"TC-SAVE-04 unload must release NPC reference");
            h.succeed();
        });
    }
    @GameTest(maxTicks=80)
    public void tradeLevels(GameTestHelper h) {
        var npcs=new java.util.ArrayList<VillagerEntityMCA>();
        for(int i=0;i<5;i++)npcs.add(pirate(h,i%2==0));
        h.runAtTickTime(40,()->{
            for(int i=0;i<5;i++) {
                var npc=npcs.get(i);
                npc.setVillagerData(npc.getVillagerData().withLevel(i+1));
                expect(h,!npc.getOffers().isEmpty(),"TC-DATA-01 offers at level "+(i+1));
                System.out.println("QA_TRADE level="+(i+1)+" offers="+npc.getOffers().size());
            }
            h.succeed();
        });
    }
    @GameTest(maxTicks=80)
    public void partialIdentityPreservesGenetics(GameTestHelper h) {
        var npc=pirate(h,false);
        npc.setCustomName(null);
        npc.setSkin("");
        npc.getGenetics().setGene(net.conczin.mca.entity.ai.Genetics.SIZE,0.31f);
        h.runAtTickTime(40,()->{
            expect(h,npc.getGenetics().getGene(net.conczin.mca.entity.ai.Genetics.SIZE)==0.31f,"TC-SPAWN-12 must preserve existing genetics");
            h.succeed();
        });
    }
    @GameTest(maxTicks=80)
    public void lethalBrawlHitAndExternalDamage(GameTestHelper h) {
        var a=pirate(h,false);var b=pirate(h,true);
        h.runAtTickTime(40,()->{
            var l=CrewRuntime.ledger(h.getLevel());var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_BRAWL");
            l.transfer(l.member(a.getUUID()),c);l.transfer(l.member(b.getUUID()),c);
            expect(h,CrewRuntime.startBrawl(h.getLevel(),a,b,h.getLevel().getGameTime()),"Brawl must start");
            b.setHealth(b.getMaxHealth());
            b.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(a),1000f);
            expect(h,b.isAlive() && b.getHealth()>=b.getMaxHealth()*.25f,"TC-AI-04 fatal hit must be capped before damage");
            System.out.println("QA_BRAWL health="+b.getHealth()+" max="+b.getMaxHealth());
            // Same tick, distinct source: protection cannot create immunity.
            b.invulnerableTime=0;
            b.hurtServer(h.getLevel(),h.getLevel().damageSources().fall(),1000f);
            expect(h,!b.isAlive(),"TC-AI-05 external fall damage must remain lethal");
            expect(h,!c.members.contains(b.getUUID()),"TC-SAVE-02 environmental death must leave active roster");
            h.succeed();
        });
    }
    @GameTest
    public void ordinaryVillagersDoNotSelectPirateOutfits(GameTestHelper h) {
        var clothes=net.conczin.mca.resources.ClothingList.getInstance().clothing.values();
        for(var gender:java.util.List.of(Gender.MALE,Gender.FEMALE)) {
            var ordinary=net.conczin.mca.resources.SkinSelection.clothingForProfession(clothes,gender,"minecraft:fisherman");
            expect(h,ordinary.stream().noneMatch(c->c.getIdentifier().startsWith("piratesmca:")),"TC-AI-03 pirate outfits leak into ordinary MCA professions");
        }
        h.succeed();
    }
    @GameTest(maxTicks=100)
    public void absentCaptainIsNotDead(GameTestHelper h) {
        var captain=pirate(h,false);var rebel=pirate(h,true);
        var crewId=java.util.UUID.randomUUID();
        h.runAtTickTime(40,()->{
            var l=CrewRuntime.ledger(h.getLevel());var c=l.getOrCreate(crewId,"QA_ABSENCE");
            l.transfer(l.member(captain.getUUID()),c);l.transfer(l.member(rebel.getUUID()),c);
            l.captain(c,captain.getUUID(),false);
            c.phase=CrewLedger.Phase.REBELLION;l.member(rebel.getUUID()).resentment=90;
            captain.discard();
        });
        h.runAtTickTime(80,()->{
            var c=CrewRuntime.ledger(h.getLevel()).crew(crewId);
            expect(h,captain.getUUID().equals(c.captain),"TC-SOC-05 absence must not depose an NPC captain");
            h.succeed();
        });
    }
    @GameTest(maxTicks=100)
    public void mcaCustomizationSurvivesRepairTick(GameTestHelper h) {
        var npc=pirate(h,false);
        String[] chosen=new String[1];
        h.runAtTickTime(40,()->{
            chosen[0]=net.conczin.mca.resources.ClothingList.getInstance().clothing.keySet().stream()
                .filter(id->id.startsWith("mca:") && SkinVisualIds.isClothing(id,Gender.MALE)).sorted().findFirst().orElseThrow();
            npc.setClothes(chosen[0]);npc.setClothingLocked(true);
            npc.getVillagerBrain().setPersonality(net.conczin.mca.entity.ai.relationship.Personality.FRIENDLY);
            npc.getVillagerBrain().modifyMoodValue(50-npc.getVillagerBrain().getMoodValue());
        });
        h.runAtTickTime(80,()->{
            expect(h,chosen[0].equals(npc.getClothes()),"TC-SPAWN-09 intentional MCA wardrobe was overwritten");
            expect(h,npc.getVillagerBrain().getPersonality()==net.conczin.mca.entity.ai.relationship.Personality.FRIENDLY,"TC-SPAWN-12 native personality was overwritten");
            expect(h,npc.getVillagerBrain().getMoodValue()>0,"MCA mood was forcibly clamped every second");
            h.succeed();
        });
    }
    @GameTest(maxTicks=100)
    public void existingMcaHeartsSurviveFirstMeet(GameTestHelper h) {
        var npc=pirate(h,false);
        var player=h.makeMockServerPlayerInLevel();
        player.setPos(npc.getX()+1,npc.getY(),npc.getZ());
        npc.getVillagerBrain().getMemoriesForPlayer(player).setHearts(60);
        h.runAtTickTime(40,()->{
            expect(h,npc.getVillagerBrain().getMemoriesForPlayer(player).getHearts()>=60,"TC-SAVE-01 existing MCA hearts were overwritten on first pirate encounter");
            h.succeed();
        });
    }
    @GameTest(maxTicks=100)
    public void brawlFireAspectDoesNotCauseDelayedDeath(GameTestHelper h) {
        var a=pirate(h,false);var b=pirate(h,true);
        h.runAtTickTime(40,()->{
            var l=CrewRuntime.ledger(h.getLevel());var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_FIRE");
            l.transfer(l.member(a.getUUID()),c);l.transfer(l.member(b.getUUID()),c);
            expect(h,CrewRuntime.startBrawl(h.getLevel(),a,b,h.getLevel().getGameTime()),"Brawl must start");
            var sword=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SWORD);
            sword.enchant(h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT),2);
            a.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,sword);
            expect(h,a.doHurtTarget(h.getLevel(),b),"Enchanted weapon hit must execute");
            expect(h,b.getRemainingFireTicks()<=0,"TC-AI-04 brawl weapon started damaging fire");
            b.igniteForTicks(100);
            expect(h,b.getRemainingFireTicks()>0,"TC-AI-05 unrelated fire must still apply");
            h.succeed();
        });
    }
    @GameTest(maxTicks=220)
    public void pirateDefendsAgainstZombie(GameTestHelper h) { defend(h,net.minecraft.world.entity.EntityTypes.ZOMBIE); }
    @GameTest(maxTicks=220)
    public void pirateDefendsAgainstDrowned(GameTestHelper h) { defend(h,net.minecraft.world.entity.EntityTypes.DROWNED); }
    @GameTest(maxTicks=220)
    public void pirateDefendsAgainstSkeleton(GameTestHelper h) { defend(h,net.minecraft.world.entity.EntityTypes.SKELETON); }
    private <T extends net.minecraft.world.entity.monster.Monster> void defend(GameTestHelper h,net.minecraft.world.entity.EntityType<T> type) {
        for(int x=0;x<8;x++)for(int z=0;z<8;z++)h.setBlock(x,1,z,net.minecraft.world.level.block.Blocks.STONE);
        var npc=pirate(h,false);npc.setNoAi(false);
        var monster=h.spawn(type,new BlockPos(3,2,2));monster.setNoAi(true);
        monster.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET));
        h.runAtTickTime(180,()->{
            expect(h,monster.getHealth()<monster.getMaxHealth(),"TC-AI-01 pirate never damaged "+type);
            System.out.println("QA_DEFENSE target="+type+" health="+monster.getHealth());
            h.succeed();
        });
    }
    @GameTest(maxTicks=100)
    public void guardCannotAttackCrewOrCaptainBeforeMutiny(GameTestHelper h) {
        var captain=pirate(h,false);var crew=pirate(h,true);var other=pirate(h,false);
        h.runAtTickTime(40,()->{
            var l=CrewRuntime.ledger(h.getLevel());var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_FRIENDLY_FIRE");
            for(var n:java.util.List.of(captain,crew,other))l.transfer(l.member(n.getUUID()),c);
            l.captain(c,captain.getUUID(),false);
            expect(h,!crew.doHurtTarget(h.getLevel(),captain),"TC-AI-06 subordinate retaliated before a mutiny");
            expect(h,!other.doHurtTarget(h.getLevel(),crew),"TC-AI-09 guard caused unauthorized friendly fire");
            expect(h,captain.doHurtTarget(h.getLevel(),crew),"Captain discipline must retain real damage");
            expect(h,l.member(crew.getUUID()).resentment>=9,"Captain assault must create resentment");
            h.succeed();
        });
    }
    @GameTest(maxTicks=100)
    public void brawlPoisonIsScopedToWeaponContext(GameTestHelper h) {
        var a=pirate(h,false);var b=pirate(h,true);
        h.runAtTickTime(40,()->{
            var l=CrewRuntime.ledger(h.getLevel());var c=l.getOrCreate(java.util.UUID.randomUUID(),"QA_POISON");
            l.transfer(l.member(a.getUUID()),c);l.transfer(l.member(b.getUUID()),c);
            expect(h,CrewRuntime.startBrawl(h.getLevel(),a,b,h.getLevel().getGameTime()),"Brawl must start");
            boolean applied=CrewRuntime.attackWithContext(a,b,()->b.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,200),a));
            expect(h,!applied && !b.hasEffect(net.minecraft.world.effect.MobEffects.POISON),"TC-AI-04 brawl weapon poison applied");
            expect(h,b.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,200)),"TC-AI-05 external poison must still apply");
            h.succeed();
        });
    }
    @GameTest(maxTicks=180)
    public void quartermasterSeparatesBrawl(GameTestHelper h) {
        var a=pirate(h,false);var b=pirate(h,true);var officer=pirate(h,false);var id=java.util.UUID.randomUUID();
        h.runAtTickTime(40,()->{
            var l=CrewRuntime.ledger(h.getLevel());var c=l.getOrCreate(id,"QA_QUARTERMASTER");
            for(var n:java.util.List.of(a,b,officer))l.transfer(l.member(n.getUUID()),c);
            a.setInvulnerable(true);b.setInvulnerable(true);
            l.officer(c,officer.getUUID(),CrewLedger.Role.QUARTERMASTER);
            expect(h,CrewRuntime.startBrawl(h.getLevel(),a,b,h.getLevel().getGameTime()),"Brawl must start");
        });
        h.runAtTickTime(140,()->{
            expect(h,!CrewRuntime.inBrawl(h.getLevel(),a.getUUID()) && !CrewRuntime.inBrawl(h.getLevel(),b.getUUID()),"TC-AI-07 quartermaster did not separate both duelists");
            expect(h,CrewRuntime.ledger(h.getLevel()).crew(id).events.stream().anyMatch(e->e.startsWith("BRAWL_SEPARATED")),"Separation needs an actual event");
            h.succeed();
        });
    }
}
