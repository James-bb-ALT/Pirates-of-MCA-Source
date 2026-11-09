package dev.arandu.piratesmca;
import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;

/** Exercises real local-player inputs with the selected EMF pack, logging the effective model. */
@SuppressWarnings({"rawtypes","unchecked"})
final class ReconstructionAvatarReview {
 static void run(ClientGameTestContext c,TestSingleplayerContext w){
  String stage=System.getProperty("piratesmca.reconstructionReview");
  w.getServer().runCommand("gamemode survival @a");w.getServer().runCommand("give @a minecraft:arrow 64");
  var poses=List.of("idle","walk","sprint","crouch","jump","attack","eat","block","bow");
  for(String pose:poses){
   c.runOnClient(mc->{mc.options.keyUp.setDown(false);mc.options.keySprint.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyJump.setDown(false);mc.options.keyAttack.setDown(false);mc.options.keyUse.setDown(false);});
   w.getServer().runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.removeAllEffects();p.setInvisible(false);p.stopUsingItem();for(var slot:ReconstructionReview.SLOTS.entrySet())p.setItemSlot(slot.getKey(),AtelierClientExtras.stack("pirateatelier:captain_armor_pristine_"+(slot.getKey()==EquipmentSlot.LEGS?"peg_left":"normal")+"_"+slot.getValue()));
    p.setItemSlot(EquipmentSlot.OFFHAND,AtelierClientExtras.stack("pirateatelier:atelier_hook_brass"));
    var held=pose.equals("eat")?new ItemStack(Items.GOLDEN_APPLE):pose.equals("block")?new ItemStack(Items.SHIELD):pose.equals("bow")?new ItemStack(Items.BOW):AtelierClientExtras.stack("pirateatelier:naval_cutlass");p.setItemSlot(EquipmentSlot.MAINHAND,held);
    // The selected hotbar slot initially receives /give arrows; replacing MAINHAND
    // with the bow can consume that stack. Supply ammunition after equipping it.
    if(pose.equals("bow"))p.getInventory().add(new ItemStack(Items.ARROW,64));
    var chest=p.getItemBySlot(EquipmentSlot.CHEST);chest.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);
    var mat=s.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.TRIM_MATERIAL).getOrThrow(net.minecraft.world.item.equipment.trim.TrimMaterials.GOLD);
    var pat=s.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.TRIM_PATTERN).getOrThrow(net.minecraft.world.item.equipment.trim.TrimPatterns.COAST);
    chest.set(DataComponents.TRIM,new net.minecraft.world.item.equipment.trim.ArmorTrim(mat,pat));
    PirateClientGameTests.commandAsOp(s,"tp @s 2056 -60 -3 180 0");
   });c.waitTicks(5);
   c.runOnClient(mc->{mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();
    if(pose.equals("walk")||pose.equals("sprint"))mc.options.keyUp.setDown(true);
    if(pose.equals("sprint"))mc.options.keySprint.setDown(true);
    if(pose.equals("crouch"))mc.options.keyShift.setDown(true);
    if(pose.equals("jump"))mc.options.keyJump.setDown(true);
    if(pose.equals("attack")){mc.options.keyAttack.setDown(true);net.minecraft.client.KeyMapping.click(mc.options.keyAttack.getDefaultKey());}
    if(List.of("eat","block","bow").contains(pose)){mc.options.keyUse.setDown(true);net.minecraft.client.KeyMapping.click(mc.options.keyUse.getDefaultKey());}
   });c.waitTicks(8);
   if(pose.equals("attack"))c.waitFor(mc->TricornePoseReview.combatProgress(mc)>.01&&TricornePoseReview.combatProgress(mc)<.8,160);
   var jumped=new java.util.concurrent.atomic.AtomicBoolean();
   for(int frame=0;frame<3;frame++){
    c.runOnClient(mc->{var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(mc.player);var state=(net.minecraft.client.renderer.entity.state.AvatarRenderState)renderer.createRenderState();renderer.extractRenderState(mc.player,state,0);var model=(net.minecraft.client.model.HumanoidModel)renderer.getModel();
     PirateClientGameTests.expect(state.skin.model().name().equals(stage.contains("slim")?"SLIM":"WIDE"),"Unexpected player model for A/B "+state.skin.model());
     PirateClientGameTests.expect(dev.arandu.piratesmca.client.WardrobeGeometry.get(state.chestEquipment)!=null,"Avatar wardrobe missing");
     if(pose.equals("walk")||pose.equals("sprint"))PirateClientGameTests.expect(mc.player.walkAnimation.speed()>.01,"Avatar movement inactive");
     if(pose.equals("sprint"))PirateClientGameTests.expect(mc.player.isSprinting(),"Avatar sprint inactive");
     if(pose.equals("crouch"))PirateClientGameTests.expect(mc.player.isCrouching(),"Avatar crouch inactive");
     if(pose.equals("jump")&&!mc.player.onGround())jumped.set(true);
     if(List.of("eat","block","bow").contains(pose))PirateClientGameTests.expect(mc.player.isUsingItem(),"Avatar use action inactive: "+pose);
     System.out.println("RECON_AVATAR pose="+pose+" model="+state.skin.model()+" root="+model.root().getClass().getName()+" leg="+model.leftLeg.getClass().getName()+" walk="+mc.player.walkAnimation.speed()+" using="+mc.player.isUsingItem()+" packNoPlayer="+Boolean.getBoolean("piratesmca.disableFAPlayer"));
    });c.takeScreenshot("RECON-"+stage+"-AVATAR-"+pose+"-frame-"+frame);c.waitTicks(3);
   }
   if(pose.equals("jump"))PirateClientGameTests.expect(jumped.get(),"Avatar jump never left ground");
  }
  c.runOnClient(mc->{mc.options.keyUp.setDown(false);mc.options.keySprint.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyJump.setDown(false);mc.options.keyAttack.setDown(false);mc.options.keyUse.setDown(false);mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);});
  System.out.println("RECON_AVATAR_PASS poses="+poses.size()+" frames="+(poses.size()*3));
 }
}
