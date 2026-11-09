package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.client.PirateLimbMask;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HumanoidModel.class)
public abstract class PirateLimbModelMixin {
 @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",at=@At("HEAD"))
 private void piratesmca$restore(HumanoidRenderState state,CallbackInfo ci){PirateLimbMask.restore((HumanoidModel<?>)(Object)this);}
 @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",at=@At("TAIL"))
 private void piratesmca$mask(HumanoidRenderState state,CallbackInfo ci){PirateLimbMask.apply((HumanoidModel<?>)(Object)this,state);}
}
