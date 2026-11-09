package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.client.PirateLimbsData;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandLayer.class)
public abstract class PirateHookHeldMixin {
 @Inject(method="submitArmWithItem",at=@At("HEAD"),cancellable=true)
 private void piratesmca$oneHook(ArmedEntityRenderState s,ItemStackRenderState item,ItemStack stack,HumanoidArm arm,PoseStack p,SubmitNodeCollector n,int light,CallbackInfo ci){if(s instanceof PirateLimbsData data && !data.piratesmca$limbs()[arm==HumanoidArm.LEFT?0:1].isEmpty())ci.cancel();}
}
