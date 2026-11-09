package dev.arandu.piratesmca.mixin;

import dev.arandu.piratesmca.PiratesOfMCA;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Tags the precise villagers spawned by Fish of Thieves seapost marker branches.
 *  This tag is copied to MCA villagers by MCA's SpawnQueue.copyPastaIntensifies().
 */
@Mixin(targets = "com.stevekung.fishofthieves.structure.SeapostPieces$SeapostPiece", remap = false)
public abstract class SeapostPiratesMixin {
    @ModifyArg(
        method = "handleDataMarker",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V", remap = false),
        index = 0,
        remap = false,
        require = 2
    )
    private Entity piratesmca$markSeapostVillager(Entity entity) {
        // Both call sites in Fish of Thieves 26.2.2.0 are seapost fisherman/leatherworker spawns.
        if (entity != null) {
            entity.addTag(PiratesOfMCA.MARKER);
            entity.addTag("pmca_adult_required");
            entity.addTag(entity.getRandom().nextBoolean()?"pmca_gender_male":"pmca_gender_female");
            BoundingBox box=((StructurePiece)(Object)this).getBoundingBox();
            // Exact, reproducible seapost origin; no merging crews merely for being nearby.
            entity.addTag("pmca_origin_"+box.minX()+"_"+box.minY()+"_"+box.minZ());
        }
        return entity;
    }
}
