package dev.arandu.piratesmca.client;
import java.util.*;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;

/** Vanilla trim UV projected onto each main garment surface, without reintroducing a closed helmet. */
public final class WardrobeTrimModel extends Model<HumanoidRenderState>{
 private static final Map<WardrobeGeometry.Part,WardrobeTrimModel[]> CACHE=new WeakHashMap<>();
 private WardrobeTrimModel(List<ModelPart.Cube> cubes){super(new ModelPart(cubes,Map.of()),RenderTypes::armorCutoutNoCull);}
 public static WardrobeTrimModel get(WardrobeGeometry.Part part,boolean clipped){
  var models=CACHE.computeIfAbsent(part,k->new WardrobeTrimModel[2]);int index=clipped?1:0;
  if(models[index]==null)models[index]=create(part,clipped);return models[index];
 }
 private static WardrobeTrimModel create(WardrobeGeometry.Part part,boolean clipped){
  var s=part.full().getFirst();boolean head=part.bone().equals("head"),body=part.bone().equals("body"),arm=part.bone().endsWith("arm");
  int u=head?0:body?16:arm?40:0,v=head?0:16;
  var vanilla=new ModelPart.Cube(u,v,head||body?-4:-2,head?-8:arm?-2:0,head?-4:-2,head||body?8:4,head?8:12,head?8:4,0,0,0,false,64,32,EnumSet.allOf(Direction.class));
  float pad=.018F;var target=new ModelPart.Cube(0,0,s.minX-pad,s.minY-pad,s.minZ-pad,s.maxX-s.minX+pad*2,s.maxY-s.minY+pad*2,s.maxZ-s.minZ+pad*2,0,0,0,false,64,32,EnumSet.allOf(Direction.class));
  for(int f=0;f<target.polygons.length;f++){
   var p=target.polygons[f];var uv=vanilla.polygons[f].vertices();var vertices=new ModelPart.Vertex[4];
   for(int i=0;i<4;i++){var a=p.vertices()[i];vertices[i]=new ModelPart.Vertex(a.x(),a.y(),a.z(),uv[i].u(),uv[i].v());}
   target.polygons[f]=new ModelPart.Polygon(vertices,p.normal());
  }
  var cubes=List.of(target);return new WardrobeTrimModel(clipped?UpperLimbGeometry.clipped(new ModelPart(cubes,Map.of()),arm?6:6.3F,0):cubes);
 }
 @Override public void setupAnim(HumanoidRenderState state){}
}
