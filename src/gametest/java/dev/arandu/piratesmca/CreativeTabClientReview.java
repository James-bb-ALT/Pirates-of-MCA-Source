package dev.arandu.piratesmca;

import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.Blocks;

/** Actual creative screen and Fabric's page-aware tab selection API. */
final class CreativeTabClientReview {
    static void expect(boolean value,String message){if(!value)throw new AssertionError(message);}
    void run(ClientGameTestContext context){
        if(Boolean.getBoolean("piratesmca.emfExtensionsReview"))context.runOnClient(mc->{
            for(String id:List.of("emf_compat_core","emf_compat_better_combat","bettercombat","player_animation_library","entity_model_features","entity_texture_features"))expect(FabricLoader.getInstance().isModLoaded(id),"Missing compatibility mod: "+id);
            var packs=mc.getResourceManager().listPacks().map(p->p.packId()).toList();
            for(String name:List.of("FreshAnimations_v1.11.2_26.1+.zip","FA+All_Extensions-v1.10.1.zip","FA+Player-v1.1.zip"))expect(packs.stream().anyMatch(p->p.contains(name)),"Missing active pack: "+name);
            System.out.println("QA_CREATIVE_EMF_EXTENSIONS packs="+packs);
        });
        try(var world=context.worldBuilder().create()){
            world.getServer().runCommand("gamemode creative @a");
            world.getServer().runCommand("tp @a 2048.0 -60.0 -3.5 0 -5");
            world.getServer().runCommand("time set 6000");
            world.getServer().runOnServer(s->{
                for(int i=0;i<2;i++){
                    var egg=i==0?PirateCreativeTab.MALE_SPAWN_EGG:PirateCreativeTab.FEMALE_SPAWN_EGG;
                    var pos=new BlockPos(2047+i*2,-61,0);s.overworld().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
                    var result=egg.useOn(new DirectionalPlaceContext(s.overworld(),pos,Direction.DOWN,new ItemStack(egg),Direction.UP));
                    expect(result.consumesAction(),"Egg item use failed");
                }
            });
            context.waitTicks(60);context.setScreen(()->null);
            var expected=world.getServer().computeOnServer(PirateClientGameTests::identities);
            expect(expected.size()==2 && expected.stream().map(PirateClientGameTests.Identity::gender).distinct().count()==2,"Two egg-spawned genders required");
            context.waitFor(mc->{int count=0;for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && expected.stream().anyMatch(a->a.id().equals(n.getUUID()))){expect(!n.isBaby() && n.getProfession()==PirateProfession.PIRATE,"Egg-spawned NPC lost adulthood/profession");count++;}return count==2;},200);
            context.takeScreenshot("CREATIVE-EGG-NPCS");
            context.setScreen(()->{var mc=net.minecraft.client.Minecraft.getInstance();return new CreativeModeInventoryScreen(mc.player,mc.level.enabledFeatures(),true);});
            context.waitTicks(2);
            context.runOnClient(mc->{
                var screen=(CreativeModeInventoryScreen)mc.gui.screen();
                var tabs=(FabricCreativeModeInventoryScreen)screen;
                tabs.setSelectedTab(PirateCreativeTab.TAB);
                expect(tabs.getSelectedTab()==PirateCreativeTab.TAB,"Fabric did not select addon tab");
                System.out.println("QA_CREATIVE_PAGE page="+(tabs.getCurrentPage()+1)+" count="+tabs.getPageCount());
            });
            context.waitTicks(4);context.takeScreenshot("CREATIVE-TAB");
            context.runOnClient(mc->{
                var screen=(CreativeModeInventoryScreen)mc.gui.screen();
                var visible=screen.getMenu().slots.stream().map(slot->slot.getItem()).filter(stack->!stack.isEmpty()).map(ItemStack::getItem).toList();
                var all=BuiltInRegistries.ITEM.entrySet().stream().filter(e->(e.getKey().identifier().getNamespace().equals(PiratesOfMCA.ID) || PirateAtelier.owns(e.getValue()))).map(Map.Entry::getValue).collect(java.util.stream.Collectors.toSet());
                System.out.println("QA_CREATIVE_VISIBLE_SLOTS "+visible.stream().map(i->BuiltInRegistries.ITEM.getKey(i).toString()).toList());
                expect(visible.containsAll(all),"Creative screen omitted addon item");
                expect(mc.getLanguageManager().getSelected()!=null,"Client language unavailable");
                System.out.println("QA_CREATIVE_TAB_VISIBLE items="+all.stream().map(i->BuiltInRegistries.ITEM.getKey(i).toString()).sorted().toList()+" title="+PirateCreativeTab.TAB.getDisplayName().getString());
            });
            context.setScreen(()->null);
            System.out.println("QA_CREATIVE_COMPLETE eggs=2 adult_genders=2 real_screen=true");
        }
    }
}
