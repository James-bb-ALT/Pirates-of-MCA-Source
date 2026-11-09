package dev.arandu.piratesmca.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import com.mojang.math.Axis;

/** Waist follows the animated hip pivots; torso breathing/waking must not bury it in legs. */
public final class SashLayer<S extends HumanoidRenderState,M extends HumanoidModel<S>> extends RenderLayer<S,M> {
    public SashLayer(RenderLayerParent<S,M> parent){super(parent);}
    public static void applyWaistAnchor(PoseStack pose,HumanoidModel<?> model){
        // Source geometry uses the neutral adult hip at y=12. Pivot fields are
        // pixels; follow the actual hip center and retain upper-body yaw only.
        pose.translate((model.leftLeg.x+model.rightLeg.x)/32F,
            (model.leftLeg.y+model.rightLeg.y-24F)/32F,
            (model.leftLeg.z+model.rightLeg.z)/32F);
        pose.mulPose(Axis.YP.rotation(model.body.yRot));
    }
    @Override public void submit(PoseStack pose,SubmitNodeCollector nodes,int light,S state,float yaw,float pitch){
        if(!(state instanceof SashRenderData data) || state.isInvisible || data.piratesmca$sash().isEmpty())return;
        pose.pushPose();
        try{
            EquipmentPose.root(pose,getParentModel());
            applyWaistAnchor(pose,getParentModel());
            pose.scale(1,-1,-1);
            data.piratesmca$sash().submit(pose,nodes,light,LivingEntityRenderer.getOverlayCoords(state,0),state.outlineColor);
        }finally{pose.popPose();}
    }
}
