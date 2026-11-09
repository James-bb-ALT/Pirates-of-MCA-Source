package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.PirateSash;
import dev.arandu.piratesmca.client.SashRenderData;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class SashExtractMixin {
    @Shadow @Final protected ItemModelResolver itemModelResolver;
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
    private void piratesmca$extractSash(LivingEntity n,LivingEntityRenderState state,float partialTick,CallbackInfo ci){
        if(!(state instanceof SashRenderData data))return;
        data.piratesmca$sash().clear();
        var stack=n.getItemBySlot(EquipmentSlot.CHEST);
        if(!n.isBaby() && stack.is(PirateSash.ITEM))itemModelResolver.updateForLiving(data.piratesmca$sash(),stack,ItemDisplayContext.FIXED,n);
    }
}
