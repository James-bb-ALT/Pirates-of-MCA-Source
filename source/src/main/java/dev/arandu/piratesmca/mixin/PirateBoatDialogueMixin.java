package dev.arandu.piratesmca.mixin;
import dev.arandu.piratesmca.PirateDialogues;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Capture a real hit before the boat's transient damage animation decays. */
@Mixin(VehicleEntity.class)
public abstract class PirateBoatDialogueMixin {
 @Inject(method="hurtServer",at=@At("TAIL"))
 private void piratesmca$boatDamage(ServerLevel level,DamageSource source,float amount,CallbackInfoReturnable<Boolean> cir){
  if(Boolean.TRUE.equals(cir.getReturnValue())&&amount>0&&(Object)this instanceof AbstractBoat boat)PirateDialogues.boatDamaged(level,boat);
 }
}
