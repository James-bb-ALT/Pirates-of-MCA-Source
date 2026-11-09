package dev.arandu.piratesmca.client;
import java.util.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
public final class PirateLimbMask {
 private PirateLimbMask(){}
 private static final Map<HumanoidModel<?>,Map<ModelPart,Boolean>> SAVED=new WeakHashMap<>();
 public static void restore(HumanoidModel<?> model){var saved=SAVED.remove(model);if(saved!=null)saved.forEach((part,visible)->part.visible=visible);}
 public static void apply(HumanoidModel<?> model,HumanoidRenderState state){
  if(!(state instanceof PirateLimbsData data))return;var items=data.piratesmca$limbs();
  if(data.piratesmca$wardrobeEnabled()&&WardrobeGeometry.coversTorso(state.chestEquipment)
      &&model instanceof net.conczin.mca.client.model.CommonVillagerModel<?> mca){
   for(var part:mca.getBreastParts()){
    SAVED.computeIfAbsent(model,m->new IdentityHashMap<>()).putIfAbsent(part,part.visible);part.visible=false;
   }
  }
  ModelPart[] bones={model.leftArm,model.rightArm,model.leftLeg,model.rightLeg};String[] wears={"left_sleeve","right_sleeve","left_pants","right_pants"};
  for(int i=0;i<4;i++)if(!items[i].isEmpty()){
   SAVED.computeIfAbsent(model,m->new IdentityHashMap<>()).putIfAbsent(bones[i],bones[i].visible);bones[i].visible=false;
   if(model instanceof net.conczin.mca.client.model.VillagerEntityModelMCA mca){
    ModelPart[] overlays={mca.leftArmwear,mca.rightArmwear,mca.leftLegwear,mca.rightLegwear};
    var overlay=overlays[i];SAVED.computeIfAbsent(model,m->new IdentityHashMap<>()).putIfAbsent(overlay,overlay.visible);overlay.visible=false;
   }
   if(model.root().hasChild(wears[i])){var p=model.root().getChild(wears[i]);SAVED.computeIfAbsent(model,m->new IdentityHashMap<>()).putIfAbsent(p,p.visible);p.visible=false;}
  }
 }
}
