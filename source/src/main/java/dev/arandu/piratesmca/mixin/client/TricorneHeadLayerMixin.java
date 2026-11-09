package dev.arandu.piratesmca.mixin.client;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.arandu.piratesmca.client.PirateHeadScale;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(CustomHeadLayer.class)
public abstract class TricorneHeadLayerMixin {
    @WrapMethod(method="submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V")
    private void piratesmca$fitMcaHead(PoseStack pose,SubmitNodeCollector nodes,int light,LivingEntityRenderState state,float yaw,float pitch,Operation<Void> original){
        float scale=state instanceof PirateHeadScale data?data.piratesmca$headScale():1;
        var model=((CustomHeadLayer)(Object)this).getParentModel();
        var root=model instanceof net.conczin.mca.client.model.CommonVillagerModel<?>
            &&state instanceof net.minecraft.client.renderer.entity.state.HumanoidRenderState humanoid
            &&dev.arandu.piratesmca.PirateWearables.isCosmetic(humanoid.headEquipment)?model.root():null;
        if(scale==1&&root==null){original.call(pose,nodes,light,state,yaw,pitch);return;}
        var saved=root==null?null:root.storePose();
        pose.pushPose();
        try{
            if(root!=null)root.loadPose(net.minecraft.client.model.geom.PartPose.ZERO);
            pose.scale(scale,scale,scale);original.call(pose,nodes,light,state,yaw,pitch);
        }finally{if(root!=null)root.loadPose(saved);pose.popPose();}
    }
}
