package dev.arandu.piratesmca;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import static dev.arandu.piratesmca.PirateDialogueCatalog.Signal;

/** Real integrated-server events, native response dialogs and EN/PT client rendering. */
final class DialogueClientReview {
 private static void check(boolean value,String reason){PirateClientGameTests.expect(value,reason);}
 private static ServerPlayer player(net.minecraft.server.MinecraftServer s){return s.getPlayerList().getPlayers().getFirst();}
 private static VillagerEntityMCA npc(ServerLevel level,Map<String,UUID> ids,String name){return (VillagerEntityMCA)level.getEntity(ids.get(name));}
 private static void ready(VillagerEntityMCA n){var m=PirateDialogues.memory(n);m.lastSpeech=((ServerLevel)n.level()).getGameTime()-2000;PirateDialogues.save(n,m);}
 private static void locale(ClientGameTestContext c,String name){
  var reload=new AtomicReference<CompletableFuture<Void>>();c.runOnClient(mc->{mc.getLanguageManager().setSelected(name);mc.options.languageCode=name;reload.set(mc.reloadResourcePacks());});
  c.waitFor(mc->reload.get()!=null&&reload.get().isDone(),600);reload.get().join();c.waitFor(mc->mc.gui.overlay()==null,600);c.waitTicks(10);
 }
 private static void scene(ClientGameTestContext c,TestSingleplayerContext w,Map<String,UUID> ids,String actor,String id,String reply){
  UUID token=w.getServer().computeOnServer(s->{var p=player(s);var n=npc(s.overworld(),ids,actor);ready(n);var t=PirateDialogues.offer(n,p,id,false);check(t!=null,"DLG scene context not eligible: "+id+" facts="+PirateDialogues.facts(n,p));check(PirateDialogues.open(p,t)==1,"DLG scene did not open "+id);return t;});
  c.waitFor(mc->mc.gui.screen()!=null&&mc.gui.screen().getTitle().getString().equals(Component.translatable("piratesmca.talk.topics").getString()),100);c.waitTicks(2);c.takeScreenshot("DLG-SCENE-"+id);
  w.getServer().runOnServer(s->{var p=player(s);check(PirateDialogues.answer(p,token,reply)==1,"DLG reply failed "+id);check(PirateDialogues.answer(p,token,reply)==0,"DLG replay accepted "+id);System.out.println("DLG_SCENE_PASS id="+id+" reply="+reply);});c.setScreen(()->null);c.waitTicks(1);
 }
 static void run(ClientGameTestContext c){
  try(var w=c.worldBuilder().create()){
   w.getServer().runOnServer(s->net.conczin.mca.server.world.data.PlayerSaveData.get(player(s)).setEntityDataSet(true));c.setScreen(()->null);
   w.getServer().runCommand("gamemode creative @a");w.getServer().runCommand("tp @a 2048.0 -60.0 -3.0 0 0");
   w.getServer().runCommand("summon mca:male_villager 2046 -60 0 {Age:0,NoAI:1b,Tags:[\"pmca_seapost\",\"pmca_friendly\"]}");
   w.getServer().runCommand("summon mca:male_villager 2050 -60 0 {Age:0,NoAI:1b,Tags:[\"pmca_seapost\",\"pmca_greedy\"]}");
   w.getServer().runCommand("summon mca:male_villager 2048 -60 0 {Age:0,NoAI:1b,Tags:[\"pmca_seapost\",\"pmca_suspicious\"]}");
   w.getServer().runCommand("summon mca:male_villager 2044 -60 0 {Age:0,NoAI:1b}");c.waitTicks(60);
   Map<String,UUID> ids=w.getServer().computeOnServer(s->{
    var map=new HashMap<String,UUID>();var level=s.overworld();var p=player(s);var ledger=CrewRuntime.ledger(level);var crew=ledger.getOrCreate(UUID.randomUUID(),"DIALOGUE_TEST");
    for(var n:level.getEntitiesOfClass(VillagerEntityMCA.class,p.getBoundingBox().inflate(12))){
     String key=n.entityTags().contains("pmca_suspicious")?"hostile":n.entityTags().contains("pmca_greedy")?"gruff":n.entityTags().contains("pmca_friendly")?"friendly":"normal";map.put(key,n.getUUID());n.setNoAi(true);n.setNoGravity(true);
     if(!key.equals("normal")){var m=CrewRuntime.prepareMember(level,n);ledger.transfer(m,crew);CrewRuntime.syncCrewTag(n,crew.id);m.resentment=0;var memory=PirateDialogues.memory(n);memory.lastSpeech=level.getGameTime();PirateDialogues.save(n,memory);}
    }check(map.size()==4,"DLG fixture actor count");ledger.captain(crew,p.getUUID(),true);
    var normal=npc(level,map,"normal");var oldTags=Set.copyOf(normal.entityTags());check(!PirateDialogues.eligible(normal),"DLG ordinary MCA accepted");check(!PirateDialogues.speak(normal,p,"greeting",0),"DLG leaked pirate speech");check(PirateDialogues.offer(normal,p,"piratesmca_hostile_mutiny_whisper",false)==null,"DLG leaked scene");check(oldTags.equals(normal.entityTags()),"DLG changed ordinary NPC tags");
    var h=npc(level,map,"hostile");check(!PirateDialogues.facts(h,p).contains(Signal.CONSPIRACY),"DLG fabricated conspiracy");
    for(String key:List.of("friendly","gruff")){var member=ledger.member(map.get(key));member.resentment=85;member.fear=10;member.ambition=95;}
    check(ledger.startConspiracy(crew,level.getGameTime()),"DLG real conspiracy failed to start");System.out.println("DLG_ISOLATION_PASS ordinary NPC unchanged; real conspiracy started");return map;
   });
   UUID token=null;for(int i=0;i<12&&token==null;i++){c.waitTicks(20);token=w.getServer().computeOnServer(s->PirateDialogues.pendingToken(player(s)));}
   check(token!=null,"DLG automatic conspiracy did not offer a response");final UUID conspiracyToken=token;
   for(String lang:List.of("en_us","pt_br")){
    locale(c,lang);w.getServer().runOnServer(s->check(PirateDialogues.open(player(s),conspiracyToken)==1,"DLG conspiracy prompt unavailable"));
    c.waitFor(mc->mc.gui.screen()!=null&&mc.gui.screen().getTitle().getString().equals(Component.translatable("piratesmca.talk.topics").getString()),100);
    c.runOnClient(mc->{String prompt=Component.translatable("dialogue.piratesmca_hostile_mutiny_whisper").getString();check(prompt.contains(lang.equals("pt_br")?"Algu\u00e9m a bordo":"Someone aboard"),"DLG locale not translated: "+prompt);System.out.println("DLG_LOCALE_PASS locale="+lang+" prompt="+prompt);});c.waitTicks(2);c.takeScreenshot("DLG-"+lang+"-automatic-conspiracy");c.setScreen(()->null);
   }
   w.getServer().runOnServer(s->{var p=player(s);check(PirateDialogues.open(p,UUID.randomUUID())==0,"DLG forged token accepted");check(PirateDialogues.answer(p,conspiracyToken,"ask_proof")==1,"DLG conspiracy reply failed");check(PirateDialogues.answer(p,conspiracyToken,"ask_proof")==0,"DLG conspiracy replay accepted");var ledger=CrewRuntime.ledger(s.overworld());var crew=ledger.crew(ledger.member(ids.get("hostile")).crew);ledger.depose(crew,s.overworld().getGameTime());crew.phase=CrewLedger.Phase.CALM;for(UUID id:crew.members)ledger.member(id).resentment=0;System.out.println("DLG_AUTO_CONSPIRACY_PASS real phase; response once; forged/replayed tokens rejected");});
   scene(c,w,ids,"gruff","piratesmca_gruff_captain_test","crew_first");
   w.getServer().runOnServer(s->{var ledger=CrewRuntime.ledger(s.overworld());var g=npc(s.overworld(),ids,"gruff");var crew=ledger.crew(ledger.member(g.getUUID()).crew);check(crew.captain==null,"DLG response granted captaincy");check(ledger.existingRelationship(g.getUUID(),player(s).getUUID()).trust==51,"DLG positive social feedback missing");ledger.captain(crew,player(s).getUUID(),true);
    var flag=new BlockPos(2048,-60,2);s.overworld().setBlock(flag,PirateFlag.BLOCK.defaultBlockState(),3);check(ledger.bindFlag(crew,player(s).getUUID(),PirateFlag.anchor(s.overworld(),flag)),"DLG flag fixture");var barrel=new BlockPos(2049,-60,1);s.overworld().setBlock(barrel,Blocks.BARREL.defaultBlockState(),3);((net.minecraft.world.Container)s.overworld().getBlockEntity(barrel)).setItem(0,new ItemStack(Items.GOLD_INGOT));check(player(s).gameMode.destroyBlock(barrel),"DLG actual container break failed");
   });
   scene(c,w,ids,"gruff","piratesmca_gruff_missing_crate","blame_crew");
   w.getServer().runOnServer(s->{var g=npc(s.overworld(),ids,"gruff");var boat=EntityTypes.OAK_BOAT.create(s.overworld(),EntitySpawnReason.COMMAND);boat.setPos(g.getX(),-60,g.getZ());boat.setNoGravity(true);s.overworld().addFreshEntity(boat);check(g.startRiding(boat,true,true),"DLG boat mount");check(boat.hurtServer(s.overworld(),s.overworld().damageSources().generic(),1),"DLG actual boat damage failed");check(PirateDialogues.memory(g).story.equals(Signal.DAMAGED_HULL.name()),"DLG boat event was not captured");});
   scene(c,w,ids,"gruff","piratesmca_gruff_damaged_hull","ask_time");
   w.getServer().runOnServer(s->{var level=s.overworld();var g=npc(level,ids,"gruff");var boat=g.getVehicle();g.stopRiding();boat.discard();g.setPos(2050,-60,0);var f=npc(level,ids,"friendly");f.getVillagerBrain().getMemoriesForPlayer(player(s)).setHearts(0);player(s).setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.DIAMOND,2));check(f.getInteractions().handle(player(s),"gift"),"DLG native MCA gift failed");check(PirateDialogues.memory(f).story.equals(Signal.TREASURE.name()),"DLG gift hook did not observe acceptance");System.out.println("DLG_NATIVE_GIFT_PASS MCA gift mechanics retained");});
   scene(c,w,ids,"friendly","piratesmca_friendly_share_the_find","keep_safe");
   w.getServer().runOnServer(s->{var f=npc(s.overworld(),ids,"friendly");f.getInteractions().handle(player(s),"trade");check(player(s).containerMenu instanceof net.minecraft.world.inventory.MerchantMenu&&!f.getOffers().isEmpty(),"DLG native profession trade changed");player(s).closeContainer();System.out.println("DLG_NATIVE_TRADE_PASS pirate profession shop unchanged");});
   w.getServer().runOnServer(s->{var f=npc(s.overworld(),ids,"friendly");var boat=EntityTypes.OAK_BOAT.create(s.overworld(),EntitySpawnReason.COMMAND);boat.setPos(f.getX(),-60,f.getZ());boat.setNoGravity(true);s.overworld().addFreshEntity(boat);check(f.startRiding(boat,true,true),"DLG voyage boat mount");CrewRuntime.ledger(s.overworld()).order(CrewRuntime.ledger(s.overworld()).member(f.getUUID()),CrewLedger.Order.FOLLOW,s.overworld().getGameTime());});
   scene(c,w,ids,"friendly","piratesmca_friendly_voyage_invitation","not_today");
   w.getServer().runCommand("forceload add 2074 0");
   w.getServer().runOnServer(s->{var f=npc(s.overworld(),ids,"friendly");var boat=f.getVehicle();f.stopRiding();boat.discard();f.setPos(2046,-60,0);var g=npc(s.overworld(),ids,"gruff");s.overworld().setBlock(new BlockPos(2074,-60,0),Blocks.WATER.defaultBlockState(),3);s.overworld().setBlock(new BlockPos(2074,-59,0),Blocks.WATER.defaultBlockState(),3);g.setPos(2074.5,-60,0.5);player(s).setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);});c.waitTicks(20);
   scene(c,w,ids,"friendly","piratesmca_friendly_lost_sailor","ask_details");
   w.getServer().runOnServer(s->{var level=s.overworld();npc(level,ids,"gruff").setPos(2050,-60,0);var ledger=CrewRuntime.ledger(level);var crew=ledger.crew(ledger.member(ids.get("hostile")).crew);ledger.captain(crew,ids.get("friendly"),false);});
   scene(c,w,ids,"hostile","piratesmca_hostile_deck_trespass","apologize");
   w.getServer().runOnServer(s->{var level=s.overworld();var ledger=CrewRuntime.ledger(level);var rival=ledger.getOrCreate(UUID.randomUUID(),"DIALOGUE_RIVAL");ledger.captain(rival,player(s).getUUID(),true);var pos=new BlockPos(2055,-60,1);level.setBlock(pos,PirateFlag.BLOCK.defaultBlockState(),3);check(ledger.bindFlag(rival,player(s).getUUID(),PirateFlag.anchor(level,pos)),"DLG rival flag fixture");});
   scene(c,w,ids,"hostile","piratesmca_hostile_rival_flag","offer_diplomacy");
   w.getServer().runOnServer(s->{var level=s.overworld();var h=npc(level,ids,"hostile");ready(h);var t=PirateDialogues.offer(h,player(s),"piratesmca_hostile_rival_flag",false);check(t!=null,"DLG range fixture");player(s).setPos(2100,-60,-3);check(PirateDialogues.answer(player(s),t,"fight_together")==0,"DLG remote response accepted");
    var memory=PirateDialogues.memory(h);memory.spoken("test-persist",level.getGameTime());PirateDialogues.save(h,memory);
    var output=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess());h.saveWithoutId(output);var copy=net.conczin.mca.registry.EntitiesMCA.MALE_VILLAGER.create(level,EntitySpawnReason.LOAD);copy.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),output.buildResult()));check(PirateDialogues.memory(copy).recent.contains("test-persist"),"DLG native save/load lost history");check(!PirateDialogues.memory(copy).ready(level.getGameTime(),100),"DLG relog bypassed cooldown");System.out.println("DLG_PERSISTENCE_RANGE_PASS native entity save/load; distant choice rejected");
   });
   System.out.println("DLG_CLIENT_PASS locales=2 scenes=9 choices_schema=27 native_gift=true pirate_only=true replay_rejected=true");
  }
 }
}
