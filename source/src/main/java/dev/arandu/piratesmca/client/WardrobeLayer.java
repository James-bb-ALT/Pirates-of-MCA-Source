package dev.arandu.piratesmca.client;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Equipment geometry follows the actual post-animation parent ModelParts, by item and slot. */
public final class WardrobeLayer<S extends HumanoidRenderState,M extends HumanoidModel<S>> extends RenderLayer<S,M>{
 private final net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer equipmentRenderer;
 private static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> TRIM_ASSET=net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,net.minecraft.resources.Identifier.parse("piratesmca:wardrobe_trim"));
 public WardrobeLayer(RenderLayerParent<S,M> parent,net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer equipmentRenderer){super(parent);this.equipmentRenderer=equipmentRenderer;}
 @Override public void submit(PoseStack pose,SubmitNodeCollector nodes,int light,S state,float yaw,float pitch){
  if(state.isInvisible||state instanceof AvatarRenderState a&&a.isSpectator)return;
  if(!(state instanceof PirateLimbsData data)||!data.piratesmca$wardrobeEnabled())return;
  var model=getParentModel();
  if(System.getProperty("piratesmca.visualFixReview")!=null&&((int)state.ageInTicks)%20==0&&state instanceof net.conczin.mca.client.render.VillagerStateHolder holder){
   var saved=holder.mca$getHumanoidModelPose();var actual=net.conczin.mca.client.render.HumanoidModelPose.capture(model);
   System.out.println("QA_EQUIPMENT_POSE tick="+state.ageInTicks+" saved="+saved+" actual="+actual);
  }
  for(var item:List.of(state.headEquipment,state.chestEquipment,state.legsEquipment,state.feetEquipment)){
   var mesh=WardrobeGeometry.get(item,state);if(mesh==null)continue;
   for(var part:mesh.parts()){
    int limb=switch(part.bone()){case "left_arm"->0;case "right_arm"->1;case "left_leg"->2;case "right_leg"->3;default->-1;};
    boolean amputated=limb>=0&&!data.piratesmca$limbs()[limb].isEmpty();
    var garment=amputated?part.upper():part.full();var e=dev.arandu.piratesmca.PirateAtelier.ITEMS.get(item.getItem());
    var cubes=amputated&&e!=null&&"boots".equals(e.slot())?java.util.stream.Stream.concat(garment.stream(),part.prosthesis().stream()).toList():garment;if(cubes.isEmpty())continue;
    ModelPart bone=switch(part.bone()){case "head"->model.head;case "body"->model.body;case "left_arm"->model.leftArm;case "right_arm"->model.rightArm;case "left_leg"->model.leftLeg;case "right_leg"->model.rightLeg;default->throw new IllegalStateException(part.bone());};
    pose.pushPose();try{
     EquipmentPose.root(pose,model);if(part.bone().equals("head"))EquipmentPose.headScale(pose,model);bone.translateAndRotate(pose);
     if(limb==0||limb==1){var actual=UpperLimbGeometry.visibleSource(bone,state,part.bone());var original=((dev.arandu.piratesmca.mixin.client.ModelPartCubesAccessor)(Object)actual).piratesmca$cubes();if(!original.isEmpty())pose.translate((original.getFirst().minX+original.getFirst().maxX)/32F,0,0);}
     int overlay=LivingEntityRenderer.getOverlayCoords(state,0);
     nodes.submitCustomGeometry(pose,RenderTypes.armorCutoutNoCull(mesh.texture()),(p,vertices)->{for(var cube:cubes)cube.compile(p,vertices,light,overlay,-1);});
     if(item.hasFoil())nodes.submitCustomGeometry(pose,RenderTypes.armorEntityGlint(),(p,vertices)->{for(var cube:cubes)cube.compile(p,vertices,light,overlay,-1);});
     if(item.get(net.minecraft.core.component.DataComponents.TRIM)!=null&&!part.full().isEmpty()){
      var shown=item.copy();shown.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE,false);
      var eq=item.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);boolean inner=eq!=null&&eq.slot()==net.minecraft.world.entity.EquipmentSlot.LEGS;
      equipmentRenderer.renderLayers(inner?net.minecraft.client.resources.model.EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS:net.minecraft.client.resources.model.EquipmentClientInfo.LayerType.HUMANOID,TRIM_ASSET,WardrobeTrimModel.get(part,amputated),state,shown,pose,nodes,light,state.outlineColor);
     }
    }finally{pose.popPose();}
   }
  }
 }
}
