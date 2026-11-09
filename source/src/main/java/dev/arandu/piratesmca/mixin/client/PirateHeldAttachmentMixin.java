package dev.arandu.piratesmca.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.arandu.piratesmca.PirateAtelier;
import dev.arandu.piratesmca.client.PirateLimbsData;
import net.conczin.mca.client.model.CommonVillagerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Addon items use the same root-free arm space as the actual MCA visible body. */
@Mixin(HumanoidModel.class)
public abstract class PirateHeldAttachmentMixin {
 @Inject(method="translateToHand(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V",at=@At("HEAD"),cancellable=true)
 private void piratesmca$visibleHand(HumanoidRenderState state,HumanoidArm arm,PoseStack pose,CallbackInfo ci){
  var model=(HumanoidModel<?>)(Object)this;
  if(!(model instanceof CommonVillagerModel<?>)||!(state instanceof PirateLimbsData data)||!data.piratesmca$wardrobeEnabled())return;
  var held=arm==HumanoidArm.LEFT?state.leftHandItemStack:state.rightHandItemStack;
  if(held==null||!PirateAtelier.owns(held.getItem()))return;
  model.getArm(arm).translateAndRotate(pose);ci.cancel();
 }
}
