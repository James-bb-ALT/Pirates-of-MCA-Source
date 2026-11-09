package dev.arandu.piratesmca.mixin.client;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.arandu.piratesmca.client.WardrobeGeometry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Suppress vanilla cubes only when that exact equipped item has an active custom mesh. */
@Mixin(HumanoidArmorLayer.class)
public abstract class WardrobeArmorMixin {
 @Inject(method="renderArmorPiece",at=@At("HEAD"),cancellable=true)
 private void piratesmca$customEquipment(PoseStack pose,SubmitNodeCollector nodes,ItemStack item,EquipmentSlot slot,int light,HumanoidRenderState state,CallbackInfo ci){if(state instanceof dev.arandu.piratesmca.client.PirateLimbsData data&&data.piratesmca$wardrobeEnabled()&&WardrobeGeometry.get(item)!=null)ci.cancel();}
}
