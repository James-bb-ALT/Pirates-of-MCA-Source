package dev.arandu.piratesmca.client;

import java.util.*;
import com.google.gson.*;
import dev.arandu.piratesmca.PirateAtelier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Loads per-equipment authored geometry lazily; texture packs remain resolved by Minecraft. */
public final class WardrobeGeometry {
 private WardrobeGeometry(){}
 public record Part(String bone,List<ModelPart.Cube> full,List<ModelPart.Cube> upper,List<ModelPart.Cube> prosthesis){}
 public record Mesh(Identifier texture,List<Part> parts,String resource,boolean opaqueTorso){}
 public record Prosthesis(Identifier texture,List<ModelPart.Cube> cubes){}
 private static final Map<String,Map<String,Mesh>> CACHE=new HashMap<>();
 public static void clear(){CACHE.clear();}
 /** Cull only anatomy hidden by an authored opaque full torso shell. */
 public static boolean coversTorso(ItemStack item){
  var mesh=get(item);return mesh!=null&&mesh.opaqueTorso();
 }
 public static Mesh get(ItemStack item){
  var e=PirateAtelier.ITEMS.get(item.getItem());if(e==null||!e.kind().equals("armor"))return null;
  return CACHE.computeIfAbsent(e.asset(),WardrobeGeometry::load).get(e.slot());
 }
 public static Mesh get(ItemStack item,net.minecraft.client.renderer.entity.state.HumanoidRenderState state){
  var e=PirateAtelier.ITEMS.get(item.getItem());
  if(e!=null&&"boots".equals(e.slot())&&state instanceof PirateLimbsData data){
   int side=e.asset().endsWith("_peg_left")?2:e.asset().endsWith("_peg_right")?3:-1;
   if(side>=0&&data.piratesmca$limbs()[side].isEmpty()){
    var normal=e.asset().replaceFirst("_peg_(left|right)$","_normal");var mesh=CACHE.computeIfAbsent(normal,WardrobeGeometry::load).get("boots");if(mesh!=null)return mesh;
   }
  }return get(item);
 }
 public static Prosthesis armorPeg(net.minecraft.client.renderer.entity.state.HumanoidRenderState state,int limb){
  if(limb<2)return null;var foot=PirateAtelier.ITEMS.get(state.feetEquipment.getItem());if(foot!=null&&foot.kind().equals("peg"))return null;
  String suffix=limb==2?"_peg_left":"_peg_right",bone=limb==2?"left_leg":"right_leg";
  for(var stack:List.of(state.legsEquipment,state.feetEquipment)){
   var e=PirateAtelier.ITEMS.get(stack.getItem());if(e==null||!e.kind().equals("armor")||!e.asset().endsWith(suffix))continue;
   var mesh=CACHE.computeIfAbsent(e.asset(),WardrobeGeometry::load).get("leggings");if(mesh!=null)for(var p:mesh.parts())if(p.bone().equals(bone)&&!p.prosthesis().isEmpty())return new Prosthesis(mesh.texture(),p.prosthesis());
  }return null;
 }
 private static Map<String,Mesh> load(String asset){
  var id=Identifier.parse(asset);var file=Identifier.fromNamespaceAndPath("piratesmca","wardrobe/"+id.getNamespace()+"/"+id.getPath()+".json");
  var resource=Minecraft.getInstance().getResourceManager().getResource(file);if(resource.isEmpty())return Map.of();
  try(var reader=resource.get().openAsReader()){
   var json=JsonParser.parseReader(reader).getAsJsonObject();var out=new HashMap<String,Mesh>();var texture=Identifier.parse(json.get("texture").getAsString());
   int width=json.getAsJsonArray("textureSize").get(0).getAsInt(),height=json.getAsJsonArray("textureSize").get(1).getAsInt();
   for(var slot:json.getAsJsonObject("slots").entrySet()){
    var selectedTexture=json.has("slotTextures")?Identifier.parse(json.getAsJsonObject("slotTextures").get(slot.getKey()).getAsString()):texture;
    boolean opaqueTorso=false;
    var parts=new ArrayList<Part>();for(var value:slot.getValue().getAsJsonArray()){
     var part=value.getAsJsonObject();String bone=part.get("part").getAsString();var cubes=new ArrayList<ModelPart.Cube>();
     for(var b:part.getAsJsonArray("boxes")){
      var box=b.getAsJsonObject();var meshCube=cube(box,width,height);cubes.add(meshCube);
      if(slot.getKey().equals("chestplate")&&bone.equals("body")&&meshCube.minX<=-4&&meshCube.maxX>=4&&meshCube.minY<=.1F&&meshCube.maxY>=11.8F&&meshCube.minZ<=-2&&meshCube.maxZ>=2)
       opaqueTorso|=opaqueFaces(box,selectedTexture);
     }
     var prosthesis=new ArrayList<ModelPart.Cube>();if(part.has("prostheticBoxes"))for(var b:part.getAsJsonArray("prostheticBoxes"))prosthesis.add(cube(b.getAsJsonObject(),width,height));
     float cut=bone.endsWith("arm")?6:6.3F;
     parts.add(new Part(bone,List.copyOf(cubes),UpperLimbGeometry.clipped(new ModelPart(cubes,Map.of()),cut,0),List.copyOf(prosthesis)));
   }
    out.put(slot.getKey(),new Mesh(selectedTexture,List.copyOf(parts),file.toString(),opaqueTorso));
   }return Map.copyOf(out);
  }catch(Exception error){throw new IllegalStateException("Invalid wardrobe geometry "+file,error);}
 }
 private static boolean opaqueFaces(JsonObject box,Identifier texture)throws java.io.IOException{
  try(var in=Minecraft.getInstance().getResourceManager().getResourceOrThrow(texture).open();var image=com.mojang.blaze3d.platform.NativeImage.read(in)){
   for(String face:List.of("Up","Down","North","South","West","East")){
    var uv=box.getAsJsonArray("uv"+face);int u0=(int)Math.floor(Math.min(uv.get(0).getAsDouble(),uv.get(2).getAsDouble())),u1=(int)Math.ceil(Math.max(uv.get(0).getAsDouble(),uv.get(2).getAsDouble()));
    int v0=(int)Math.floor(Math.min(uv.get(1).getAsDouble(),uv.get(3).getAsDouble())),v1=(int)Math.ceil(Math.max(uv.get(1).getAsDouble(),uv.get(3).getAsDouble()));
    for(int y=Math.max(0,v0);y<Math.min(image.getHeight(),v1);y++)for(int x=Math.max(0,u0);x<Math.min(image.getWidth(),u1);x++)if((image.getPixel(x,y)>>>24)!=255)return false;
   }return true;
  }
 }
 private static ModelPart.Cube cube(JsonObject box,int width,int height){
  var c=box.getAsJsonArray("coordinates");float x=c.get(0).getAsFloat(),y=c.get(1).getAsFloat(),z=c.get(2).getAsFloat(),w=c.get(3).getAsFloat(),h=c.get(4).getAsFloat(),d=c.get(5).getAsFloat();
  var cube=new ModelPart.Cube(0,0,x,y,z,w,h,d,0,0,0,false,width,height,EnumSet.allOf(Direction.class));
  for(int f=0;f<cube.polygons.length;f++){
   var polygon=cube.polygons[f];var n=polygon.normal();String side=n.x()<-.5?"West":n.x()>.5?"East":n.y()<-.5?"Up":n.y()>.5?"Down":n.z()<-.5?"North":"South";
   var uv=box.getAsJsonArray("uv"+side);if(uv==null)throw new IllegalArgumentException("Absent face "+side);
   float minU=Float.MAX_VALUE,minV=Float.MAX_VALUE,maxU=-Float.MAX_VALUE,maxV=-Float.MAX_VALUE;
   for(var v:polygon.vertices()){minU=Math.min(minU,v.u());maxU=Math.max(maxU,v.u());minV=Math.min(minV,v.v());maxV=Math.max(maxV,v.v());}
   var verts=new ModelPart.Vertex[4];int i=0;for(var v:polygon.vertices()){
    float u=uv.get(v.u()<=(minU+maxU)*.5F?0:2).getAsFloat()/width,texV=uv.get(v.v()<=(minV+maxV)*.5F?1:3).getAsFloat()/height;
    verts[i++]=new ModelPart.Vertex(v.x(),v.y(),v.z(),u,texV);
   }cube.polygons[f]=new ModelPart.Polygon(verts,polygon.normal());
  }return cube;
 }
}
