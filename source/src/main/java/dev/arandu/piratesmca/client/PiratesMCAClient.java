package dev.arandu.piratesmca.client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.conczin.mca.client.render.VillagerLikeEntityMCARenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

public final class PiratesMCAClient implements ClientModInitializer {
    public void onInitializeClient(){
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES).registerReloadListener(net.minecraft.resources.Identifier.parse("piratesmca:wardrobe"),(net.minecraft.server.packs.resources.ResourceManagerReloadListener)resources->WardrobeGeometry.clear());
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type,renderer,helper,context)->{
            if(renderer instanceof VillagerLikeEntityMCARenderer<?> mca){helper.register(new SashLayer<>(mca));helper.register(new PirateLimbsLayer<>(mca,context.getEquipmentRenderer()));helper.register(new WardrobeLayer<>(mca,context.getEquipmentRenderer()));helper.register(new AtelierSkinDetailsLayer<>(mca));}
            else if(renderer instanceof AvatarRenderer<?> player){helper.register(new SashLayer<>(player));helper.register(new PirateLimbsLayer<>(player,context.getEquipmentRenderer()));helper.register(new WardrobeLayer<>(player,context.getEquipmentRenderer()));}
        });
    }
}
