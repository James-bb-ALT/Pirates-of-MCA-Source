package dev.arandu.piratesmca;

import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.client.render.VillagerVisuals;
import net.conczin.mca.client.render.layer.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Trace the actual resolved layers before and after the reconstruction, without desktop input. */
@SuppressWarnings({"rawtypes","unchecked"})
final class ReconstructionReview {
 static final Map<EquipmentSlot,String> SLOTS=Map.of(EquipmentSlot.HEAD,"helmet",EquipmentSlot.CHEST,"chestplate",EquipmentSlot.LEGS,"leggings",EquipmentSlot.FEET,"boots");
 static void run(ClientGameTestContext c,TestSingleplayerContext w,List<PirateClientGameTests.Identity> ids){
  String stage=System.getProperty("piratesmca.reconstructionReview");
  var male=ids.stream().filter(i->i.gender().equals("MALE")).findFirst().orElseThrow();
  var female=ids.stream().filter(i->i.gender().equals("FEMALE")).findFirst().orElseThrow();
  c.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();mc.options.particles().set(net.minecraft.server.level.ParticleStatus.MINIMAL);System.out.println("RECON_PACK_ORDER "+mc.getResourceManager().listPacks().map(p->p.packId()).toList());});
  if(Boolean.getBoolean("piratesmca.emfExtensionsReview"))c.runOnClient(mc->{
   var loader=net.fabricmc.loader.api.FabricLoader.getInstance();for(String mod:List.of("entity_model_features","entity_texture_features","emf_compat_core","emf_compat_better_combat","bettercombat"))PirateClientGameTests.expect(loader.isModLoaded(mod),"Missing A/B mod "+mod);
   var packs=mc.getResourceManager().listPacks().map(p->p.packId()).toList();for(String pack:List.of("FreshAnimations_v1.11.2_26.1+.zip","FA+All_Extensions-v1.10.1.zip"))PirateClientGameTests.expect(packs.stream().anyMatch(n->n.contains(pack)),"Missing A/B pack "+pack);
   boolean hasPlayer=packs.stream().anyMatch(n->n.contains("FA+Player-v1.1.zip"));PirateClientGameTests.expect(hasPlayer!=Boolean.getBoolean("piratesmca.disableFAPlayer"),"FA+Player A/B configuration mismatch");
   System.out.println("RECON_PACK_ASSERT_PASS FAPlayer="+hasPlayer);
  });
  w.getServer().runOnServer(s->{s.overworld().getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME,false,s);s.overworld().getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_WEATHER,false,s);});w.getServer().runCommand("time set 6000");
  w.getServer().runOnServer(s->{for(var id:ids){var n=(VillagerEntityMCA)s.overworld().getEntity(id.id());if(!id.id().equals(male.id())&&!id.id().equals(female.id())){n.discard();continue;}n.setPos(id.id().equals(male.id())?2048:2070,-60,0);n.setNoAi(true);for(var slot:EquipmentSlot.values())n.setItemSlot(slot,ItemStack.EMPTY);}});
  if(!stage.equals("before")){
   var control=w.getServer().computeOnServer(s->{var z=net.minecraft.world.entity.EntityTypes.ZOMBIE.create(s.overworld(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);z.setPos(2048,-60,20);z.setNoAi(true);z.setInvulnerable(true);z.setItemSlot(EquipmentSlot.CHEST,AtelierClientExtras.stack("pirateatelier:captain_armor_pristine_normal_chestplate"));s.overworld().addFreshEntity(z);return z.getUUID();});
   c.waitFor(mc->{for(var e:mc.level.entitiesForRendering())if(e.getUUID().equals(control))return true;return false;},200);
   c.runOnClient(mc->{for(var e:mc.level.entitiesForRendering())if(e.getUUID().equals(control)){
    var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(e);var state=(net.minecraft.client.renderer.entity.state.HumanoidRenderState)renderer.createRenderState();renderer.extractRenderState((net.minecraft.world.entity.LivingEntity)e,state,0);
    PirateClientGameTests.expect(!((dev.arandu.piratesmca.client.PirateLimbsData)state).piratesmca$wardrobeEnabled(),"Wardrobe masked an unsupported mob");System.out.println("RECON_NATIVE_ARMOR_FALLBACK_PASS mob=zombie");
   }});w.getServer().runOnServer(s->s.overworld().getEntity(control).discard());c.waitTicks(2);
  }
  var skins=stage.equals("before")?List.of("boatswain","deckhand_f"):Arrays.stream(PirateAtelier.SKINS).map(PirateAtelier.Skin::name).toList();
  for(String skin:skins){
   var selectedSkin=Arrays.stream(PirateAtelier.SKINS).filter(v->v.name().equals(skin)).findFirst().orElseThrow();
   var chosen=selectedSkin.gender().equals("female")?female:male;
   w.getServer().runOnServer(s->{for(var id:List.of(male,female))((VillagerEntityMCA)s.overworld().getEntity(id.id())).setPos(id.id().equals(chosen.id())?2048:2070,-60,0);var n=(VillagerEntityMCA)s.overworld().getEntity(chosen.id());n.setSkin("pirateatelier:textures/skins/"+skin+".png");});
   c.waitTicks(10);trace(c,chosen.id(),skin);views(c,w,"RECON-"+stage+"-skin-"+skin);
   w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s 2048.0 -60.1 -1.1 0 0"));c.waitTicks(8);c.takeScreenshot("RECON-"+stage+"-skin-"+skin+"-head");
  }
  w.getServer().runOnServer(s->{((VillagerEntityMCA)s.overworld().getEntity(female.id())).setPos(2070,-60,0);var n=(VillagerEntityMCA)s.overworld().getEntity(male.id());n.setPos(2048,-60,0);n.setSkin("pirateatelier:textures/skins/boatswain.png");});
  for(String set:List.of("pirateatelier:captain_armor_pristine_normal","pirateatelier:fisherman_teal_pristine_peg_left","lfpirates:corsair_tier_1","lfpirates:skull_1")){
   w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(male.id());for(var slot:SLOTS.entrySet())n.setItemSlot(slot.getKey(),AtelierClientExtras.stack(set+"_"+slot.getValue()));});
   c.waitTicks(10);trace(c,male.id(),set);views(c,w,"RECON-"+stage+"-armor-"+set.replace(':','_'));
  }
  System.out.println("RECON_CAPTURE_PASS stage="+stage+" skins="+skins.size()+" armor_configurations=4 screenshots="+(skins.size()*4+12));
  if(!stage.equals("before"))ReconstructionPoseReview.run(c,w,male.id());
  if(Boolean.getBoolean("piratesmca.emfExtensionsReview"))ReconstructionAvatarReview.run(c,w);
 }
 static void views(ClientGameTestContext c,TestSingleplayerContext w,String label){for(String view:List.of("front","side","back")){
  String tp=switch(view){case "side"->"2045.0 -60.4 0.0 -90 5";case "back"->"2048.0 -60.4 3.0 180 5";default->"2048.0 -60.4 -3.0 0 5";};
  w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+tp));c.waitTicks(8);c.takeScreenshot(label+"-"+view);
 }}
 static void trace(ClientGameTestContext c,UUID id,String label){c.runOnClient(mc->{
  for(var entity:mc.level.entitiesForRendering())if(entity instanceof VillagerEntityMCA n&&n.getUUID().equals(id)){
   var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);
   var state=(net.minecraft.client.renderer.entity.state.HumanoidRenderState)renderer.createRenderState();renderer.extractRenderState(n,state,0);
   var model=(net.minecraft.client.model.HumanoidModel)renderer.getModel();
   var skin=new SkinLayer(renderer,model);var clothes=new ClothingLayer(renderer,model,"normal");
   if(!System.getProperty("piratesmca.reconstructionReview").equals("before")&&PirateAtelier.extraSkin(n.getSkin())){
    PirateClientGameTests.expect(dev.arandu.piratesmca.client.PirateSkinLayers.body(n.getSkin()).equals(skin.getSkin(state)),"Layered body skin texture binding mismatch");
    PirateClientGameTests.expect(clothes.getSkin(state)==null,"Default profession clothing still covers full skin");
   }
   System.out.println("RECON_TRACE label="+label+" renderer="+renderer.getClass().getName()+" model="+model.getClass().getName()+" visuals="+VillagerVisuals.require(state)+" skinTexture="+skin.getSkin(state)+" clothingTexture="+clothes.getSkin(state));
   for(var slot:SLOTS.keySet()){var stack=n.getItemBySlot(slot);var mesh=dev.arandu.piratesmca.client.WardrobeGeometry.get(stack);System.out.println("RECON_ITEM "+slot+" item="+net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())+" equipment="+stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE)+" geometry="+(mesh==null?null:mesh.resource())+" material="+(mesh==null?null:mesh.texture())+" parts="+(mesh==null?0:mesh.parts().size()));
    if(PirateAtelier.owns(stack.getItem())&&PirateAtelier.ITEMS.get(stack.getItem()).kind().equals("armor"))PirateClientGameTests.expect(mesh!=null,"No wardrobe geometry for "+stack);
   }
   for(var bone:List.of(model.head,model.body,model.leftArm,model.rightArm,model.leftLeg,model.rightLeg))System.out.println("RECON_BONE "+bone.getClass().getName()+" pivot="+bone.x+","+bone.y+","+bone.z+" rotation="+bone.xRot+","+bone.yRot+","+bone.zRot+" scale="+bone.xScale+","+bone.yScale+","+bone.zScale+" visible="+bone.visible);
   return;
  }throw new AssertionError("Missing reconstruction specimen "+id);
 });}
}
