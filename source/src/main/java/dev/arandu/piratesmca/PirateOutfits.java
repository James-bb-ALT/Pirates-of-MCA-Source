package dev.arandu.piratesmca;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.resources.SkinVisualIds;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.commands.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;

/** Native MCA clothing IDs; snapshots are persisted in entity tags, never sampled on render. */
public final class PirateOutfits {
    private PirateOutfits(){}
    public static final List<String> IDS=List.of("deckhand");
    private static final Map<Item,String> TOKENS=new HashMap<>();
    private static final String ACTIVE="pmca_look_active_",PREVIOUS="pmca_look_previous_",LOCK="pmca_look_lock_";
    public static String texture(String name){return "piratesmca:outfits/"+name+".png";}
    public static void register(){
        for(String name:IDS){var id=Identifier.fromNamespaceAndPath(PiratesOfMCA.ID,name+"_outfit");var item=Registry.register(BuiltInRegistries.ITEM,id,new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id)).stacksTo(1)));TOKENS.put(item,name);}
        UseEntityCallback.EVENT.register((player,level,hand,entity,hit)->{
            var name=TOKENS.get(player.getItemInHand(hand).getItem());if(name==null)return InteractionResult.PASS;
            if(level.isClientSide())return InteractionResult.SUCCESS;
            if(!(entity instanceof VillagerEntityMCA n) || !(player instanceof ServerPlayer p) || !authorized(p,n)){player.sendSystemMessage(Component.translatable("piratesmca.outfit.denied"));return InteractionResult.FAIL;}
            boolean changed=apply(n,name);p.sendSystemMessage(Component.translatable(changed?"piratesmca.outfit.updated":"piratesmca.outfit.unavailable"));return InteractionResult.SUCCESS;
        });
    }
    static boolean authorized(ServerPlayer p,VillagerEntityMCA n){
        if(n.isBaby() || !n.entityTags().contains(PiratesOfMCA.MARKER) || p.distanceToSqr(n)>36 || !p.hasLineOfSight(n))return false;
        var level=(ServerLevel)p.level();var m=CrewRuntime.ledger(level).member(n.getUUID());var crew=m==null?null:CrewRuntime.ledger(level).crew(m.crew);
        return p.createCommandSourceStack().permissions().hasPermission(Permissions.COMMANDS_MODERATOR) || (crew!=null && crew.playerCaptain && p.getUUID().equals(crew.captain));
    }
    private static String tag(VillagerEntityMCA n,String prefix){return n.entityTags().stream().filter(t->t.startsWith(prefix)).sorted().map(t->t.substring(prefix.length())).findFirst().orElse(null);}
    private static void clearTags(VillagerEntityMCA n){for(var t:new ArrayList<>(n.entityTags()))if(t.startsWith(ACTIVE)||t.startsWith(PREVIOUS)||t.startsWith(LOCK))n.removeTag(t);}
    public static boolean apply(VillagerEntityMCA n,String name){
        if(n.isBaby() || !n.entityTags().contains(PiratesOfMCA.MARKER) || !IDS.contains(name) || !SkinVisualIds.isClothing(texture(name),n.getGenetics().getGender()))return false;
        String old=n.getClothes(),active=tag(n,ACTIVE);if(texture(name).equals(old))return false;
        if(active==null || !texture(active).equals(old)){
            if(!SkinVisualIds.isClothing(old,n.getGenetics().getGender()))return false;
            clearTags(n);n.addTag(PREVIOUS+Base64.getUrlEncoder().withoutPadding().encodeToString(old.getBytes(StandardCharsets.UTF_8)));n.addTag(LOCK+(n.isClothingLocked()?"1":"0"));
        }
        for(var t:new ArrayList<>(n.entityTags()))if(t.startsWith(ACTIVE))n.removeTag(t);
        n.setClothes(texture(name));n.setClothingLocked(true);n.addTag(ACTIVE+name);return true;
    }
    public static boolean restore(VillagerEntityMCA n){
        String active=tag(n,ACTIVE),previous=tag(n,PREVIOUS);if(active==null || previous==null || !IDS.contains(active))return false;
        if(!texture(active).equals(n.getClothes())){clearTags(n);return false;}
        try{
            String old=new String(Base64.getUrlDecoder().decode(previous),StandardCharsets.UTF_8);if(!SkinVisualIds.isClothing(old,n.getGenetics().getGender()))return false;
            boolean locked="1".equals(tag(n,LOCK));n.setClothes(old);n.setClothingLocked(locked);clearTags(n);return true;
        }catch(IllegalArgumentException invalid){return false;}
    }
    static void repairIfMissing(VillagerEntityMCA n){
        String active=tag(n,ACTIVE);if(active==null || !IDS.contains(active))return;
        if(SkinVisualIds.isClothing(n.getClothes(),n.getGenetics().getGender()))return;
        if(SkinVisualIds.isClothing(texture(active),n.getGenetics().getGender())){n.setClothes(texture(active));n.setClothingLocked(true);}
    }
    static void applyInitialPreset(VillagerEntityMCA n){for(String name:IDS)if(n.entityTags().contains("pmca_look_"+name)){apply(n,name);return;}}
    static void commands(com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> root){
        var nearest=Commands.literal("nearest");var actions=new ArrayList<>(IDS);actions.add("restore");
        for(String name:actions)nearest.then(Commands.literal(name).executes(ctx->{
            var p=ctx.getSource().getPlayerOrException();var n=CrewRuntime.nearestPirate((ServerLevel)p.level(),p,6);
            if(n==null || n.isBaby() || p.distanceToSqr(n)>36 || !p.hasLineOfSight(n) || (!authorized(p,n) && !ctx.getSource().permissions().hasPermission(Permissions.COMMANDS_MODERATOR))){ctx.getSource().sendFailure(Component.translatable("piratesmca.outfit.denied"));return 0;}
            boolean changed=name.equals("restore")?restore(n):apply(n,name);
            if(changed)ctx.getSource().sendSuccess(()->Component.translatable("piratesmca.outfit.updated"),false);else ctx.getSource().sendFailure(Component.translatable("piratesmca.outfit.unavailable"));return changed?1:0;
        }));root.then(Commands.literal("outfit").then(nearest));
    }
}
