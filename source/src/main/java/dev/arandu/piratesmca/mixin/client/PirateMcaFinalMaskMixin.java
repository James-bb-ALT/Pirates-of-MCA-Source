package dev.arandu.piratesmca.mixin.client;

import dev.arandu.piratesmca.client.PirateLimbMask;
import net.conczin.mca.client.model.VillagerEntityBaseModelMCA;
import net.conczin.mca.client.render.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** MCA applies sex/body dimensions after HumanoidModel.setupAnim; mask covered anatomy afterward. */
@Mixin(value=VillagerEntityBaseModelMCA.class,remap=false)
public abstract class PirateMcaFinalMaskMixin {
 @Inject(method="setupAnim(Lnet/conczin/mca/client/render/VillagerRenderState;)V",at=@At("TAIL"),remap=false)
 private void piratesmca$coveredAnatomy(VillagerRenderState state,CallbackInfo ci){
  PirateLimbMask.apply((VillagerEntityBaseModelMCA)(Object)this,state);
 }
}
