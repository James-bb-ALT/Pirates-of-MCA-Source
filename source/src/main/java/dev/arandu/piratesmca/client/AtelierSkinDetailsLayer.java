package dev.arandu.piratesmca.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.arandu.piratesmca.PirateAtelier;
import net.conczin.mca.client.render.VillagerVisuals;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Preserves each original head overlay while opening the frontal cap above the eyes. No PNG edits. */
public final class AtelierSkinDetailsLayer<S extends HumanoidRenderState,M extends HumanoidModel<S>> extends RenderLayer<S,M>{
 private static final List<ModelPart.Cube> HAT=create();
 public AtelierSkinDetailsLayer(RenderLayerParent<S,M> parent){super(parent);}
 private static List<ModelPart.Cube> create(){
  // Keep the original headwear UV, but fit the overlay to the head: a .5px
  // vanilla shell leaves floating side strips when opening the face.
  var cubes=List.of(new ModelPart.Cube(32,0,-4,-8,-4,8,8,8,.02F,.02F,.02F,false,64,64,EnumSet.allOf(net.minecraft.core.Direction.class)));
  for(var cube:cubes)for(int f=0;f<cube.polygons.length;f++){
   var poly=cube.polygons[f];if(poly.normal().z()>-.5F)continue;var vertices=new ModelPart.Vertex[4];
   for(int i=0;i<4;i++){var a=poly.vertices()[i];float y=Math.min(a.y(),-6.1F),u=a.u(),v=a.v();
    if(y!=a.y())for(var b:poly.vertices())if(Math.abs(b.x()-a.x())<.001F&&b.y()<a.y()){float t=(y-a.y())/(b.y()-a.y());u=a.u()+t*(b.u()-a.u());v=a.v()+t*(b.v()-a.v());break;}
    vertices[i]=new ModelPart.Vertex(a.x(),y,a.z(),u,v);
   }cube.polygons[f]=new ModelPart.Polygon(vertices,poly.normal());
  }return List.copyOf(cubes);
 }
 @Override public void submit(PoseStack pose,SubmitNodeCollector nodes,int light,S state,float yaw,float pitch){
  if(state.isInvisible)return;var skin=VillagerVisuals.require(state).skin();if(!PirateAtelier.extraSkin(skin))return;
  pose.pushPose();try{EquipmentPose.root(pose,getParentModel());EquipmentPose.headScale(pose,getParentModel());getParentModel().head.translateAndRotate(pose);
   int overlay=LivingEntityRenderer.getOverlayCoords(state,0);
   nodes.submitCustomGeometry(pose,RenderTypes.armorCutoutNoCull(Identifier.parse(skin)),(p,vertices)->{for(var cube:HAT)cube.compile(p,vertices,light,overlay,-1);});
  }finally{pose.popPose();}
 }
}
