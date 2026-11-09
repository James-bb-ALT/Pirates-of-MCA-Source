package dev.arandu.piratesmca;
import net.conczin.mca.registry.EntitiesMCA;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.storage.*;

public class SashGameTests {
    @GameTest public void sashUsesChestWithoutArmorAssetOrDefense(GameTestHelper h){
        var stack=new ItemStack(PirateSash.ITEM);var eq=stack.get(DataComponents.EQUIPPABLE);
        WearableGameTests.expect(h,eq!=null && eq.slot()==EquipmentSlot.CHEST && eq.assetId().isEmpty(),"SASH-01 must not replace vanilla armor meshes");
        WearableGameTests.expect(h,stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY).modifiers().isEmpty(),"SASH-01 cosmetic gained defense");h.succeed();
    }
    @GameTest(maxTicks=90) public void sashPreservesHeadIdentityAndPersists(GameTestHelper h){
        var n=WearableGameTests.pirate(h,true);n.addTag(PirateWearables.PRESET);n.addTag(PirateWearables.FACE_PRESET);n.addTag(PirateSash.PRESET);
        h.runAtTickTime(40,()->{
            var skin=n.getSkin();var hair=n.getHairStyleId();var clothes=n.getClothes();var name=n.getName().getString();var gender=n.getGenetics().getGender();
            WearableGameTests.expect(h,n.getItemBySlot(EquipmentSlot.CHEST).is(PirateSash.ITEM) && n.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE_EYEPATCH),"SASH-02 composed preset failed");
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());n.saveWithoutId(out);
            var copy=EntitiesMCA.FEMALE_VILLAGER.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.LOAD);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
            WearableGameTests.expect(h,copy.getItemBySlot(EquipmentSlot.CHEST).is(PirateSash.ITEM) && copy.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE_EYEPATCH) && copy.entityTags().contains(PirateSash.PRESET),"SASH-03 native NBT lost equipment");
            WearableGameTests.expect(h,PirateSash.clear(n) && PirateSash.equip(n) && !PirateSash.equip(n),"SASH-02 equip/clear duplicated or failed");
            WearableGameTests.expect(h,skin.equals(n.getSkin()) && hair.equals(n.getHairStyleId()) && clothes.equals(n.getClothes()) && name.equals(n.getName().getString()) && gender==n.getGenetics().getGender(),"SASH-02 changed MCA identity");
            PirateSash.clear(n);
        });
        h.runAtTickTime(65,()->{WearableGameTests.expect(h,n.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && !n.entityTags().contains(PirateSash.PRESET) && n.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE_EYEPATCH),"SASH-03 re-equipped on tick or removed head");h.succeed();});
    }
    @GameTest(maxTicks=80) public void sashNeverOverwritesArmorOrEquipsOrdinaryChildren(GameTestHelper h){
        var n=WearableGameTests.pirate(h,false);n.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.IRON_CHESTPLATE));
        WearableGameTests.expect(h,!PirateSash.equip(n) && !PirateSash.clear(n) && n.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE),"SASH-04 replaced chest armor");
        var child=h.spawn(EntitiesMCA.FEMALE_VILLAGER,new BlockPos(3,2,2));child.setAge(-24000);child.setNoAi(true);child.addTag(PirateSash.PRESET);
        h.runAtTickTime(40,()->{WearableGameTests.expect(h,child.isBaby() && child.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && !PirateSash.equip(child),"SASH-04 changed ordinary child");h.succeed();});
    }
}
