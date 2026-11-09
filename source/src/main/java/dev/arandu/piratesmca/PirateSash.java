package dev.arandu.piratesmca;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.Equippable;

/** Cosmetic waist loadout currently mapped to native CHEST; never replaces armor. */
public final class PirateSash {
    private PirateSash(){}
    public static final String PRESET="pmca_preset_sash_red";
    public static Item ITEM;
    public static void register(){
        var id=Identifier.fromNamespaceAndPath(PiratesOfMCA.ID,"sash_red");
        ITEM=Registry.register(BuiltInRegistries.ITEM,id,new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id)).stacksTo(1)
            .component(DataComponents.EQUIPPABLE,Equippable.builder(EquipmentSlot.CHEST).setDamageOnHurt(false).build())));
    }
    public static boolean equip(VillagerEntityMCA n){
        if(n.isBaby() || !n.entityTags().contains(PiratesOfMCA.MARKER) || !n.getItemBySlot(EquipmentSlot.CHEST).isEmpty())return false;
        n.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ITEM));n.addTag(PRESET);return true;
    }
    public static boolean clear(VillagerEntityMCA n){
        if(!n.getItemBySlot(EquipmentSlot.CHEST).is(ITEM))return false;
        n.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);n.removeTag(PRESET);return true;
    }
    static void applyInitialPreset(VillagerEntityMCA n){if(n.entityTags().contains(PRESET))equip(n);}
}
