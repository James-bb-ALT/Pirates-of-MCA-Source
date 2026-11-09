package dev.arandu.piratesmca;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.WeakHashMap;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Memories;
import net.conczin.mca.entity.ai.brain.VillagerBrain;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.Personality;
import net.conczin.mca.resources.Names;
import net.conczin.mca.resources.BodySkinList;
import net.conczin.mca.resources.ClothingList;
import net.conczin.mca.resources.HairStyleList;
import net.conczin.mca.resources.SkinVisualIds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Runs only on MCA villagers tagged by a coastal structure or Fish seapost adapter.
 *  Uses existing MCA personalities, relationship hearts, clothes, and mob navigation.
 *  No global villager type patch and no world-wide structure searches.
 */
public final class PirateBehavior {
    private PirateBehavior() {}

    private static final Map<VillagerEntityMCA, Combat> COMBAT =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Properties OPTIONS = new Properties();
    private static volatile boolean loaded;
    static void clearRuntime() { COMBAT.clear(); }

    private enum Profile {
        CAPTAIN("captain", 10, Personality.CRABBY, -48, 12),
        BRUISER("bruiser", 25, Personality.CRABBY, -32, 8),
        GREEDY("greedy", 25, Personality.GREEDY, -23, 6),
        SUSPICIOUS("suspicious", 25, Personality.ANXIOUS, -17, 4),
        FRIENDLY("friendly", 15, Personality.FRIENDLY, 7, 0);

        final String id;
        final int weight;
        final Personality personality;
        final int initialHearts;
        final int fatigue;

        Profile(String id, int weight, Personality personality, int initialHearts, int fatigue) {
            this.id = id;
            this.weight = weight;
            this.personality = personality;
            this.initialHearts = initialHearts;
            this.fatigue = fatigue;
        }
    }

    private static final class Combat {
        UUID opponent;
        long lastSwing;
        long expiry;
        long restUntil;
        final Map<UUID, Integer> seenHearts = new HashMap<>();
    }

