package dev.arandu.piratesmca;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.conczin.mca.registry.ProfessionsMCA;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;

/** A dedicated registry-backed profession, not an alias for fisherman or outlaw.
 *  Trades are datapack-driven (Minecraft 26.2 trade_set/villager_trade).
 */
public final class PirateProfession {
    private PirateProfession() {}

    public static final Identifier ID = Identifier.fromNamespaceAndPath(PiratesOfMCA.ID, "pirate");
    public static final ResourceKey<VillagerProfession> KEY =
            ResourceKey.create(BuiltInRegistries.VILLAGER_PROFESSION.key(), ID);
    public static VillagerProfession PIRATE;

    private static ResourceKey<TradeSet> tradeSet(int level) {
        return ResourceKey.create(Registries.TRADE_SET,
                Identifier.fromNamespaceAndPath(PiratesOfMCA.ID, "pirate/level_" + level));
    }

    public static void register() {
        if (PIRATE != null) return;
        @SuppressWarnings("unchecked")
        Int2ObjectMap.Entry<ResourceKey<TradeSet>>[] tradeLevels = new Int2ObjectMap.Entry[5];
        for (int level = 1; level <= 5; level++) {
            tradeLevels[level - 1] = Int2ObjectMap.entry(level, tradeSet(level));
        }
        PIRATE = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, ID,
                new VillagerProfession(
                        Component.translatable("entity.minecraft.villager.piratesmca.pirate"),
                        PoiType.NONE,
                        VillagerProfession.ALL_ACQUIRABLE_JOBS,
                        ImmutableSet.of(), ImmutableSet.of(),
                        SoundEvents.VILLAGER_WORK_FISHERMAN,
                        Int2ObjectMap.ofEntries(tradeLevels)));
        // IMPORTANT: Do NOT add the pirate profession to NEEDS_NO_HOME.
        // MCA VillagerTasksMCA.initializeTasks() tests requiresHome() before
        // isGuard(). The no-home shortcut gives pirates the guest/panic brain,
        // even when isGuard() returns true. Keep normal MCA brain selection,
        // then isGuard() selects the combat/guard task package.
        ProfessionsMCA.IS_IMPORTANT.add(KEY);
    }
}
