package dev.arandu.piratesmca;

import net.fabricmc.api.ModInitializer;

/** Compatibility addon; original mod JARs are not modified. */
public final class PiratesOfMCA implements ModInitializer {
    public static final String ID = "piratesmca";
    public static final String MARKER = "pmca_seapost";
    public static final String READY = "pmca_ready";

    @Override
    public void onInitialize() {
        PirateProfession.register();
        PirateFlag.register();
        PirateWearables.register();
        PirateSash.register();
        PirateOutfits.register();
        PirateAtelier.register();
        PirateCreativeTab.register();
        PirateBehavior.initialize();
        PirateCommands.register();
        CrewRuntime.initialize();
        PirateDialogues.initialize();
        String version = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(ID)
                .orElseThrow().getMetadata().getVersion().getFriendlyString();
        System.out.println("[Pirates of MCA] v" + version + " loaded: coastal structure crews, crew flags, pirate guard AI, captain commands");
    }
}
