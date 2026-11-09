package dev.arandu.piratesmca;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.registry.EntitiesMCA;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;

public class WearableGameTests {
    static void expect(GameTestHelper h,boolean value,String msg){h.assertTrue(value,Component.literal(msg));}
    static VillagerEntityMCA pirate(GameTestHelper h,boolean female){
        var npc=h.spawn(female?EntitiesMCA.FEMALE_VILLAGER:EntitiesMCA.MALE_VILLAGER,new BlockPos(female?3:1,2,2));
        npc.setAge(0);npc.setNoAi(true);npc.addTag(PiratesOfMCA.MARKER);npc.addTag("pmca_adult_required");return npc;
    }
    @GameTest public void tricorneUsesRealHeadEquipmentWithoutArmorAsset(GameTestHelper h){
        var eq=new ItemStack(PirateWearables.TRICORNE).get(DataComponents.EQUIPPABLE);
        expect(h,eq!=null && eq.slot()==EquipmentSlot.HEAD && eq.assetId().isEmpty(),"HAT-01 must use CustomHeadLayer, not replace armor meshes");h.succeed();
    }
    @GameTest(maxTicks=100) public void presetPreservesGenderIdentityAndExistingHelmets(GameTestHelper h){
        var male=pirate(h,false);var female=pirate(h,true);male.addTag(PirateWearables.PRESET);female.addTag(PirateWearables.PRESET);
        h.runAtTickTime(40,()->{
            for(var n:java.util.List.of(male,female)){
                expect(h,n.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE),"HAT-02 initial preset not equipped");
                var skin=n.getSkin();var clothes=n.getClothes();var hair=n.getHairStyleId();var gender=n.getGenetics().getGender();var name=n.getName().getString();
                expect(h,PirateWearables.clear(n) && !n.entityTags().contains(PirateWearables.PRESET),"HAT-03 unequip failed");
                expect(h,PirateWearables.equip(n),"HAT-03 re-equip failed");
                expect(h,skin.equals(n.getSkin()) && clothes.equals(n.getClothes()) && hair.equals(n.getHairStyleId()) && gender==n.getGenetics().getGender() && name.equals(n.getName().getString()),"HAT-02 equipment changed MCA identity");
                n.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));
                expect(h,!PirateWearables.equip(n) && !PirateWearables.clear(n) && n.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET),"HAT-03 overwrote another helmet");
            }
            h.succeed();
        });
    }
    @GameTest(maxTicks=80) public void equipmentAndPresetSurviveNativeEntitySaveLoad(GameTestHelper h){
        var npc=pirate(h,false);npc.addTag(PirateWearables.PRESET);
        h.runAtTickTime(40,()->{
            var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());npc.saveWithoutId(output);
            var copy=EntitiesMCA.MALE_VILLAGER.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.LOAD);
            copy.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),output.buildResult()));
            expect(h,copy.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE) && copy.entityTags().contains(PirateWearables.PRESET),"HAT-04 native NBT lost item/preset");
            expect(h,copy.getSkin().equals(npc.getSkin()) && copy.getClothes().equals(npc.getClothes()) && copy.getHairStyleId().equals(npc.getHairStyleId()),"HAT-04 load changed appearance");h.succeed();
        });
    }
    @GameTest(maxTicks=80) public void ordinaryChildDoesNotReceivePiratePreset(GameTestHelper h){
        var npc=h.spawn(EntitiesMCA.FEMALE_VILLAGER,new BlockPos(1,2,1));npc.setAge(-24000);npc.setNoAi(true);npc.addTag(PirateWearables.PRESET);
        h.runAtTickTime(40,()->{expect(h,npc.isBaby() && npc.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && !PirateWearables.equip(npc),"HAT-02 ordinary child was changed");h.succeed();});
    }
}
