package dev.arandu.piratesmca.mixin;
import dev.arandu.piratesmca.PiratesOfMCA;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.brain.tasks.EquipmentTask;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Keep guard AI, prevent its equipment task from reissuing MCA guard armor/weapons. */
@Mixin(value=EquipmentTask.class,remap=false)
public abstract class PirateEquipmentTaskMixin {
 @Inject(method="checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/conczin/mca/entity/VillagerEntityMCA;)Z",at=@At("HEAD"),cancellable=true,remap=false)
 private void piratesmca$ownLoadout(ServerLevel level,VillagerEntityMCA npc,CallbackInfoReturnable<Boolean> cir){
  if(npc.entityTags().contains(PiratesOfMCA.MARKER))cir.setReturnValue(false);
 }
}
