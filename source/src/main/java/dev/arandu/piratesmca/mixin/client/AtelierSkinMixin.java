package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.PirateAtelier;
import net.conczin.mca.client.render.VillagerVisuals;
import net.conczin.mca.client.render.layer.SkinLayer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=SkinLayer.class,remap=false)
public abstract class AtelierSkinMixin {
 @Inject(method="prepareModel(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",at=@At("TAIL"),remap=false)
 private void piratesmca$openCap(net.minecraft.client.renderer.entity.state.HumanoidRenderState state,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){
  if(PirateAtelier.extraSkin(VillagerVisuals.require(state).skin()))((net.conczin.mca.client.render.layer.VillagerLayer)(Object)this).model.hat.visible=false;
 }
 @Inject(method="getSkin(Lnet/conczin/mca/client/render/VillagerVisuals;)Lnet/minecraft/resources/Identifier;",at=@At("HEAD"),cancellable=true,remap=false)
 private void piratesmca$fullSkin(VillagerVisuals v,CallbackInfoReturnable<Identifier> cir){if(PirateAtelier.extraSkin(v.skin()))cir.setReturnValue(dev.arandu.piratesmca.client.PirateSkinLayers.body(v.skin()));}
 @Inject(method="getColor(Lnet/conczin/mca/client/render/VillagerVisuals;F)I",at=@At("HEAD"),cancellable=true,remap=false)
 private void piratesmca$paintedColor(VillagerVisuals v,float delta,CallbackInfoReturnable<Integer> cir){if(PirateAtelier.extraSkin(v.skin()))cir.setReturnValue(-1);}
}
