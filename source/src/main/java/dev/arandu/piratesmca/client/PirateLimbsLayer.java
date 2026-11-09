package dev.arandu.piratesmca.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.arandu.piratesmca.PirateAtelier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Native upper skin/clothing/armor + actual 3D prosthesis, following the animated limb. */
@SuppressWarnings({"rawtypes","unchecked"})
public final class PirateLimbsLayer<S extends HumanoidRenderState,M extends HumanoidModel<S>> extends RenderLayer<S,M> {
 private final net.conczin.mca.client.render.layer.SkinLayer skin;
 private final net.conczin.mca.client.render.layer.ClothingLayer clothes;
 private final net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer equipmentRenderer;
 private final ModelPart innerArmor=LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(.5F),0),64,32).bakeRoot();
 private final Map<ModelPart,UpperArmorModel> armorModels=new WeakHashMap<>();
 private final ModelPart armor=LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(1),0),64,32).bakeRoot();
 private final Map<ModelPart,List<ModelPart.Cube>> upper=new WeakHashMap<>(),cloth=new WeakHashMap<>(),upperArmor=new WeakHashMap<>();
 public PirateLimbsLayer(RenderLayerParent<S,M> parent,net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer equipmentRenderer){super(parent);this.equipmentRenderer=equipmentRenderer;boolean mca=parent instanceof net.conczin.mca.client.render.VillagerLikeEntityMCARenderer;
  skin=mca?new net.conczin.mca.client.render.layer.SkinLayer(parent,parent.getModel()):null;clothes=mca?new net.conczin.mca.client.render.layer.ClothingLayer(parent,parent.getModel(),"normal"):null;}
 private void draw(PoseStack p,SubmitNodeCollector nodes,int light,int overlay,List<ModelPart.Cube> cubes,Identifier tex,int color){
  if(tex==null)return;nodes.submitCustomGeometry(p,RenderTypes.armorCutoutNoCull(tex),(pose,vertices)->{for(var c:cubes)c.compile(pose,vertices,light,overlay,color);});
 }
 @Override public void submit(PoseStack p,SubmitNodeCollector nodes,int light,S state,float yaw,float pitch){
  if(!(state instanceof PirateLimbsData data) || state.isInvisible || (state instanceof AvatarRenderState a && a.isSpectator))return;var meshes=data.piratesmca$limbs();var model=getParentModel();
  ModelPart[] bones={model.leftArm,model.rightArm,model.leftLeg,model.rightLeg};String[] names={"left_arm","right_arm","left_leg","right_leg"};
  for(int i=0;i<4;i++)if(!meshes[i].isEmpty()){
   var bone=bones[i];var geometry=UpperLimbGeometry.visibleSource(bone,state,names[i]);final float cut=i<2?6:6.3F;int overlay=LivingEntityRenderer.getOverlayCoords(state,0);
   p.pushPose();try{
    EquipmentPose.root(p,model);bone.translateAndRotate(p);
    Identifier texture=skin!=null?skin.getSkin(state):state instanceof AvatarRenderState a?a.skin.body().texturePath():null;
    int color=skin!=null?skin.getColor(state,0):-1;
    draw(p,nodes,light,overlay,upper.computeIfAbsent(geometry,b->UpperLimbGeometry.clipped(b,cut,0)),texture,color);
    if(clothes!=null)draw(p,nodes,light,overlay,cloth.computeIfAbsent(geometry,b->UpperLimbGeometry.clipped(b,cut,.0625F)),clothes.getSkin(state),clothes.getColor(state,0));
    ItemStack[] equipped=i<2?new ItemStack[]{state.chestEquipment}:new ItemStack[]{state.legsEquipment,state.feetEquipment};
    for(var item:equipped){var eq=item.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);if(eq==null || eq.assetId().isEmpty())continue;
     if(WardrobeGeometry.get(item)!=null)continue;
     boolean inner=eq.slot()==net.minecraft.world.entity.EquipmentSlot.LEGS;var source=(inner?innerArmor:armor).getChild(names[i]);
     equipmentRenderer.renderLayers(inner?net.minecraft.client.resources.model.EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS:net.minecraft.client.resources.model.EquipmentClientInfo.LayerType.HUMANOID,eq.assetId().get(),armorModels.computeIfAbsent(source,boneSource->new UpperArmorModel(boneSource,cut)),state,item,p,nodes,light,state.outlineColor);
    }
    if(i<2){var cubes=((dev.arandu.piratesmca.mixin.client.ModelPartCubesAccessor)(Object)geometry).piratesmca$cubes();if(!cubes.isEmpty())p.translate((cubes.getFirst().minX+cubes.getFirst().maxX)/32F,0,0);}
    AnimationPoseProbe.record(model,state,bone,i,p);
    var armorPeg=WardrobeGeometry.armorPeg(state,i);
    if(armorPeg!=null)draw(p,nodes,light,overlay,armorPeg.cubes(),armorPeg.texture(),-1);
    else{p.scale(1,-1,-1);meshes[i].submit(p,nodes,light,overlay,state.outlineColor);}
   }finally{p.popPose();}
  }
 }
}
