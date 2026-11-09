package dev.arandu.piratesmca.client;
import java.util.*;
import net.minecraft.client.model.geom.ModelPart;
import dev.arandu.piratesmca.mixin.client.ModelPartCubesAccessor;
/** Original upper limb retained, UV interpolated at the cut. Never edits native cubes. */
public final class UpperLimbGeometry {
 private UpperLimbGeometry(){}
 private static final ModelPart MCA=net.minecraft.client.model.geom.builders.LayerDefinition.create(net.conczin.mca.client.model.VillagerEntityModelMCA.bodyData(net.minecraft.client.model.geom.builders.CubeDeformation.NONE),64,64).bakeRoot();
 private static final ModelPart SLIM=net.minecraft.client.model.geom.builders.LayerDefinition.create(net.minecraft.client.model.player.PlayerModel.createMesh(net.minecraft.client.model.geom.builders.CubeDeformation.NONE,true),64,64).bakeRoot();
 /** MCA's parent model is an animation skeleton with empty cubes; use its real visible body mesh. */
 public static ModelPart visibleSource(ModelPart animated,net.minecraft.client.renderer.entity.state.HumanoidRenderState state,String part){
  if(!((ModelPartCubesAccessor)(Object)animated).piratesmca$cubes().isEmpty())return animated;
  boolean slim=state instanceof net.minecraft.client.renderer.entity.state.AvatarRenderState a && a.skin.model()==net.minecraft.world.entity.player.PlayerModelType.SLIM;
  return (slim?SLIM:MCA).getChild(part);
 }
 public static List<ModelPart.Cube> clipped(ModelPart part,float cut,float inflate){
  var out=new ArrayList<ModelPart.Cube>();for(var c:((ModelPartCubesAccessor)(Object)part).piratesmca$cubes()){
   if(c.minY>=cut)continue;
   var copy=new ModelPart.Cube(0,0,c.minX,c.minY,c.minZ,c.maxX-c.minX,c.maxY-c.minY,c.maxZ-c.minZ,0,0,0,false,64,64,java.util.EnumSet.allOf(net.minecraft.core.Direction.class));
   for(int f=0;f<c.polygons.length;f++){
    var p=c.polygons[f];var v=p.vertices();var vertices=new ModelPart.Vertex[v.length];
    for(int i=0;i<v.length;i++){
     var a=v[i];float y=Math.min(a.y(),cut),u=a.u(),texV=a.v();
     if(y!=a.y())for(var b:v)if(Math.abs(b.x()-a.x())<.001 && Math.abs(b.z()-a.z())<.001 && b.y()<a.y()){
      float t=(y-a.y())/(b.y()-a.y());u=a.u()+t*(b.u()-a.u());texV=a.v()+t*(b.v()-a.v());break;
     }
     float cx=(c.minX+c.maxX)/2,cz=(c.minZ+c.maxZ)/2;
     vertices[i]=new ModelPart.Vertex(a.x()+Math.signum(a.x()-cx)*inflate,y,a.z()+Math.signum(a.z()-cz)*inflate,u,texV);
    }
    copy.polygons[f]=new ModelPart.Polygon(vertices,p.normal());
   }out.add(copy);
  }return List.copyOf(out);
 }
}
