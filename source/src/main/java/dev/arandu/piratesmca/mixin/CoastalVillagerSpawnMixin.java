package dev.arandu.piratesmca.mixin;

import dev.arandu.piratesmca.CoastalStructureSpawns;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mark before MCA intercepts/replaces a vanilla villager or worldgen serializes it. */
@Mixin({ServerLevel.class,WorldGenRegion.class})
public abstract class CoastalVillagerSpawnMixin {
    @Inject(method="addFreshEntity",at=@At("HEAD"))
    private void piratesmca$coastalVillager(Entity entity,CallbackInfoReturnable<Boolean> cir){CoastalStructureSpawns.markGenerated(entity);}
}
