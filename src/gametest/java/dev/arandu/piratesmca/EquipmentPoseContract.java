package dev.arandu.piratesmca;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import dev.arandu.piratesmca.client.EquipmentPose;
import dev.arandu.piratesmca.mixin.client.ModelPartCubesAccessor;
import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.minecraft.client.model.geom.builders.*;

/** Compare actual native MCA renderCommon vertices with the accessory attachment space.
 * The negative control proves that the former extra root transform fails this contract.
 */
final class EquipmentPoseContract {
 static void run(){
  var model=new VillagerEntityModelMCA(LayerDefinition.create(VillagerEntityModelMCA.bodyData(CubeDeformation.NONE),64,64).bakeRoot()).hideWears();
  model.setAllVisible(false);model.rightArm.visible=true;int cases=0;
  for(float rootYaw:new float[]{-.4F,0,.4F})for(float swing:new float[]{-.8F,0,.8F}){
   model.root().y=1.4F;model.root().yRot=rootYaw;model.rightArm.xRot=swing;
   var nativeVertices=new Capture();model.renderCommon(new PoseStack(),nativeVertices,0,0,-1);
   var attached=new PoseStack();EquipmentPose.root(attached,model);model.rightArm.translateAndRotate(attached);
   var corrected=new Capture();var cubes=((ModelPartCubesAccessor)(Object)model.rightArm).piratesmca$cubes();
   for(var cube:cubes)cube.compile(attached.last(),corrected,0,0,-1);
   PirateClientGameTests.expect(equal(nativeVertices.values,corrected.values),"VISUAL-ARM-ROOT native MCA vertices differ from attachment transform");
   var old=new PoseStack();model.root().translateAndRotate(old);model.rightArm.translateAndRotate(old);var wrong=new Capture();for(var cube:cubes)cube.compile(old.last(),wrong,0,0,-1);
   PirateClientGameTests.expect(!equal(nativeVertices.values,wrong.values),"VISUAL-ARM-ROOT negative control did not reproduce extra root displacement");cases++;
  }
  System.out.println("QA_EQUIPMENT_NATIVE_VERTEX_CONTRACT_PASS cases="+cases+" negative_control=extra_root_fails");
  var state=new net.conczin.mca.client.render.VillagerRenderState();
  ((dev.arandu.piratesmca.client.PirateLimbsData)state).piratesmca$wardrobeEnabled(true);
  state.chestEquipment=new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("pirateatelier:captain_armor_pristine_normal_chestplate")));
  model.getBreastPart().visible=true;dev.arandu.piratesmca.client.PirateLimbMask.apply(model,state);
  PirateClientGameTests.expect(!model.getBreastPart().visible,"VISUAL-FEMALE-CHEST covered anatomy was not masked");
  dev.arandu.piratesmca.client.PirateLimbMask.restore(model);
  PirateClientGameTests.expect(model.getBreastPart().visible,"VISUAL-FEMALE-CHEST unequip did not restore native body");
  state.chestEquipment=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE);
  dev.arandu.piratesmca.client.PirateLimbMask.apply(model,state);
  PirateClientGameTests.expect(model.getBreastPart().visible,"VISUAL-FEMALE-CHEST affected native armor fallback");
  System.out.println("QA_COVERED_ANATOMY_MASK_PASS restore=true native_armor_unchanged=true");
  for(var arm:net.minecraft.world.entity.HumanoidArm.values()){
   if(arm==net.minecraft.world.entity.HumanoidArm.RIGHT)state.rightHandItemStack=new net.minecraft.world.item.ItemStack(PirateAtelier.WEAPONS.getFirst());
   else state.leftHandItemStack=new net.minecraft.world.item.ItemStack(PirateAtelier.WEAPONS.getFirst());
   var attachedHand=new PoseStack();model.translateToHand(state,arm,attachedHand);
   var expectedHand=new PoseStack();model.getArm(arm).translateAndRotate(expectedHand);
   PirateClientGameTests.expect(attachedHand.last().pose().equals(expectedHand.last().pose(),1E-6F),"VISUAL-HELD-ROOT addon item differs from visible MCA arm: "+arm);
   if(arm==net.minecraft.world.entity.HumanoidArm.RIGHT)state.rightHandItemStack=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD);
   else state.leftHandItemStack=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD);
   var nativeHand=new PoseStack();model.translateToHand(state,arm,nativeHand);
   var expectedNative=new PoseStack();model.root().translateAndRotate(expectedNative);model.getArm(arm).translateAndRotate(expectedNative);
   PirateClientGameTests.expect(nativeHand.last().pose().equals(expectedNative.last().pose(),1E-6F),"VISUAL-HELD-ROOT changed an unrelated item: "+arm);
  }
  System.out.println("QA_HELD_ROOT_CONTRACT_PASS sides=2 native_items_unchanged=true");
 }
 private static boolean equal(List<float[]> a,List<float[]> b){if(a.isEmpty()||a.size()!=b.size())return false;for(int i=0;i<a.size();i++)for(int j=0;j<3;j++)if(Math.abs(a.get(i)[j]-b.get(i)[j])>1E-6F)return false;return true;}
 private static final class Capture implements VertexConsumer {
  final List<float[]> values=new ArrayList<>();
  public VertexConsumer addVertex(float x,float y,float z){values.add(new float[]{x,y,z});return this;}
  public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setColor(int color){return this;}
  public VertexConsumer setUv(float u,float v){return this;}public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}
  public VertexConsumer setNormal(float x,float y,float z){return this;}public VertexConsumer setLineWidth(float width){return this;}
 }
}
