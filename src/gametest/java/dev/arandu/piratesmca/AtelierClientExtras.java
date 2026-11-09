package dev.arandu.piratesmca;
import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
final class AtelierClientExtras {
 static ItemStack stack(String id){return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));}
 static void run(ClientGameTestContext c,TestSingleplayerContext w){
  c.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();mc.options.particles().set(net.minecraft.server.level.ParticleStatus.MINIMAL);});
  c.runOnClient(mc->{var resolver=new net.minecraft.client.renderer.item.ItemModelResolver(mc.getModelManager());int count=0;for(var item:PirateAtelier.ITEMS.keySet())for(var context:List.of(ItemDisplayContext.GUI,ItemDisplayContext.FIXED)){
   var state=new net.minecraft.client.renderer.item.ItemStackRenderState();resolver.updateForLiving(state,new ItemStack(item),context,mc.player);
   PirateClientGameTests.expect(!state.isEmpty(),"ATELIER-06 empty GUI model "+BuiltInRegistries.ITEM.getKey(item));var material=state.pickParticleMaterial(net.minecraft.util.RandomSource.create(1));
   PirateClientGameTests.expect(material!=null && !material.sprite().contents().name().toString().contains("missingno"),"ATELIER-06 missing sprite "+BuiltInRegistries.ITEM.getKey(item));count++;
  }
   for(var pair:PirateAtelier.ITEMS.entrySet())if(pair.getValue().kind().equals("hook")){
    var id=Identifier.parse(pair.getValue().id());var shown=new ItemStack(pair.getKey());shown.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,Identifier.fromNamespaceAndPath(id.getNamespace(),"attachments/"+id.getPath()+"_left"));var state=new net.minecraft.client.renderer.item.ItemStackRenderState();resolver.updateForLiving(state,shown,ItemDisplayContext.FIXED,mc.player);var material=state.pickParticleMaterial(net.minecraft.util.RandomSource.create(1));PirateClientGameTests.expect(material!=null && !material.sprite().contents().name().toString().contains("missingno"),"ATELIER-11 left hook atlas missing");count++;
   }
   for(var pair:PirateAtelier.ITEMS.entrySet())if(List.of("hook","peg").contains(pair.getValue().kind())){
    var id=Identifier.parse(pair.getValue().id());var shown=new ItemStack(pair.getKey());shown.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,Identifier.fromNamespaceAndPath(id.getNamespace(),"attachments/"+id.getPath()));var state=new net.minecraft.client.renderer.item.ItemStackRenderState();resolver.updateForLiving(state,shown,ItemDisplayContext.FIXED,mc.player);var material=state.pickParticleMaterial(net.minecraft.util.RandomSource.create(1));PirateClientGameTests.expect(material!=null && !material.sprite().contents().name().toString().contains("missingno"),"ATELIER-11 dedicated attachment atlas missing");count++;
   }
   System.out.println("QA_ATELIER_GUI_FIXED_MODELS count="+count);});
  var ids=w.getServer().computeOnServer(s->PirateClientGameTests.identities(s).stream().map(a->a.id()).toList());
  w.getServer().runOnServer(s->{for(int i=0;i<ids.size();i++){
   var n=(VillagerEntityMCA)s.overworld().getEntity(ids.get(i));boolean female=n.getGenetics().getGender()==net.conczin.mca.entity.ai.relationship.Gender.FEMALE;
   n.setSkin(PirateAtelier.SKINS[0].id());if(female)n.setSkin(Arrays.stream(PirateAtelier.SKINS).filter(t->t.gender().equals("female")).findFirst().orElseThrow().id());
   String base=i%3==0?"captain_armor_pristine_normal":i%3==1?"sailor_navy_weathered_peg_left":"pirate_crimson_tattered_peg_right";
   for(var pair:Map.of(EquipmentSlot.HEAD,"helmet",EquipmentSlot.CHEST,"chestplate",EquipmentSlot.LEGS,"leggings",EquipmentSlot.FEET,"boots").entrySet())n.setItemSlot(pair.getKey(),stack("pirateatelier:"+base+"_"+pair.getValue()));
   n.setItemSlot(EquipmentSlot.MAINHAND,stack(i%2==0?"pirateatelier:naval_cutlass":"pirateatelier:atelier_hook_iron"));n.setItemSlot(EquipmentSlot.OFFHAND,stack(i%2==0?"pirateatelier:atelier_hook_brass":"pirateatelier:officer_rapier"));if(i==2)n.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);if(i==3){n.setItemSlot(EquipmentSlot.MAINHAND,stack("pirateatelier:naval_cutlass"));n.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);}
  }});c.waitTicks(20);
  // Every skin rendered on its matching adult gender, then restore the fixture look.
  for(var skin:PirateAtelier.SKINS){
   var target=w.getServer().computeOnServer(s->ids.stream().filter(id->{var n=(VillagerEntityMCA)s.overworld().getEntity(id);return n.getGenetics().getGender().name().equalsIgnoreCase(skin.gender());}).findFirst().orElseThrow());
   var old=w.getServer().computeOnServer(s->((VillagerEntityMCA)s.overworld().getEntity(target)).getSkin());
   var pos=w.getServer().computeOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(target);n.setSkin(skin.id());n.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);return new double[]{n.getX(),n.getZ()};});
   w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+pos[0]+" -60.0 "+(pos[1]-2.0)+" 0 0"));c.waitTicks(10);
   c.runOnClient(mc->{for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && n.getUUID().equals(target))PirateClientGameTests.expect(n.getSkin().equals(skin.id()),"ATELIER-07 selected skin did not sync");});c.takeScreenshot("ATELIER-SKIN-"+skin.name());
   w.getServer().runOnServer(s->((VillagerEntityMCA)s.overworld().getEntity(target)).setSkin(old));
  }
  // Two amputated sides, ordinary controls, and 3D weapons in the same frame.
  w.getServer().runOnServer(s->{for(int i=0;i<ids.size();i++){var n=(VillagerEntityMCA)s.overworld().getEntity(ids.get(i));n.setPos(i<4?2042+i*4:2040+(i-4)*2,-60,i<4?0:10);}});
  for(int i=0;i<4;i++)for(String view:List.of("front","side","back")){
   double x=2042+i*4;String camera=switch(view){case "side"->(x-2.6)+" -60.4 0.0 -90 5";case "back"->x+" -60.4 2.6 180 5";default->x+" -60.4 -2.6 0 5";};
   w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+camera));c.waitTicks(10);c.takeScreenshot("ATELIER-FIT-"+i+"-"+view);
  }

  var specimen=ids.getFirst();var saved=w.getServer().computeOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(specimen);var m=new java.util.EnumMap<EquipmentSlot,ItemStack>(EquipmentSlot.class);for(var slot:EquipmentSlot.values())m.put(slot,n.getItemBySlot(slot).copy());return m;});
  for(var entry:PirateAtelier.ITEMS.entrySet())if(List.of("hook","peg").contains(entry.getValue().kind())){
   w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(specimen);n.setItemSlot(EquipmentSlot.LEGS,stack("pirateatelier:captain_armor_pristine_normal_leggings"));n.setItemSlot(EquipmentSlot.FEET,stack("pirateatelier:captain_armor_pristine_normal_boots"));n.setItemSlot(EquipmentSlot.MAINHAND,stack("pirateatelier:naval_cutlass"));n.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);n.setItemSlot(entry.getValue().kind().equals("hook")?EquipmentSlot.MAINHAND:EquipmentSlot.FEET,new ItemStack(entry.getKey()));if(entry.getValue().id().endsWith("peg_leg_wood"))n.setItemSlot(EquipmentSlot.LEGS,ItemStack.EMPTY);});
   w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s 2042.0 -60.4 -2.6 0 5"));c.waitTicks(10);c.takeScreenshot("ATELIER-PROSTH-"+entry.getValue().id().replace(':','_'));
  }
  w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(specimen);saved.forEach(n::setItemSlot);});c.waitTicks(10);
  System.out.println("QA_ATELIER_PROSTHESES count=17");
  var phase=new java.util.concurrent.atomic.AtomicReference<>("idle");var expectedServer=w.getServer().computeOnServer(s->s);
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(s->{if(s!=expectedServer)return;for(int i=0;i<4;i++)if(s.overworld().getEntity(ids.get(i)) instanceof VillagerEntityMCA n){
   if(phase.get().equals("walk"))n.setPos(2042+i*4+Math.sin(s.overworld().getGameTime()*.3)*.7,-60,0);
   if(phase.get().equals("swim")){n.setSwimming(true);n.setSprinting(true);n.setPose(Pose.SWIMMING);}
   if(phase.get().equals("attack") && s.overworld().getGameTime()%6==0)n.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
  }});
  for(String pose:List.of("idle","walk","attack","swim")){
   phase.set(pose);
   w.getServer().runOnServer(s->{for(int i=0;i<4;i++){var n=(VillagerEntityMCA)s.overworld().getEntity(ids.get(i));n.setNoGravity(true);n.setYRot(180);n.setYHeadRot(180);if(pose.equals("walk"))n.setPos(n.getX()+.6,n.getY(),n.getZ());if(pose.equals("attack"))n.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);if(pose.equals("swim")){s.overworld().setBlock(n.blockPosition(),net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(),2);n.setSprinting(true);n.setSwimming(true);n.setPose(Pose.SWIMMING);}}});
   w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s 2048.0 -59.5 -7.5 0 5"));c.waitTicks(pose.equals("attack")?2:15);c.takeScreenshot("ATELIER-LIMBS-"+pose);
   c.runOnClient(mc->{int count=0;for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && ids.subList(0,4).contains(n.getUUID())){
    var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);var state=(net.minecraft.client.renderer.entity.state.HumanoidRenderState)renderer.createRenderState();renderer.extractRenderState(n,state,0);
    var limbs=((dev.arandu.piratesmca.client.PirateLimbsData)state).piratesmca$limbs();int active=0;for(var part:limbs)if(!part.isEmpty())active++;if(pose.equals("walk"))PirateClientGameTests.expect(n.walkAnimation.speed()>.01,"ATELIER-08 walking pose not active");if(pose.equals("swim"))PirateClientGameTests.expect(n.getPose()==Pose.SWIMMING,"ATELIER-08 swimming pose not active");if(pose.equals("attack"))PirateClientGameTests.expect(n.attackAnim>0,"ATELIER-08 attack pose not active");boolean control=n.getUUID().equals(ids.get(3));PirateClientGameTests.expect(control?active==0:active>=1,"ATELIER-08 missing prosthetic/control state");
    var model=(net.minecraft.client.model.HumanoidModel)renderer.getModel();model.setupAnim(state);
    if(control)PirateClientGameTests.expect(model.leftArm.visible && model.rightArm.visible && model.leftLeg.visible && model.rightLeg.visible,"ATELIER-12 mask leaked to ordinary actor");
    else for(int part=0;part<4;part++){if(limbs[part].isEmpty())continue;var source=switch(part){case 0->model.leftArm;case 1->model.rightArm;case 2->model.leftLeg;default->model.rightLeg;};float cut=part<2?6:6.3F;source=dev.arandu.piratesmca.client.UpperLimbGeometry.visibleSource(source,state,new String[]{"left_arm","right_arm","left_leg","right_leg"}[part]);var clipped=dev.arandu.piratesmca.client.UpperLimbGeometry.clipped(source,cut,0);PirateClientGameTests.expect(!clipped.isEmpty(),"ATELIER-12 upper limb absent");for(var cube:clipped)for(var face:cube.polygons)for(var vertex:face.vertices())PirateClientGameTests.expect(vertex.y()<=cut+.0001,"ATELIER-12 upper limb exceeds cut");}
    count++;
    System.out.println("QA_ATELIER_LIMBS npc="+n.getUUID()+" pose="+pose+" active="+active);
   }PirateClientGameTests.expect(count==4,"ATELIER-08 missing actors");});
   phase.set("idle");w.getServer().runCommand("fill 2038 -60 -4 2058 -57 6 air");
   w.getServer().runOnServer(s->{for(int i=0;i<4;i++){var n=(VillagerEntityMCA)s.overworld().getEntity(ids.get(i));s.overworld().setBlock(n.blockPosition(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);n.setSprinting(false);n.setSwimming(false);n.setPose(Pose.STANDING);n.setPos(2042+i*4,-60,0);}});c.waitTicks(15);
  }
  if(Boolean.getBoolean("piratesmca.emfExtensionsReview")){
   w.getServer().runCommand("item replace entity @a armor.chest with pirateatelier:captain_armor_pristine_normal_chestplate");
   w.getServer().runCommand("item replace entity @a armor.legs with pirateatelier:pirate_crimson_pristine_peg_left_leggings");
   w.getServer().runCommand("item replace entity @a armor.feet with pirateatelier:captain_armor_pristine_normal_boots");
   w.getServer().runCommand("item replace entity @a weapon.offhand with pirateatelier:atelier_hook_brass");
   w.getServer().runOnServer(s->{var player=s.getPlayerList().getPlayers().getFirst();var chest=player.getItemBySlot(EquipmentSlot.CHEST);chest.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);
    var material=s.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.TRIM_MATERIAL).getOrThrow(net.minecraft.world.item.equipment.trim.TrimMaterials.GOLD);
    var pattern=s.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.TRIM_PATTERN).getOrThrow(net.minecraft.world.item.equipment.trim.TrimPatterns.COAST);
    chest.set(net.minecraft.core.component.DataComponents.TRIM,new net.minecraft.world.item.equipment.trim.ArmorTrim(material,pattern));});
   TricornePoseReview.reviewPlayer(c,w);
  }
  System.out.println("QA_ATELIER_SKINS_COMPLETE count=20");
 }
}
