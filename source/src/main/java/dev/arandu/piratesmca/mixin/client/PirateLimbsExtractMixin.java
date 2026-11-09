package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.*;
import dev.arandu.piratesmca.client.PirateLimbsData;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
public abstract class PirateLimbsExtractMixin {
 @Shadow @Final protected ItemModelResolver itemModelResolver;
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
 private void piratesmca$extract(LivingEntity n,LivingEntityRenderState state,float delta,CallbackInfo ci){
  if(!(state instanceof PirateLimbsData data))return;var parts=data.piratesmca$limbs();for(var part:parts)part.clear();
  data.piratesmca$wardrobeEnabled(!n.isBaby()&&(n instanceof net.conczin.mca.entity.VillagerEntityMCA || n instanceof net.minecraft.world.entity.player.Player));
  if(n.isBaby() || !(n instanceof net.conczin.mca.entity.VillagerEntityMCA || n instanceof net.minecraft.world.entity.player.Player))return;
  for(var slot:java.util.List.of(EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND)){
   var stack=n.getItemBySlot(slot);var e=PirateAtelier.ITEMS.get(stack.getItem());if(e==null || !e.kind().equals("hook"))continue;
   boolean left=(n.getMainArm()==HumanoidArm.LEFT)==(slot==EquipmentSlot.MAINHAND);var shown=stack.copy();var id=Identifier.parse(e.id());shown.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,Identifier.fromNamespaceAndPath(id.getNamespace(),"attachments/"+id.getPath()+(left?"_left":"")));itemModelResolver.updateForLiving(parts[left?0:1],shown,ItemDisplayContext.FIXED,n);
  }
  String leggings=BuiltInRegistries.ITEM.getKey(n.getItemBySlot(EquipmentSlot.LEGS).getItem()).toString();var legEntry=PirateAtelier.ITEMS.get(n.getItemBySlot(EquipmentSlot.LEGS).getItem());var feet=n.getItemBySlot(EquipmentSlot.FEET);var e=PirateAtelier.ITEMS.get(feet.getItem());
  boolean left=legEntry!=null&&legEntry.kind().equals("armor")&&leggings.contains("_peg_left_"),right=legEntry!=null&&legEntry.kind().equals("armor")&&leggings.contains("_peg_right_");
  if(!left && !right&&e!=null&&e.kind().equals("armor")){String bootId=BuiltInRegistries.ITEM.getKey(feet.getItem()).toString();left=bootId.contains("_peg_left_");right=bootId.contains("_peg_right_");}
  if(e!=null && e.kind().equals("peg")){if(!left && !right)right=true;}
  else feet=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("pirateatelier:atelier_peg_leg_wood")));
  if(left || right){var shown=feet.copy();var id=BuiltInRegistries.ITEM.getKey(feet.getItem());shown.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,Identifier.fromNamespaceAndPath(id.getNamespace(),"attachments/"+id.getPath()));itemModelResolver.updateForLiving(parts[left?2:3],shown,ItemDisplayContext.FIXED,n);}
 }
}
