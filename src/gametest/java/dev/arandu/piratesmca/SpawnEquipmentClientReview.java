package dev.arandu.piratesmca;
import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
final class SpawnEquipmentClientReview {
 static List<EquipmentSlot> tracked(){return List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND);}
 record Loadout(PirateClientGameTests.Identity identity,List<String> armor){}
 static List<Loadout> state(MinecraftServer s){return PirateClientGameTests.identities(s).stream().map(a->{var n=(VillagerEntityMCA)s.overworld().getEntity(a.id());return new Loadout(a,tracked().stream().map(slot->n.getItemBySlot(slot).toString()).toList());}).toList();}
 static void verifyArmor(ClientGameTestContext c,List<Loadout> expected){
  c.waitFor(mc->expected.stream().allMatch(a->{for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && n.getUUID().equals(a.identity().id()))return a.armor().equals(tracked().stream().map(slot->n.getItemBySlot(slot).toString()).toList());return false;}),200);
  System.out.println("QA_SPAWN_EQUIPMENT_CLIENT_SYNC count="+expected.size());
 }
 void run(ClientGameTestContext c){
  TestWorldSave save;List<Loadout> before;
  try(var w=c.worldBuilder().create()){
   save=w.getWorldSave();w.getServer().runCommand("tp @a 2048.0 -60.0 -7.0 0 0");w.getServer().runCommand("time set 6000");
   for(int i=0;i<12;i++){String gender=i%2==0?"male":"female";w.getServer().runCommand("summon mca:"+gender+"_villager "+(2043+(i%6)*2)+" -60 "+(i/6*3)+" {Age:0,NoAI:1b,Rotation:[180f,0f],Tags:[\"pmca_seapost\",\"pmca_friendly\",\"pmca_adult_required\",\"pmca_gender_"+gender+"\"]}");}
   c.waitTicks(100);c.setScreen(()->null);if(Boolean.getBoolean("piratesmca.atelierReview"))AtelierClientExtras.run(c,w);before=w.getServer().computeOnServer(SpawnEquipmentClientReview::state);PirateClientGameTests.expect(before.size()==12,"SPAWN-EQ-06 expected 12 pirates");
   w.getServer().runOnServer(s->{var pools=PirateSpawnEquipment.pools();for(var a:before){var n=(VillagerEntityMCA)s.overworld().getEntity(a.identity().id());PirateClientGameTests.expect(n.getClothes().startsWith("piratesmca:") && n.entityTags().contains(PirateSpawnEquipment.DONE),"SPAWN-EQ-06 default clothes missing");for(var slot:PirateSpawnEquipment.SLOTS){var stack=n.getItemBySlot(slot);PirateClientGameTests.expect(stack.isEmpty() || pools.get(slot).contains(stack.getItem()),"SPAWN-EQ-06 unexpected guard armor");}}});
   if(Boolean.getBoolean("piratesmca.emfExtensionsReview"))c.runOnClient(mc->{var loader=net.fabricmc.loader.api.FabricLoader.getInstance();for(String id:List.of("entity_model_features","entity_texture_features","emf_compat_core","emf_compat_better_combat","bettercombat","player_animation_library"))PirateClientGameTests.expect(loader.isModLoaded(id),"Missing mod "+id);var packs=mc.getResourceManager().listPacks().map(p->p.packId()).toList();for(String id:List.of("FreshAnimations_v1.11.2_26.1+.zip","FA+All_Extensions-v1.10.1.zip","FA+Player-v1.1.zip"))PirateClientGameTests.expect(packs.stream().anyMatch(p->p.contains(id)),"Missing pack "+id);System.out.println("QA_SPAWN_EQUIPMENT_PACKS "+packs);});
   PirateClientGameTests.verifyClient(c,before.stream().map(Loadout::identity).toList(),"SPAWN-EQ-initial");verifyArmor(c,before);
   c.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();});
   for(String view:List.of("front","side","back")){String camera=switch(view){case "side"->"2039.0 -58.0 0.0 -80 10";case "back"->"2048.0 -58.0 8.0 180 10";default->"2048.0 -58.0 -7.0 0 10";};w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+camera));c.waitTicks(15);c.takeScreenshot("SPAWN-EQ-"+view);}
   if(Boolean.getBoolean("piratesmca.atelierReview")){
    var traveler=before.getFirst();var pos=w.getServer().computeOnServer(s->{var n=s.overworld().getEntity(traveler.identity().id());return new double[]{n.getX(),n.getY(),n.getZ()};});
    w.getServer().runCommand("execute in minecraft:the_nether run forceload add 0 0");c.waitTicks(30);
    w.getServer().runCommand("execute in minecraft:the_nether run tp "+traveler.identity().id()+" 0.5 100.0 0.5");c.waitTicks(30);
    w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.getLevel(net.minecraft.world.level.Level.NETHER).getEntity(traveler.identity().id());PirateClientGameTests.expect(n!=null && n.getSkin().equals(traveler.identity().skin()) && tracked().stream().map(slot->n.getItemBySlot(slot).toString()).toList().equals(traveler.armor()),"ATELIER-14 dimension lost skin or equipment");});
    w.getServer().runCommand("execute in minecraft:overworld run tp "+traveler.identity().id()+" "+pos[0]+" "+pos[1]+" "+pos[2]);c.waitTicks(30);
    PirateClientGameTests.expect(before.equals(w.getServer().computeOnServer(SpawnEquipmentClientReview::state)),"ATELIER-14 dimension changed identity");System.out.println("QA_ATELIER_DIMENSION_PASS slots=6");
   }
   w.getServer().runCommand("tp @a 4096.0 -60.0 4096.0");c.waitTicks(400);
   w.getServer().runOnServer(s->{for(var a:before)PirateClientGameTests.expect(s.overworld().getEntity(a.identity().id())==null,"SPAWN-EQ-07 chunks did not unload");});w.getServer().runCommand("tp @a 2048.0 -60.0 -7.0 0 0");c.waitTicks(100);
   PirateClientGameTests.expect(before.equals(w.getServer().computeOnServer(SpawnEquipmentClientReview::state)),"SPAWN-EQ-07 loadout rerolled after chunk reload");System.out.println("QA_SPAWN_EQUIPMENT_CHUNK_PASS count=12");
  }
  try(var w=save.open()){c.waitTicks(100);PirateClientGameTests.expect(before.equals(w.getServer().computeOnServer(SpawnEquipmentClientReview::state)),"SPAWN-EQ-08 saved equipment changed");PirateClientGameTests.verifyClient(c,before.stream().map(Loadout::identity).toList(),"SPAWN-EQ-reopen");verifyArmor(c,before);c.setScreen(()->null);c.takeScreenshot("SPAWN-EQ-reopen");verifyArmor(c,before);System.out.println("QA_SPAWN_EQUIPMENT_REOPEN_PASS count=12");}
 }
}
