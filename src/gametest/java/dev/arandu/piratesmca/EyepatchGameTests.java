package dev.arandu.piratesmca;
import net.conczin.mca.registry.EntitiesMCA;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.*;

public class EyepatchGameTests {
    @GameTest(maxTicks=80) public void independentHeadAndFacePreserveIdentity(GameTestHelper h){
        var n=WearableGameTests.pirate(h,false);
        h.runAtTickTime(40,()->{
            // This test starts with an explicitly empty head; spawn is now random.
            PirateWearables.clear(n);n.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
            var skin=n.getSkin();var hair=n.getHairStyleId();var clothes=n.getClothes();var gender=n.getGenetics().getGender();var name=n.getName().getString();
            WearableGameTests.expect(h,PirateWearables.equipEyepatch(n) && n.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.EYEPATCH),"EYE-01 patch alone");
            WearableGameTests.expect(h,PirateWearables.equip(n) && n.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE_EYEPATCH),"EYE-01 hat must preserve face slot");
            WearableGameTests.expect(h,PirateWearables.clearEyepatch(n) && n.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE),"EYE-01 removing face must preserve hat");
            WearableGameTests.expect(h,PirateWearables.equipEyepatch(n) && n.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE_EYEPATCH),"EYE-01 face must preserve hat");
            WearableGameTests.expect(h,!PirateWearables.equipEyepatch(n),"EYE-01 already equipped must not duplicate");
            WearableGameTests.expect(h,skin.equals(n.getSkin()) && hair.equals(n.getHairStyleId()) && clothes.equals(n.getClothes()) && gender==n.getGenetics().getGender() && name.equals(n.getName().getString()),"EYE-02 MCA identity changed");h.succeed();
        });
    }
    @GameTest(maxTicks=80) public void compositePersistsWithoutReequippingOnTick(GameTestHelper h){
        var n=WearableGameTests.pirate(h,true);n.addTag(PirateWearables.PRESET);n.addTag(PirateWearables.FACE_PRESET);
        h.runAtTickTime(40,()->{
            WearableGameTests.expect(h,n.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE_EYEPATCH),"EYE-03 initial combined preset");
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());n.saveWithoutId(out);
            var copy=EntitiesMCA.FEMALE_VILLAGER.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.LOAD);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
            WearableGameTests.expect(h,copy.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE_EYEPATCH) && copy.entityTags().contains(PirateWearables.PRESET) && copy.entityTags().contains(PirateWearables.FACE_PRESET),"EYE-03 native save lost loadout");
            WearableGameTests.expect(h,PirateWearables.clear(n),"EYE-03 clear failed");
        });
        h.runAtTickTime(60,()->{WearableGameTests.expect(h,n.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && !n.entityTags().contains(PirateWearables.FACE_PRESET),"EYE-03 item re-equipped after clear");h.succeed();});
    }
    @GameTest(maxTicks=80) public void foreignHelmetsAndOrdinaryChildrenAreUntouched(GameTestHelper h){
        var n=WearableGameTests.pirate(h,false);n.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
        WearableGameTests.expect(h,!PirateWearables.equipEyepatch(n) && !PirateWearables.clearEyepatch(n) && n.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET),"EYE-04 replaced real armor");
        var child=h.spawn(EntitiesMCA.FEMALE_VILLAGER,new BlockPos(3,2,2));child.setAge(-24000);child.setNoAi(true);child.addTag(PirateWearables.FACE_PRESET);
        h.runAtTickTime(40,()->{WearableGameTests.expect(h,child.isBaby() && child.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && !PirateWearables.equipEyepatch(child),"EYE-04 changed ordinary child");h.succeed();});
    }
}
