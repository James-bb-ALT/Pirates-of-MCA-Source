package dev.arandu.piratesmca.mixin;

import dev.arandu.piratesmca.CrewRuntime;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class BrawlEffectMixin {
    @Inject(method="addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",at=@At("HEAD"),cancellable=true)
    private void piratesmca$preventBrawlDebuff(MobEffectInstance effect,Entity source,CallbackInfoReturnable<Boolean> cir) {
        if(CrewRuntime.isBrawlSecondaryTarget((Entity)(Object)this) && !effect.getEffect().value().isBeneficial())cir.setReturnValue(false);
    }
}
