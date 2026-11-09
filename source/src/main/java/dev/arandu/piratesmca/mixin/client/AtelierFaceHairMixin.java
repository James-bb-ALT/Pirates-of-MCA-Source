package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.PirateAtelier;
import net.conczin.mca.client.render.VillagerVisuals;
import net.conczin.mca.client.render.layer.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Render the authored face once using MCA's blink state; native HairLayer stays active. */
@Mixin(value=FaceLayer.class,remap=false)
public abstract class AtelierFaceHairMixin {
 @Inject(method="renderFinal(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FZZ)V",at=@At("HEAD"),cancellable=true,remap=false)
 private void piratesmca$avoidDoubleFace(PoseStack pose,SubmitNodeCollector nodes,int light,HumanoidRenderState state,float delta,boolean translucent,boolean outline,CallbackInfo ci){
  var visuals=VillagerVisuals.require(state);var profile=dev.arandu.piratesmca.client.PirateSkinLayers.get(visuals.skin());if(profile==null)return;
  if(translucent||outline){var model=((VillagerLayer)(Object)this).model;
   var texture=net.minecraft.resources.Identifier.parse(visuals.isBlinking()?profile.blink():profile.face());
   var type=translucent?net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(texture):net.minecraft.client.renderer.rendertype.RenderTypes.outline(texture);
   nodes.submitModel(model,state,pose,type,light,net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords(state,0),-1,null,state.outlineColor,null);
  }ci.cancel();
 }
}
