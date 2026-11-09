package dev.arandu.piratesmca;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.commands.*;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.Equippable;

/** Head/face loadouts use native equipment NBT; no re-equip on tick. */
public final class PirateWearables {
    private PirateWearables(){}
    public static final String PRESET="pmca_preset_tricorne_classic";
    public static Item TRICORNE;
    public static Item EYEPATCH,TRICORNE_EYEPATCH;
    public static final String FACE_PRESET="pmca_preset_eyepatch_leather";
    public static void register(){
        TRICORNE=registerHead("tricorne_classic");EYEPATCH=registerHead("eyepatch_leather");TRICORNE_EYEPATCH=registerHead("tricorne_eyepatch");
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.withDefaultNamespace("combat"))).register(entries->{entries.accept(TRICORNE);entries.accept(EYEPATCH);entries.accept(TRICORNE_EYEPATCH);});
    }
    private static Item registerHead(String name){var id=Identifier.fromNamespaceAndPath(PiratesOfMCA.ID,name);return Registry.register(BuiltInRegistries.ITEM,id,new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id)).stacksTo(1).component(DataComponents.EQUIPPABLE,Equippable.builder(EquipmentSlot.HEAD).setDamageOnHurt(false).build())));}
    public static boolean isCosmetic(ItemStack stack){return stack.is(TRICORNE)||stack.is(EYEPATCH)||stack.is(TRICORNE_EYEPATCH);}
    private static boolean change(VillagerEntityMCA npc,boolean hat,boolean patch){
        if(npc.isBaby() || !npc.entityTags().contains(PiratesOfMCA.MARKER))return false;
        var old=npc.getItemBySlot(EquipmentSlot.HEAD);if(!old.isEmpty() && !isCosmetic(old))return false;
        Item target=hat?(patch?TRICORNE_EYEPATCH:TRICORNE):(patch?EYEPATCH:null);
        if(target==null?old.isEmpty():old.is(target))return false;
        npc.setItemSlot(EquipmentSlot.HEAD,target==null?ItemStack.EMPTY:new ItemStack(target));
        if(hat)npc.addTag(PRESET);else npc.removeTag(PRESET);
        if(patch)npc.addTag(FACE_PRESET);else npc.removeTag(FACE_PRESET);
        return true;
    }
    public static boolean equip(VillagerEntityMCA npc){
        var old=npc.getItemBySlot(EquipmentSlot.HEAD);return change(npc,true,old.is(EYEPATCH)||old.is(TRICORNE_EYEPATCH));
    }
    public static boolean equipEyepatch(VillagerEntityMCA npc){var old=npc.getItemBySlot(EquipmentSlot.HEAD);return change(npc,old.is(TRICORNE)||old.is(TRICORNE_EYEPATCH),true);}
    public static boolean clearEyepatch(VillagerEntityMCA npc){var old=npc.getItemBySlot(EquipmentSlot.HEAD);if(!old.is(EYEPATCH)&&!old.is(TRICORNE_EYEPATCH))return false;return change(npc,old.is(TRICORNE_EYEPATCH),false);}
    public static boolean clear(VillagerEntityMCA npc){
        if(!isCosmetic(npc.getItemBySlot(EquipmentSlot.HEAD)))return false;return change(npc,false,false);
    }
    static void applyInitialPreset(VillagerEntityMCA npc){boolean hat=npc.entityTags().contains(PRESET),patch=npc.entityTags().contains(FACE_PRESET);if(hat)equip(npc);if(patch)equipEyepatch(npc);PirateSash.applyInitialPreset(npc);}
    static void commands(com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> root){
        var nearest=Commands.literal("nearest");
        for(String action:java.util.List.of("tricorne_classic","eyepatch_leather","eyepatch_clear","sash_red","sash_clear","clear"))nearest.then(Commands.literal(action).executes(ctx->{
            var src=ctx.getSource();var player=src.getPlayerOrException();var level=(ServerLevel)player.level();
            var npc=CrewRuntime.nearestPirate(level,player,6);
            if(npc==null || !player.hasLineOfSight(npc))return 0;
            var m=CrewRuntime.ledger(level).member(npc.getUUID());var crew=m==null?null:CrewRuntime.ledger(level).crew(m.crew);
            boolean owner=crew!=null && crew.playerCaptain && player.getUUID().equals(crew.captain);
            if(!owner && !src.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)){src.sendFailure(Component.translatable("piratesmca.wearable.denied"));return 0;}
            boolean changed=switch(action){case "clear"->clear(npc);case "eyepatch_clear"->clearEyepatch(npc);case "eyepatch_leather"->equipEyepatch(npc);case "sash_red"->PirateSash.equip(npc);case "sash_clear"->PirateSash.clear(npc);default->equip(npc);};
            if(changed)src.sendSuccess(()->Component.translatable("piratesmca.wearable.updated",npc.getName()),false);
            else src.sendFailure(Component.translatable("piratesmca.wearable.occupied"));
            return changed?1:0;
        }));
        root.then(Commands.literal("preset").then(nearest));
    }
}
