package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.client.PirateLimbMask;
import net.conczin.mca.client.render.layer.VillagerLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=VillagerLayer.class,remap=false)
public abstract class PirateVillagerLimbMixin {
 @Shadow @Final public HumanoidModel model;
 @Inject(method="submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",at=@At("HEAD"),remap=false)
 private void piratesmca$restore(PoseStack p,SubmitNodeCollector n,int l,HumanoidRenderState s,float y,float x,CallbackInfo ci){PirateLimbMask.restore(model);}
 @Inject(method="renderFinal(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FZZ)V",at=@At("HEAD"),remap=false)
 private void piratesmca$mask(PoseStack p,SubmitNodeCollector n,int l,HumanoidRenderState s,float d,boolean t,boolean o,CallbackInfo ci){PirateLimbMask.apply(model,s);}
}
