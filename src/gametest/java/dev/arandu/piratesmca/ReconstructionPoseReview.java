package dev.arandu.piratesmca;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

/** Temporal pose fixtures. These exercise renderer state, not autonomous NPC gameplay. */
final class ReconstructionPoseReview {
 static void run(ClientGameTestContext c,TestSingleplayerContext w,UUID id){
  String stage=System.getProperty("piratesmca.reconstructionReview");var phase=new AtomicReference<>("idle");var server=w.getServer().computeOnServer(s->s);var seats=new ArrayList<Entity>();
  ServerTickEvents.END_SERVER_TICK.register(s->{if(s!=server||!(s.overworld().getEntity(id) instanceof VillagerEntityMCA n))return;String p=phase.get();
   if(p.equals("walk")||p.equals("sprint")){n.setPos(2048+Math.sin(s.overworld().getGameTime()*(p.equals("sprint")?.3:.15))*.7,-60,0);n.setSprinting(p.equals("sprint"));}
   if(p.equals("jump")){n.setPos(2048,-60+Math.abs(Math.sin(s.overworld().getGameTime()*.15))*.9,0);n.setOnGround(false);}
   if((p.equals("attack")||p.equals("mine"))&&s.overworld().getGameTime()%6==0)n.swing(InteractionHand.MAIN_HAND,true);
   if(p.equals("swim")||p.equals("crawl")){n.setPose(Pose.SWIMMING);n.setSwimming(p.equals("swim"));}
   n.setYRot(180);n.setYHeadRot(p.equals("head_turn")?150:180);
  });
  var poses=List.of("idle","walk","sprint","crouch","jump","swim","crawl","boat","horse","attack","mine","eat","block","bow","head_turn","sleep");
  for(String p:poses){phase.set("idle");
   w.getServer().runOnServer(s->{var level=s.overworld();var n=(VillagerEntityMCA)level.getEntity(id);n.stopRiding();if(n.isSleeping())n.stopSleeping();n.stopUsingItem();for(var seat:seats)seat.discard();seats.clear();
    s.getCommands().performPrefixedCommand(s.createCommandSourceStack(),"fill 2045 -60 -3 2051 -57 3 air");
    n.setNoGravity(true);n.setDeltaMovement(Vec3.ZERO);n.setPos(2048,-60,0);n.setSprinting(false);n.setSwimming(false);n.setShiftKeyDown(false);n.setPose(Pose.STANDING);n.removeAllEffects();
    for(var slot:ReconstructionReview.SLOTS.entrySet())n.setItemSlot(slot.getKey(),AtelierClientExtras.stack("pirateatelier:fisherman_teal_pristine_peg_left_"+slot.getValue()));
    n.setItemSlot(EquipmentSlot.MAINHAND,AtelierClientExtras.stack("pirateatelier:naval_cutlass"));n.setItemSlot(EquipmentSlot.OFFHAND,AtelierClientExtras.stack("pirateatelier:atelier_hook_iron"));
    if(p.equals("crouch")){n.setShiftKeyDown(true);n.setPose(Pose.CROUCHING);}
    if(p.equals("swim"))for(int y=-60;y<=-59;y++)level.setBlock(new BlockPos(2048,y,0),Blocks.WATER.defaultBlockState(),2);
    if(p.equals("boat")||p.equals("horse")){var seat=(p.equals("boat")?EntityTypes.OAK_BOAT:EntityTypes.HORSE).create(level,EntitySpawnReason.COMMAND);seat.setPos(2048,-60,0);seat.setNoGravity(true);if(seat instanceof Mob mob)mob.setNoAi(true);if(seat instanceof net.minecraft.world.entity.animal.equine.AbstractHorse horse)horse.setTamed(true);level.addFreshEntity(seat);seats.add(seat);PirateClientGameTests.expect(n.startRiding(seat,true,true),"Could not mount "+p);}
    if(p.equals("mine"))n.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_PICKAXE));
    if(p.equals("eat")){n.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.APPLE));n.startUsingItem(InteractionHand.MAIN_HAND);}
    if(p.equals("block")){n.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.SHIELD));n.startUsingItem(InteractionHand.MAIN_HAND);}
    if(p.equals("bow")){n.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));n.startUsingItem(InteractionHand.MAIN_HAND);}
    if(p.equals("sleep")){var foot=new BlockPos(2048,-60,0);var bed=Blocks.BED.pick(DyeColor.RED).defaultBlockState().setValue(BedBlock.FACING,Direction.SOUTH);level.setBlock(foot,bed.setValue(BedBlock.PART,BedPart.FOOT),2);level.setBlock(foot.south(),bed.setValue(BedBlock.PART,BedPart.HEAD),2);n.startSleeping(foot.south());}
    phase.set(p);
   });
   String camera=p.equals("sleep")?"2045 -58.5 -2 -49 30":p.equals("swim")||p.equals("crawl")?"2048 -57.0 -3.5 0 25":"2045.8 -60.0 -3.3 -34 0";
   w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+camera));c.waitTicks(10);
   for(int frame=0;frame<3;frame++){
    c.runOnClient(mc->{for(var entity:mc.level.entitiesForRendering())if(entity instanceof VillagerEntityMCA n&&n.getUUID().equals(id)){
     if(p.equals("walk")||p.equals("sprint"))PirateClientGameTests.expect(n.walkAnimation.speed()>.01,"Walking fixture inactive");
     if(p.equals("boat")||p.equals("horse"))PirateClientGameTests.expect(n.isPassenger(),"Seat fixture inactive");
     if(p.equals("sleep"))PirateClientGameTests.expect(n.isSleeping(),"Sleep fixture inactive");
     if(p.equals("crouch"))PirateClientGameTests.expect(n.isCrouching(),"Crouch fixture inactive");
     if(p.equals("swim")||p.equals("crawl"))PirateClientGameTests.expect(n.getPose()==Pose.SWIMMING,"Swimming/crawling fixture inactive");
     if(List.of("eat","block","bow").contains(p))PirateClientGameTests.expect(n.isUsingItem(),"Use fixture inactive: "+p);
     System.out.println("RECON_TEMPORAL pose="+p+" age="+n.tickCount+" walk="+n.walkAnimation.speed()+" attack="+n.attackAnim+" using="+n.isUsingItem()+" passenger="+n.isPassenger()+" sleeping="+n.isSleeping());
    }});
    c.takeScreenshot("RECON-"+stage+"-POSE-"+p+"-frame-"+frame);c.waitTicks(3);
   }
  }
  phase.set("idle");System.out.println("RECON_TEMPORAL_PASS poses="+poses.size()+" frames="+(poses.size()*3)+" emote=BLOCKED_no_verified_runtime_trigger");
 }
}
