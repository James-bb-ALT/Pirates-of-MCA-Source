package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.PirateWearables;
import dev.arandu.piratesmca.client.PirateHeadScale;
import net.conczin.mca.client.render.*;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=VillagerLikeEntityMCARenderer.class,remap=false)
public abstract class TricorneExtractMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Mob;Lnet/conczin/mca/client/render/VillagerRenderState;F)V",at=@At("TAIL"))
    private void piratesmca$extractHat(Mob npc,VillagerRenderState state,float delta,CallbackInfo ci){
        ((PirateHeadScale)state).piratesmca$headScale(PirateWearables.isCosmetic(npc.getItemBySlot(EquipmentSlot.HEAD))?VillagerVisuals.require(state).dimensions().getHead():1);
        // Authored pirate faces keep their higher eye row; native MCA faces use
        // the original lower fit. Resolve a presentation-only copy, never save it.
        var helmet=npc.getItemBySlot(EquipmentSlot.HEAD);
        if(dev.arandu.piratesmca.client.PirateSkinLayers.get(VillagerVisuals.require(state).skin())!=null
            &&(helmet.is(PirateWearables.EYEPATCH)||helmet.is(PirateWearables.TRICORNE_EYEPATCH))){
            var shown=helmet.copy();
            shown.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,net.minecraft.resources.Identifier.fromNamespaceAndPath("piratesmca",
                helmet.is(PirateWearables.EYEPATCH)?"eyepatch_leather_pirate_face":"tricorne_eyepatch_pirate_face"));
            net.minecraft.client.Minecraft.getInstance().getItemModelResolver().updateForLiving(state.headItem,shown,net.minecraft.world.item.ItemDisplayContext.HEAD,npc);
        }
    }
}
