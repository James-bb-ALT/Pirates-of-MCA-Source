package dev.arandu.piratesmca;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;

/** Exercises the actual Punchy/Better Combat bridge; reflection keeps both mods optional in production. */
final class WeaponHandheldReview {
 private static Object call(String type,String method,Class<?>[] params,Object... args){
  try{return Class.forName(type).getMethod(method,params).invoke(null,args);}
  catch(ReflectiveOperationException e){throw new AssertionError(type+"."+method,e);}
 }
 private static long epoch(String method){return ((Number)call("punchy.client.state.AttackActionTracker",method,new Class<?>[0])).longValue();}
 private static void check(boolean b,String text){PirateClientGameTests.expect(b,text);}
 static void run(ClientGameTestContext c){
  for(String mod:List.of("punchy","bettercombat","entity_model_features","emf_compat_better_combat"))check(FabricLoader.getInstance().isModLoaded(mod),"Missing compatibility fixture "+mod);
  check((Boolean)call("punchy.config.PunchyConfig","isBetterCombatCompatEnabled",new Class<?>[0]),"Enable Punchy's bettercombatCompat option for this bridge test");
  try(var w=c.worldBuilder().create()){
   var victim=new java.util.concurrent.atomic.AtomicReference<UUID>();
   w.getServer().runOnServer(s->{
    var p=s.getPlayerList().getPlayers().getFirst();net.conczin.mca.server.world.data.PlayerSaveData.get(p).setEntityDataSet(true);
    var mob=EntityTypes.HUSK.create(s.overworld(),EntitySpawnReason.COMMAND);mob.setNoAi(true);mob.setPersistenceRequired();mob.setSilent(true);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);mob.setHealth(1024);mob.setPos(2048,-60,0);s.overworld().addFreshEntity(mob);victim.set(mob.getUUID());
   });
   c.setScreen(()->null);w.getServer().runCommand("gamemode creative @a");w.getServer().runCommand("time set 6000");w.getServer().runCommand("tp @a 2048 -60 -2.8 0 0");c.waitTicks(80);
   // F1 hides the actual first-person hands, so this review must keep the HUD visible.
   c.runOnClient(mc->{check((Boolean)call("punchy.api.PunchyApi","isActive",new Class<?>[0]),"Punchy inactive");check((Boolean)call("punchy.compat.BetterCombatCompat","isAvailable",new Class<?>[0]),"Punchy/Better Combat bridge inactive");check(mc.getResourcePackRepository().getSelectedIds().contains("punchy:punchy"),"Punchy animation pack must remain selected after GameTest options reset");mc.options.setCameraType(CameraType.FIRST_PERSON);if(mc.gui.hud.isHidden())mc.gui.hud.toggle();System.out.println("WEAPON_PACKS "+mc.getResourcePackRepository().getSelectedIds());});
   int count=0;
   for(var item:PirateAtelier.WEAPONS){
    String id=PirateAtelier.ITEMS.get(item).id(),label=id.replace(':','_');
    c.runOnClient(mc->mc.options.keyAttack.setDown(false));
    w.getServer().runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item));p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);var target=s.overworld().getEntity(victim.get());target.setPos(2048,-60,0);target.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);PirateClientGameTests.commandAsOp(s,"tp @s 2048.0 -60 -2.8 0 0");});
    c.waitFor(mc->mc.player.getMainHandItem().is(item),100);c.waitTicks(20);
    c.runOnClient(mc->{
     var stack=mc.player.getMainHandItem();check((Boolean)call("punchy.compat.BetterCombatCompat","isWeaponStack",new Class<?>[]{ItemStack.class},stack),"Punchy cannot classify "+id);
     for(var display:List.of(ItemDisplayContext.GUI,ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,ItemDisplayContext.FIRST_PERSON_LEFT_HAND,ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,ItemDisplayContext.THIRD_PERSON_LEFT_HAND,ItemDisplayContext.GROUND,ItemDisplayContext.FIXED)){
      var state=new ItemStackRenderState();mc.getItemModelResolver().updateForLiving(state,stack,display,mc.player);check(!state.isEmpty(),"Empty model "+id+"/"+display);check(!state.usesBlockLight(),"Voxel block lighting returned "+id);
      if(display==ItemDisplayContext.GUI){var box=state.getModelBoundingBox();double depth=box.maxZ-box.minZ;check(depth>0&&depth<=.063,"Expected vanilla one-pixel extrusion: "+id+" depth="+depth);System.out.println("WEAPON_MODEL_PASS id="+id+" depth="+depth+" contexts=7 punchy_weapon=true");}
     }
    });
    c.takeScreenshot("WEAPON-"+label+"-firstperson-idle");
    var hitBefore=new AtomicLong();var visualBefore=new AtomicLong();
    c.runOnClient(mc->{hitBefore.set(epoch("peekEpoch"));visualBefore.set(epoch("peekVisualEpoch"));mc.options.keyAttack.setDown(true);KeyMapping.click(mc.options.keyAttack.getDefaultKey());});
    c.waitFor(mc->TricornePoseReview.combatProgress(mc)>.01&&TricornePoseReview.combatProgress(mc)<.95,160);
    var probes=new java.util.concurrent.atomic.AtomicInteger();
    c.waitFor(mc->{long hit=epoch("peekEpoch"),visual=epoch("peekVisualEpoch");if(probes.getAndIncrement()%20==0)System.out.println("WEAPON_ANIM_PROBE id="+id+" hit="+hit+"/"+hitBefore.get()+" visual="+visual+"/"+visualBefore.get());return hit>hitBefore.get()&&visual>visualBefore.get();},160);
    for(int frame=0;frame<3;frame++){c.takeScreenshot("WEAPON-"+label+"-firstperson-attack-"+frame);c.waitTicks(2);}
    c.runOnClient(mc->{mc.options.keyAttack.setDown(false);System.out.println("WEAPON_ATTACK_PASS id="+id+" combat_progress="+TricornePoseReview.combatProgress(mc)+" punchy_hit_epoch="+epoch("peekEpoch")+" punchy_visual_epoch="+epoch("peekVisualEpoch"));mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);});
    c.waitTicks(4);c.takeScreenshot("WEAPON-"+label+"-thirdperson");c.runOnClient(mc->mc.options.setCameraType(CameraType.FIRST_PERSON));count++;
   }
   check(count==10,"Weapon coverage changed");
   System.out.println("WEAPON_HANDHELD_PASS weapons="+count+" contexts=70 attack_series=10 screenshots=50");
  }
 }
}
