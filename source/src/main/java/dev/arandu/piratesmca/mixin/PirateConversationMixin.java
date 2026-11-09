package dev.arandu.piratesmca.mixin;
import dev.arandu.piratesmca.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.interaction.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

/** Observe completed native MCA actions, never replace their trade/gift mechanics. */
@Mixin(value=VillagerCommandHandler.class,remap=false)
public abstract class PirateConversationMixin extends EntityCommandHandler<VillagerEntityMCA> {
 protected PirateConversationMixin(VillagerEntityMCA entity){super(entity);}
 @WrapMethod(method="handle")
 private boolean piratesmca$completedInteraction(ServerPlayer player,String command,Operation<Boolean> original){
  boolean pirate=PirateDialogues.eligible(entity);var before=player.getMainHandItem().copy();int hearts=pirate?entity.getVillagerBrain().getMemoriesForPlayer(player).getHearts():0;
  boolean result=original.call(player,command);
  if(pirate&&PirateDialogues.eligible(entity)){
   if(command.equals("trade")&&player.containerMenu instanceof MerchantMenu)PirateDialogues.trade(entity,player);
   if(result&&command.equals("gift")&&!before.isEmpty()&&(player.getMainHandItem().getCount()<before.getCount()||entity.getVillagerBrain().getMemoriesForPlayer(player).getHearts()!=hearts))PirateDialogues.gift(entity,player,before);
  }return result;
 }
}
