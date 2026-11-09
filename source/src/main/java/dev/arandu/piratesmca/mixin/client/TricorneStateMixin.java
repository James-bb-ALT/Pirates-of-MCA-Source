package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.client.PirateHeadScale;
import net.conczin.mca.client.render.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(value=VillagerRenderState.class,remap=false)
public abstract class TricorneStateMixin implements PirateHeadScale {
    @Unique private float piratesmca$headScale=1;
    public float piratesmca$headScale(){return piratesmca$headScale;}
    public void piratesmca$headScale(float value){piratesmca$headScale=value;}
}
