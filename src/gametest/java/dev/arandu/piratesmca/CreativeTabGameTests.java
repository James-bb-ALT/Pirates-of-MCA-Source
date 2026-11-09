package dev.arandu.piratesmca;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.registry.EntitiesMCA;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.*;

public class CreativeTabGameTests {
    static void expect(GameTestHelper h,boolean value,String message){h.assertTrue(value,Component.literal(message));}
    @GameTest public void tabContainsEveryAddonItemExactlyOnce(GameTestHelper h){
        var tab=PirateCreativeTab.TAB;
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(h.getLevel().enabledFeatures(),false,h.getLevel().registryAccess()));
        var expected=BuiltInRegistries.ITEM.entrySet().stream().filter(e->(e.getKey().identifier().getNamespace().equals(PiratesOfMCA.ID) || PirateAtelier.owns(e.getValue()))).map(java.util.Map.Entry::getValue).collect(java.util.stream.Collectors.toSet());
        var actual=tab.getDisplayItems().stream().map(ItemStack::getItem).toList();
        expect(h,actual.size()==expected.size() && new java.util.HashSet<>(actual).equals(expected),"CREATIVE-01 missing or duplicate addon items");
        expect(h,actual.containsAll(java.util.List.of(PirateFlag.ITEM,PirateWearables.TRICORNE,PirateCreativeTab.MALE_SPAWN_EGG,PirateCreativeTab.FEMALE_SPAWN_EGG)),"CREATIVE-01 required items absent");h.succeed();
    }
    static VillagerEntityMCA useEgg(GameTestHelper h,Item egg,BlockPos local){
        var pos=h.absolutePos(local);h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
        var stack=new ItemStack(egg);
        var result=egg.useOn(new DirectionalPlaceContext(h.getLevel(),pos,Direction.DOWN,stack,Direction.UP));
        expect(h,result.consumesAction() && stack.isEmpty(),"CREATIVE-02 egg use failed or was not consumed");
        var npcs=h.getLevel().getEntitiesOfClass(VillagerEntityMCA.class,new AABB(pos).inflate(1,3,1));
        expect(h,npcs.size()==1,"CREATIVE-02 egg must spawn one MCA NPC");var n=npcs.getFirst();n.setNoAi(true);
        expect(h,n.entityTags().contains(PiratesOfMCA.MARKER) && !n.isBaby(),"CREATIVE-02 pirate marker/adult age missing before first tick");return n;
    }
    @GameTest(maxTicks=80,padding=16) public void eggsSpawnAdultPiratesOfBothGenders(GameTestHelper h){
        var male=useEgg(h,PirateCreativeTab.MALE_SPAWN_EGG,new BlockPos(1,1,1));
        var female=useEgg(h,PirateCreativeTab.FEMALE_SPAWN_EGG,new BlockPos(4,1,1));
        h.runAtTickTime(40,()->{
            expect(h,male.getGenetics().getGender()==Gender.MALE && female.getGenetics().getGender()==Gender.FEMALE,"CREATIVE-02 egg gender ignored");
            for(var n:java.util.List.of(male,female))expect(h,!n.isBaby() && n.getProfession()==PirateProfession.PIRATE && !n.getSkin().isBlank() && !n.getClothes().isBlank() && !n.getName().getString().isBlank(),"CREATIVE-02 identity/profession not initialized");h.succeed();
        });
    }
    @GameTest(maxTicks=80,padding=16) public void eggUsedOnAnAdultDoesNotLeaveAChild(GameTestHelper h){
        var parent=h.spawn(EntitiesMCA.FEMALE_VILLAGER,new BlockPos(1,2,1));parent.setAge(0);parent.setNoAi(true);
        var child=SpawnEggItem.spawnOffspringFromSpawnEgg(h.makeMockServerPlayerInLevel(),parent,EntitiesMCA.FEMALE_VILLAGER,h.getLevel(),Vec3.atCenterOf(h.absolutePos(new BlockPos(3,2,1))),new ItemStack(PirateCreativeTab.FEMALE_SPAWN_EGG)).orElseThrow();
        child.setNoAi(true);
        expect(h,!child.isBaby() && child.entityTags().contains(PiratesOfMCA.MARKER),"CREATIVE-03 entity interaction bypassed adult egg data");
        h.runAtTickTime(40,()->{expect(h,child instanceof VillagerEntityMCA n && !n.isBaby() && n.getGenetics().getGender()==Gender.FEMALE && n.getProfession()==PirateProfession.PIRATE,"CREATIVE-03 offspring path lost pirate identity");h.succeed();});
    }
    @GameTest(maxTicks=80,padding=16) public void dispenserPreservesPirateEggData(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,2,2));
        h.getLevel().setBlockAndUpdate(pos,Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.EAST));
        var dispenser=(DispenserBlockEntity)h.getLevel().getBlockEntity(pos);dispenser.setItem(0,new ItemStack(PirateCreativeTab.MALE_SPAWN_EGG));
        h.getLevel().setBlockAndUpdate(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState());
        h.runAtTickTime(40,()->{
            var npcs=h.getLevel().getEntitiesOfClass(VillagerEntityMCA.class,new AABB(pos.east()).inflate(3));
            expect(h,npcs.size()==1 && dispenser.getItem(0).isEmpty(),"CREATIVE-04 dispenser did not spawn exactly one NPC");
            var n=npcs.getFirst();expect(h,n.entityTags().contains(PiratesOfMCA.MARKER) && !n.isBaby() && n.getGenetics().getGender()==Gender.MALE && n.getProfession()==PirateProfession.PIRATE,"CREATIVE-04 dispenser lost entity data");h.succeed();
        });
    }
    @GameTest(padding=16) public void vanillaEggStillCreatesAnOrdinaryChild(GameTestHelper h){
        var parent=h.spawn(net.minecraft.world.entity.EntityTypes.VILLAGER,new BlockPos(1,2,1));parent.setAge(0);parent.setNoAi(true);
        var child=SpawnEggItem.spawnOffspringFromSpawnEgg(h.makeMockServerPlayerInLevel(),parent,net.minecraft.world.entity.EntityTypes.VILLAGER,h.getLevel(),Vec3.atCenterOf(h.absolutePos(new BlockPos(3,2,1))),new ItemStack(Items.VILLAGER_SPAWN_EGG)).orElseThrow();
        expect(h,child.isBaby() && !child.entityTags().contains(PiratesOfMCA.MARKER),"CREATIVE-05 vanilla egg behavior changed");h.succeed();
    }
}
