package dev.arandu.piratesmca.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.arandu.piratesmca.CoastalStructureSpawns;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureStart.class)
public abstract class CoastalStructureContextMixin {
    @WrapMethod(method="placeInChunk")
    private void piratesmca$structureContext(WorldGenLevel level,StructureManager structures,ChunkGenerator generator,RandomSource random,BoundingBox box,ChunkPos chunk,Operation<Void> original) {
        CoastalStructureSpawns.during(level,(StructureStart)(Object)this,()->original.call(level,structures,generator,random,box,chunk));
    }
}