    public static synchronized void initialize() {
        if (loaded) return;
        loaded = true;
        OPTIONS.setProperty("player_aggression", "false");
        OPTIONS.setProperty("crew_fights", "true");
        OPTIONS.setProperty("spawn_weapons", "true");
        OPTIONS.setProperty("initial_relationship_penalty", "true");
        OPTIONS.setProperty("outfits", "true");
        OPTIONS.setProperty("activation_range", "14");
        OPTIONS.setProperty("crew_fight_range", "8");
        OPTIONS.setProperty("outfit_count", "24");
        OPTIONS.setProperty("relationship_resistance", "true");
        OPTIONS.setProperty("mood_enforcement", "true");
        Path path = Path.of("config", "piratesmca.properties");
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                try (InputStream in = Files.newInputStream(path)) { OPTIONS.load(in); }
            } else {
                try (OutputStream out = Files.newOutputStream(path)) {
                    OPTIONS.store(out, "Pirates of MCA - server behavior (restart world to apply changes)");
                }
            }
        } catch (IOException ex) {
            System.err.println("[Pirates of MCA] Could not read/write config: " + ex);
        }
    }

    private static boolean flag(String key) {
        return Boolean.parseBoolean(OPTIONS.getProperty(key, "true"));
    }

    private static int number(String key, int defaultValue, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(OPTIONS.getProperty(key, "" + defaultValue))));
        } catch (NumberFormatException ignored) { return defaultValue; }
    }

    /**
     * MCA has two independent visual layers: the body skin (MCA registry), and the
     * clothing texture (piratesmca). Earlier versions called initializeSkin(false)
     * on the first entity tick, even if the MCA resource lists were unavailable.
     * Its randomizeSkin() then writes an EMPTY body skin; the old identity marker
     * incorrectly prevented further retries. NBT /summon can bypass finalizeSpawn.
     *
     * Never reroll an established appearance. Only repair missing fields, and
     * never mark a blank body skin as initialized.
     */
    private static void ensureMcaIdentity(VillagerEntityMCA npc) {
        boolean unnamed = npc.getCustomName() == null
                || npc.getCustomName().getString().isBlank()
                || "Unnamed".equalsIgnoreCase(npc.getCustomName().getString());
        boolean skinListReady = BodySkinList.getInstance() != null
                && !BodySkinList.getInstance().skins.isEmpty();
        boolean skinMissing = npc.getSkin() == null || npc.getSkin().isBlank()
                || (skinListReady && !SkinVisualIds.isBodySkin(npc.getSkin(), npc.getGenetics().getGender()));

        // Missing visual fields do not prove that an NPC has no established genome.
        // Genetics belong to MCA's spawn/inheritance pipeline, never to a repair tick.

        // A blank/missing skin must be retried after the MCA datapack reload.
        // randomizeSkin() does NOT overwrite the pirate outfit, unlike
        // initializeSkin(false), which calls randomizeClothes() as well.
        if (skinMissing && skinListReady && (npc.tickCount == 1 || npc.tickCount % 20 == 0)) {
            try {
                npc.randomizeSkin();
            } catch (RuntimeException ex) {
                if (npc.tickCount % 200 == 0)
                    System.err.println("[Pirates of MCA] Skin not ready: " + ex.getMessage());
            }
        }

        // Layered hair is independent of body/clothing. Generate only once if
        // no hair was ever assigned, rather than recoloring on chunk reload.
        if (!npc.entityTags().contains("pmca_hair_v203") && HairStyleList.getInstance() != null) {
            if (npc.getHairStyleId() == null || npc.getHairStyleId().isBlank()) {
                try { npc.randomizeHair(); }
                catch (RuntimeException ex) {
                    if (npc.tickCount % 200 == 0)
                        System.err.println("[Pirates of MCA] Hair not ready: " + ex.getMessage());
                }
            }
            if (npc.getHairStyleId() != null && !npc.getHairStyleId().isBlank())
                npc.addTag("pmca_hair_v203");
        }

        if (unnamed && (npc.tickCount == 1 || npc.tickCount % 20 == 0)) {
            try {
                String generated = Names.pickCitizenName(npc.getGenetics().getGender(), npc);
                if (generated != null && !generated.isBlank()
                        && !"Unnamed".equalsIgnoreCase(generated))
                    npc.setCustomName(Component.literal(generated));
            } catch (RuntimeException ex) {
                if (npc.tickCount % 200 == 0)
                    System.err.println("[Pirates of MCA] Citizen name unavailable: " + ex.getMessage());
            }
        }
    }

    private static int outfitIndex(VillagerEntityMCA npc) {
        for (String tag : npc.entityTags()) {
            if (tag.startsWith("pmca_outfit_")) {
                try {
                    int index = Integer.parseInt(tag.substring("pmca_outfit_".length()));
                    if (index >= 0 && index < 24) return index;
                } catch (NumberFormatException ignored) { }
            }
        }
        // Migrate an existing pirate texture to an indexed, persistent choice.
        String clothes = npc.getClothes();
        if (clothes != null && clothes.startsWith("piratesmca:skins/clothing/")) {
            try {
                int slash = clothes.lastIndexOf('/');
                int dot = clothes.lastIndexOf(".png");
                int index = Integer.parseInt(clothes.substring(slash + 1, dot));
                if (index >= 0 && index < 24) {
                    npc.addTag("pmca_outfit_" + index);
                    return index;
                }
            } catch (RuntimeException ignored) { }
        }
        Profile profile = profile(npc);
        int group = switch (profile == null ? Profile.FRIENDLY : profile) {
            case CAPTAIN -> 0;
            case BRUISER -> 1;
            case GREEDY -> 2;
            case SUSPICIOUS, FRIENDLY -> 3;
        };
        int index = group * 6 + npc.getRandom().nextInt(6);
        npc.addTag("pmca_outfit_" + index);
        return index;
    }

    private static void ensureOutfit(VillagerEntityMCA npc) {
        // Avoid registering a missing outfit as a valid visual state while the
        // clothing datapack is not loaded yet.
        if (ClothingList.getInstance() == null || ClothingList.getInstance().clothing.isEmpty()) return;
        int index = outfitIndex(npc);
        String group = npc.getGenetics().getGender() == Gender.FEMALE ? "female" : "male";
        String expected = "piratesmca:skins/clothing/" + group + "/" + index + ".png";
        String actual = npc.getClothes();
        // A valid wardrobe selection is authoritative, including a deliberate locked MCA outfit.
        // Lock state alone does not tell us that a change was accidental.
        boolean vanished = actual == null || actual.isBlank()
                || !SkinVisualIds.isClothing(actual,npc.getGenetics().getGender());
        if (vanished) {
            if (!expected.equals(actual)) npc.setClothes(expected);
            if (!npc.isClothingLocked()) npc.setClothingLocked(true);
        } else if(actual.startsWith("piratesmca:skins/clothing/")) {
            try {
                int chosen=Integer.parseInt(actual.substring(actual.lastIndexOf('/')+1,actual.lastIndexOf('.')));
                if(chosen>=0 && chosen<24) {
                    for(String tag:new java.util.ArrayList<>(npc.entityTags()))
                        if(tag.startsWith("pmca_outfit_") && !tag.equals("pmca_outfit_"+chosen))npc.removeTag(tag);
                    npc.addTag("pmca_outfit_"+chosen);
                }
            } catch(NumberFormatException ignored) { /* Valid external clothing IDs are preserved. */ }
        }
    }

    /** Tags contain only a short marker and profile name; all survive MCA conversion and world saves. */
    private static Profile profile(VillagerEntityMCA npc) {
        for (Profile p : Profile.values()) {
            if (npc.entityTags().contains("pmca_" + p.id)) return p;
        }
        return null;
    }

    private static Profile chooseProfile(VillagerEntityMCA npc) {
        int roll = npc.getRandom().nextInt(100);
        for (Profile p : Profile.values()) {
            roll -= p.weight;
            if (roll < 0) return p;
        }
        return Profile.FRIENDLY;
    }

    private static void setup(VillagerEntityMCA npc) {
        Profile p = profile(npc);
        if (p == null) {
            p = chooseProfile(npc);
            npc.addTag("pmca_" + p.id);
        }
        if (npc.entityTags().contains(PiratesOfMCA.READY)) return;
        // Set the real registered profession before locking pirate clothes.
        npc.setProfession(PirateProfession.PIRATE);
        if(flag("outfits"))PirateSpawnEquipment.queueDefaultClothes(npc);
        PirateWearables.applyInitialPreset(npc);
        PirateSpawnEquipment.equipOnce(npc);
        VillagerBrain<?> brain = npc.getVillagerBrain();
        brain.setPersonality(p.personality);
        if (p == Profile.CAPTAIN || p == Profile.BRUISER) {
            brain.modifyMoodValue(p == Profile.CAPTAIN ? -45 : -25);
            // Pirate is a real profession for every crew member; profile governs aggression.
        } else if (p == Profile.GREEDY) {
            brain.modifyMoodValue(-12);
        }
        if (flag("outfits")) {
            // A persistent outfit index survives reloads and identity repairs;
            // never reroll just because MCA reset the clothing tracked value.
            ensureOutfit(npc);
        }
        if (flag("spawn_weapons")) {
            PirateAtelier.equipWeapon(npc);
        }
        PirateSpawnEquipment.finishDefaultClothes(npc);
        PirateOutfits.applyInitialPreset(npc);
        npc.setPersistenceRequired();
        npc.addTag("pmca_atelier_skin_pending");PirateAtelier.skinTick(npc);
        npc.addTag(PiratesOfMCA.READY);
    }

    /** MCA may randomize tracked personality during late initialization.
     *  Repair it for both newly created pirates and v1.0.1 pirates already saved.
     */
    private static void keepProfile(VillagerEntityMCA npc, Profile profile) {
        if(npc.entityTags().contains("pmca_profile_v206"))return;
        VillagerBrain<?> brain = npc.getVillagerBrain();
        if (brain.getPersonality() != profile.personality) {
            brain.setPersonality(profile.personality);
        }
        npc.addTag("pmca_profile_v206");
    }

    /** Applies even in Creative so chat/relationship tests don't appear friendly.
     *  A distinct v1.0.2 marker migrates NPCs previously configured by 1.0.1.
     */
    private static void playerRelationship(VillagerEntityMCA npc, ServerPlayer player,
                                           Profile profile, Combat state) {
        UUID id = player.getUUID();
        Memories memory = npc.getVillagerBrain().getMemoriesForPlayer(player);
        String firstMeet = "pmca_bond_v102_" + id;
        if (!npc.entityTags().contains(firstMeet)) {
            if (flag("initial_relationship_penalty") && memory.getHearts()==0 && memory.getInteractionFatigue()==0) {
                int hearts = memory.getHearts();
                int baseline = profile.initialHearts;
                memory.setHearts(baseline < 0 ? Math.min(hearts, baseline) : Math.max(hearts, baseline));
                memory.setInteractionFatigue(Math.max(memory.getInteractionFatigue(), profile.fatigue));
            }
            npc.addTag(firstMeet);
            state.seenHearts.put(id, memory.getHearts());
            return;
        }

        // Native MCA dialogue still runs. Only the positive relationship *gain*
        // is reduced by profile, leaving bad reactions and gifts meaningful.
        int hearts = memory.getHearts();
        Integer previous = state.seenHearts.get(id);
        if (previous != null && hearts > previous && flag("relationship_resistance")) {
            int gain = hearts - previous;
            int retained = switch (profile) {
                case CAPTAIN -> Math.max(0, gain / 5);
                case BRUISER -> Math.max(0, gain / 4);
                case GREEDY -> Math.max(1, gain / 3);
                case SUSPICIOUS -> Math.max(1, gain / 2);
                case FRIENDLY -> gain;
            };
            if (retained != gain) {
                hearts = previous + retained;
                memory.setHearts(hearts);
            }
        }
        state.seenHearts.put(id, hearts);
    }

    public static void tick(VillagerEntityMCA npc) {
        if (!npc.entityTags().contains(PiratesOfMCA.MARKER)) return;
        if (!(npc.level() instanceof ServerLevel level) || !npc.isAlive()) return;
        if (!loaded) initialize();
        // MCA's vanilla Age (ticks) and custom AgeState must agree. Age:30
        // does NOT mean 30 years; byCurrentAge(0) is ADULT in MCA 8.1.11.
        // Sea-post crews are always adults, including /summon with SNBT.
        if (npc.isBaby() || npc.getAgeState() != AgeState.ADULT) {
            npc.setAge(0);
            if (npc.getAgeState() != AgeState.ADULT) npc.setAgeState(AgeState.ADULT);
            npc.refreshDimensions();
        }
        // Entity type or explicit gender tag is authoritative. Enforce BEFORE
        // sampling a body skin, so the MCA skin pool uses the correct gender.
        CrewRuntime.enforceGender(npc);
        ensureMcaIdentity(npc);
        if (!npc.entityTags().contains(PiratesOfMCA.READY)) setup(npc);
        else if(!npc.entityTags().contains("pmca_atelier_skin_v1") && !npc.entityTags().contains("pmca_atelier_skin_pending"))npc.addTag("pmca_atelier_skin_v1");
        if(npc.tickCount % 20 == 0){PirateSpawnEquipment.finishDefaultClothes(npc);PirateOutfits.repairIfMissing(npc);}
        if (flag("outfits") && npc.tickCount % 20 == 0) ensureOutfit(npc);
        if (npc.tickCount % 10 != 0 || npc.isBaby()) return;
        // One-time migration of old seapost fishermen/leatherworkers (v1.0.x),
        // plus periodic safeguard against job-site reassignment. Do not change
        // ordinary MCA villagers or villagers spawned without the pirate marker.
        if (npc.getProfession() != PirateProfession.PIRATE) {
            npc.setProfession(PirateProfession.PIRATE);
            npc.addTag("pmca_profession_v103");
        }
        // Brain migration from v1: old pirates were classified by MCA as
        // homeless guests and received its flight/panic behaviors. Rebuild
        // the brain once AFTER the profession and adult age are established.
        if (!npc.entityTags().contains("pmca_guard_brain_v2")) {
            npc.addTag("pmca_guard_brain_v2");
            npc.refreshBrain(level);
        }

        Profile profile = profile(npc);
        if (profile == null) return;
        if (npc.tickCount % 20 == 0) keepProfile(npc, profile);
        CrewRuntime.tick(level,npc,profile.id);

        int activeRange = number("activation_range", 14, 4, 48);
        ServerPlayer observer = nearestPlayer(level, npc, activeRange, true);
        long now = level.getGameTime();
        Combat combat = COMBAT.computeIfAbsent(npc, k -> new Combat());
        if (observer != null) playerRelationship(npc, observer, profile, combat);
        // All violent pirate interactions now pass through the hierarchy/social runtime.
        // Do not run v1 random Captain/Bruiser-vs-player or captain-vs-crew fights.
    }

    private static ServerPlayer nearestPlayer(ServerLevel level, VillagerEntityMCA npc,
                                               int max, boolean includeCreative) {
        ServerPlayer nearest = null;
        double best = max * max;
        for (ServerPlayer player : level.players()) {
            if ((!includeCreative && player.isCreative()) || player.isSpectator() || !player.isAlive()) continue;
            double distance = npc.distanceToSqr(player);
            if (distance < best) { best = distance; nearest = player; }
        }
        return nearest;
    }

    private static void begin(Combat state, Entity target, long time, long duration) {
        state.opponent = target.getUUID();
        state.expiry = time + duration;
        state.lastSwing = 0;
    }

    private static boolean resolveCombat(VillagerEntityMCA npc, ServerLevel level, Combat combat, long now) {
        if (combat.opponent == null) return false;
        Entity target = level.getEntity(combat.opponent);
        if (!(target instanceof net.minecraft.world.entity.LivingEntity living)
                || !living.isAlive() || now >= combat.expiry || npc.distanceToSqr(target) > 22 * 22
                || target instanceof ServerPlayer p && (p.isCreative() || p.isSpectator())) {
            end(npc, combat, now);
            return false;
        }
        // Crew quarrels are non-lethal. Keep a substantial safety margin before attacking.
        if (target instanceof VillagerEntityMCA && living.getHealth() <= 14.0f) {
            end(npc, combat, now);
            return false;
        }
        npc.getLookControl().setLookAt(living, 30f, 30f);
        npc.getNavigation().moveTo(living, 1.12);
        if (npc.distanceToSqr(living) <= 5.4 && now - combat.lastSwing >= 22) {
            npc.swing(InteractionHand.MAIN_HAND);
            npc.doHurtTarget(level, living); // MCA's actual melee damage pipeline
            combat.lastSwing = now;
        }
        return true;
    }

    private static void end(VillagerEntityMCA npc, Combat combat, long now) {
        combat.opponent = null;
        combat.restUntil = now + 400 + npc.getRandom().nextInt(300);
        npc.getNavigation().stop();
    }
}
