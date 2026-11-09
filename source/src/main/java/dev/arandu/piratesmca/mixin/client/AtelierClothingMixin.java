package dev.arandu.piratesmca.mixin.client;
import dev.arandu.piratesmca.PirateAtelier;
import net.conczin.mca.client.render.VillagerVisuals;
import net.conczin.mca.client.render.layer.ClothingLayer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Full player skins already contain their individual clothes; MCA clothes data is retained. */
@Mixin(value=ClothingLayer.class,remap=false)
public abstract class AtelierClothingMixin {
 @Inject(method="getSkin(Lnet/conczin/mca/client/render/VillagerVisuals;)Lnet/minecraft/resources/Identifier;",at=@At("HEAD"),cancellable=true,remap=false)
 private void piratesmca$fullOutfit(VillagerVisuals visuals,CallbackInfoReturnable<Identifier> cir){if(PirateAtelier.extraSkin(visuals.skin())&&visuals.clothes().startsWith("piratesmca:skins/clothing/"))cir.setReturnValue(null);}
}
