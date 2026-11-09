package dev.arandu.piratesmca;

import net.conczin.mca.registry.EntitiesMCA;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TypedEntityData;

/** Namespace-based contents also include future registered addon items. */
public final class PirateCreativeTab {
    private PirateCreativeTab(){}
    public static final Identifier ID=Identifier.fromNamespaceAndPath(PiratesOfMCA.ID,"main");
    public static CreativeModeTab TAB;
    public static Item MALE_SPAWN_EGG,FEMALE_SPAWN_EGG;

    public static void register(){
        MALE_SPAWN_EGG=egg("male_pirate_spawn_egg",EntitiesMCA.MALE_VILLAGER,"male");
        FEMALE_SPAWN_EGG=egg("female_pirate_spawn_egg",EntitiesMCA.FEMALE_VILLAGER,"female");
        TAB=Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,ID,FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.piratesmca.main"))
            .icon(()->new ItemStack(PirateWearables.TRICORNE))
            .displayItems((parameters,output)->{
                output.accept(PirateFlag.ITEM);output.accept(PirateWearables.TRICORNE);
                BuiltInRegistries.ITEM.entrySet().stream()
                    .filter(e->(e.getKey().identifier().getNamespace().equals(PiratesOfMCA.ID) || PirateAtelier.owns(e.getValue())))
                    .sorted(java.util.Comparator.comparing(e->e.getKey().identifier().toString()))
                    .map(java.util.Map.Entry::getValue)
                    .filter(item->item!=PirateFlag.ITEM && item!=PirateWearables.TRICORNE)
                    .forEach(output::accept);
            }).build());
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.withDefaultNamespace("spawn_eggs")))
            .register(output->{output.accept(MALE_SPAWN_EGG);output.accept(FEMALE_SPAWN_EGG);});
    }

    private static Item egg(String name,EntityType<?> type,String gender){
        var id=Identifier.fromNamespaceAndPath(PiratesOfMCA.ID,name);
        var tags=new ListTag();
        for(String tag:java.util.List.of(PiratesOfMCA.MARKER,"pmca_adult_required","pmca_gender_"+gender))tags.add(StringTag.valueOf(tag));
        var data=new CompoundTag();data.putInt("Age",0);data.put("Tags",tags);
        return Registry.register(BuiltInRegistries.ITEM,id,new SpawnEggItem(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM,id)).spawnEgg(type)
            .component(DataComponents.ENTITY_DATA,TypedEntityData.of(type,data))));
    }
}
