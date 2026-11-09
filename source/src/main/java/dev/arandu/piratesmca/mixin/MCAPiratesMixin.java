package dev.arandu.piratesmca.mixin;

import dev.arandu.piratesmca.CrewRuntime;
import dev.arandu.piratesmca.PirateBehavior;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;

@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class MCAPiratesMixin {
    @Inject(method="die",at=@At("HEAD"),remap=false)
    private void piratesmca$atelierLoot(DamageSource source,CallbackInfo ci){dev.arandu.piratesmca.PirateAtelier.dropLoot((VillagerEntityMCA)(Object)this,source);}
    @WrapMethod(method="doHurtTarget")
    private boolean piratesmca$combatContext(ServerLevel level,Entity target,Operation<Boolean> original) {
        return CrewRuntime.attackWithContext((VillagerEntityMCA)(Object)this,target,()->original.call(level,target));
    }
    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void piratesmca$tick(CallbackInfo ci) {
        var npc=(VillagerEntityMCA)(Object)this;PirateBehavior.tick(npc);
        dev.arandu.piratesmca.PirateAtelier.skinTick(npc);
        if(npc.level() instanceof ServerLevel level)CrewRuntime.enforceMovementOverride(level,npc);
    }
    /** MCA chooses guard combat/hostile targeting instead of regular villager panic for pirates.
     *  Guard logic still retreats when seriously wounded (25% health).
     */
    @Inject(method="isGuard",at=@At("HEAD"),cancellable=true,remap=false)
    private void piratesmca$guardBrain(CallbackInfoReturnable<Boolean> cir) {
        VillagerEntityMCA npc=(VillagerEntityMCA)(Object)this;
        if(CrewRuntime.isPirate(npc))cir.setReturnValue(true);
    }
    /** Only pirate-on-pirate BRAWL damage is capped. Natural sources still work. */
    @ModifyVariable(method="hurtServer",at=@At("HEAD"),argsOnly=true,ordinal=0,remap=false)
    private float piratesmca$nonlethalBrawl(float original,ServerLevel level,DamageSource source,float amount) {
        return CrewRuntime.scaleBrawlDamage((VillagerEntityMCA)(Object)this,source,original);
    }
    @Inject(method="hurtServer",at=@At("HEAD"),cancellable=true,remap=false)
    private void piratesmca$rejectFriendlyFire(ServerLevel level,DamageSource source,float amount,CallbackInfoReturnable<Boolean> cir) {
        if(source.getEntity() instanceof VillagerEntityMCA attacker
                && !CrewRuntime.mayAttack(attacker,(VillagerEntityMCA)(Object)this))cir.setReturnValue(false);
    }
    @Inject(method="hurtServer",at=@At("TAIL"),remap=false)
    private void piratesmca$recordAssault(ServerLevel level,DamageSource source,float amount,CallbackInfoReturnable<Boolean> cir) {
        if(Boolean.TRUE.equals(cir.getReturnValue()))
            CrewRuntime.afterDamage(level,(VillagerEntityMCA)(Object)this,source.getEntity(),amount);
    }
}
