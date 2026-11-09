package dev.arandu.piratesmca.mixin;

import dev.arandu.piratesmca.CrewRuntime;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class BrawlFireMixin {
    @Inject(method="igniteForTicks",at=@At("HEAD"),cancellable=true)
    private void piratesmca$preventBrawlIgnition(int ticks,CallbackInfo ci) {
        if(ticks>0 && CrewRuntime.isBrawlSecondaryTarget((Entity)(Object)this))ci.cancel();
    }
}
