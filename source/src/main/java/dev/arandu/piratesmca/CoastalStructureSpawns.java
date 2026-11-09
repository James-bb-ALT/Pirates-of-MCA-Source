package dev.arandu.piratesmca;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/** Event-scoped generation context. No per-tick structure searches or nearby-NPC conversions. */
public final class CoastalStructureSpawns {
    private CoastalStructureSpawns(){}
    public static final TagKey<Biome> BIOMES=TagKey.create(Registries.BIOME,Identifier.fromNamespaceAndPath(PiratesOfMCA.ID,"pirate_waters"));
    private record Context(WorldGenLevel level,String origin){}
    private static final ThreadLocal<Context> GENERATING=new ThreadLocal<>();

    public static void during(WorldGenLevel level,StructureStart start,Runnable action) {
        var previous=GENERATING.get();
        var id=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
        var chunk=start.getChunkPos();
        if(id==null)GENERATING.remove();
        else GENERATING.set(new Context(level,"pmca_origin_structure_"+id+"_"+chunk.x()+"_"+chunk.z()));
        try{action.run();}finally{if(previous==null)GENERATING.remove();else GENERATING.set(previous);}
    }
    public static void markGenerated(Entity entity) {
        var context=GENERATING.get();
        if(context==null || !(entity instanceof Villager) || entity.entityTags().contains(PiratesOfMCA.MARKER))return;
        if(entity.level()!=context.level().getLevel() || !context.level().getBiome(entity.blockPosition()).is(BIOMES))return;
        entity.addTag(PiratesOfMCA.MARKER);
        entity.addTag("pmca_adult_required");
        entity.addTag(context.origin());
        // MCA entity type/genome is authoritative. Vanilla replacements choose
        // their gender through MCA, rather than an unrelated second coin flip.
        if(entity instanceof VillagerEntityMCA npc) {
            if(npc.getType()==net.conczin.mca.registry.EntitiesMCA.MALE_VILLAGER)entity.addTag("pmca_gender_male");
            else if(npc.getType()==net.conczin.mca.registry.EntitiesMCA.FEMALE_VILLAGER)entity.addTag("pmca_gender_female");
        }
    }
}
