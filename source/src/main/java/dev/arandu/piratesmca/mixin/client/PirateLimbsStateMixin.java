package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.client.PirateLimbsData;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(HumanoidRenderState.class)
public abstract class PirateLimbsStateMixin implements PirateLimbsData {
 @Unique private final ItemStackRenderState[] piratesmca$limbs={new ItemStackRenderState(),new ItemStackRenderState(),new ItemStackRenderState(),new ItemStackRenderState()};
 @Unique private boolean piratesmca$wardrobe;
 public boolean piratesmca$wardrobeEnabled(){return piratesmca$wardrobe;}
 public void piratesmca$wardrobeEnabled(boolean enabled){piratesmca$wardrobe=enabled;}
 public ItemStackRenderState[] piratesmca$limbs(){return piratesmca$limbs;}
}
