package dev.arandu.piratesmca.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;

/** Match the actual draw path: MCA renderCommon draws the limbs without the root.
 * Vanilla/player models draw their root normally. MCA also scales the head separately.
 */
public final class EquipmentPose {
 private EquipmentPose(){}
 public static void root(PoseStack pose,HumanoidModel<?> model){
  if(!(model instanceof net.conczin.mca.client.model.CommonVillagerModel<?>))model.root().translateAndRotate(pose);
 }
 public static void headScale(PoseStack pose,HumanoidModel<?> model){
  if(model instanceof net.conczin.mca.client.model.CommonVillagerModel<?> mca){float scale=mca.getDimensions().getHead();pose.scale(scale,scale,scale);}
 }
}
