package dev.arandu.piratesmca.mixin;

import java.util.Optional;
import dev.arandu.piratesmca.PirateCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vanilla offspring bypasses ENTITY_DATA; adult pirate eggs use normal item spawning. */
@Mixin(SpawnEggItem.class)
public abstract class PirateSpawnEggMixin {
    @Inject(method="spawnOffspringFromSpawnEgg",at=@At("HEAD"),cancellable=true)
    private static void piratesmca$adultEgg(Player player,Mob parent,EntityType<? extends Mob> type,ServerLevel level,Vec3 position,ItemStack stack,CallbackInfoReturnable<Optional<Mob>> cir){
        if(!stack.is(PirateCreativeTab.MALE_SPAWN_EGG) && !stack.is(PirateCreativeTab.FEMALE_SPAWN_EGG))return;
        if(!SpawnEggItem.spawnsEntity(stack,type)){cir.setReturnValue(Optional.empty());return;}
        var npc=type.spawn(level,stack,player,BlockPos.containing(position),EntitySpawnReason.SPAWN_ITEM_USE,false,false);
        if(npc!=null)stack.consume(1,player);
        cir.setReturnValue(Optional.ofNullable(npc));
    }
}
