package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.client.SashRenderData;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(HumanoidRenderState.class)
public abstract class SashStateMixin implements SashRenderData {
    @Unique private final ItemStackRenderState piratesmca$sash=new ItemStackRenderState();
    public ItemStackRenderState piratesmca$sash(){return piratesmca$sash;}
}
