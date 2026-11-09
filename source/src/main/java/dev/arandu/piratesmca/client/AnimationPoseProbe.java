package dev.arandu.piratesmca.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/** Optional reconstruction telemetry, sampled from the actual render-layer submission. */
public final class AnimationPoseProbe {
 private AnimationPoseProbe(){}
 private static final Map<HumanoidRenderState,int[]> LAST=new WeakHashMap<>();
 public static void record(HumanoidModel<?> model,HumanoidRenderState state,ModelPart bone,int limb,PoseStack pose){
  if(System.getProperty("piratesmca.reconstructionReview")==null)return;
  int tick=(int)state.ageInTicks;var last=LAST.computeIfAbsent(state,k->new int[]{-100,-100,-100,-100});if(tick-last[limb]<5)return;last[limb]=tick;
  float[] matrix=new float[16];pose.last().pose().get(matrix);
  System.out.println("RECON_POST_ANIMATION kind="+(state instanceof net.minecraft.client.renderer.entity.state.AvatarRenderState?"AVATAR":"MCA")+" tick="+tick+" limb="+limb+" model="+model.getClass().getName()+" root="+model.root().getClass().getName()+" bone="+bone.getClass().getName()+" rotation="+bone.xRot+","+bone.yRot+","+bone.zRot+" matrix="+Arrays.toString(matrix));
 }
}
