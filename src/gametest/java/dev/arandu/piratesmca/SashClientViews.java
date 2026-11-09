package dev.arandu.piratesmca;
import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import dev.arandu.piratesmca.client.SashRenderData;

final class SashClientViews {
    private static int legacyFrontClips;
    static double maxY(com.mojang.blaze3d.vertex.PoseStack pose,dev.arandu.piratesmca.client.SashRenderData data){
        var matrix=pose.last().pose();double[] max={-Double.MAX_VALUE};
        data.piratesmca$sash().visitExtents(v->{var p=new org.joml.Vector3f(v).mulPosition(matrix);max[0]=Math.max(max[0],p.y()*16);});return max[0];
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    static void deterministicWakeRegression(net.minecraft.client.Minecraft mc,VillagerEntityMCA n){
        var renderer=(LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);var state=(LivingEntityRenderState)renderer.createRenderState();renderer.extractRenderState(n,state,0);
        var model=(net.minecraft.client.model.HumanoidModel)renderer.getModel();var saved=model.body.storePose();
        try{
            // Measured on the original failing actor, independent of machine FPS.
            model.body.setPos(.07820743F,1.606595F,-.5650008F);model.body.setRotation(.07437499F,.09701631F,.0011497792F);
            var old=new com.mojang.blaze3d.vertex.PoseStack();model.body.translateAndRotate(old);old.scale(1,-1,-1);
            var fixed=new com.mojang.blaze3d.vertex.PoseStack();dev.arandu.piratesmca.client.SashLayer.applyWaistAnchor(fixed,model);fixed.scale(1,-1,-1);
            double hip=(model.leftLeg.y+model.rightLeg.y)/2.0,a=maxY(old,(SashRenderData)state),b=maxY(fixed,(SashRenderData)state);
            TricornePoseReview.expect(a>hip && b<=hip-.2,"SASH-FIT-01 deterministic old/new contrast failed");
            System.out.println("QA_SASH_DETERMINISTIC_WAKE oldMaxY="+a+" correctedMaxY="+b+" hipY="+hip);
        }finally{model.body.loadPose(saved);}
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    static void verifySnapshot(net.minecraft.client.Minecraft mc,VillagerEntityMCA n,String stage){
        TricornePoseReview.expect(n.getItemBySlot(EquipmentSlot.CHEST).is(PirateSash.ITEM),"SASH-05 missing client sash at "+stage);
        var renderer=(LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);var state=(LivingEntityRenderState)renderer.createRenderState();renderer.extractRenderState(n,state,0);
        TricornePoseReview.expect(state instanceof SashRenderData d && !d.piratesmca$sash().isEmpty(),"SASH-05 item model was not extracted");
        var model=(net.minecraft.client.model.HumanoidModel)renderer.getModel();
        var legacy=new com.mojang.blaze3d.vertex.PoseStack();model.body.translateAndRotate(legacy);legacy.scale(1,-1,-1);
        var fixed=new com.mojang.blaze3d.vertex.PoseStack();dev.arandu.piratesmca.client.SashLayer.applyWaistAnchor(fixed,model);fixed.scale(1,-1,-1);
        double hip=(model.leftLeg.y+model.rightLeg.y)/2.0,oldY=maxY(legacy,(SashRenderData)state),newY=maxY(fixed,(SashRenderData)state);
        TricornePoseReview.expect(newY<=hip-.2,"SASH-FIT-01 sash crossed hip plane at "+stage+": "+newY+" hip="+hip);
        if(stage.startsWith("front-case") && oldY>hip)legacyFrontClips++;
        System.out.println("QA_SASH_HIP_BOUNDS stage="+stage+" oldMaxY="+oldY+" newMaxY="+newY+" hipY="+hip);
        System.out.println("QA_SASH_RENDER_SNAPSHOT stage="+stage+" npc="+n.getUUID()+" renderer="+renderer.getClass().getName()+" gender="+n.getGenetics().getGender());
    }
    static void run(ClientGameTestContext context,TestSingleplayerContext world,List<UUID> ids){
        legacyFrontClips=0;
        world.getServer().runOnServer(s->{s.getCommands().performPrefixedCommand(s.createCommandSourceStack(),"fill 2035 -60 -10 2061 -57 10 air");for(int i=0;i<ids.size();i++){var n=(VillagerEntityMCA)s.overworld().getEntity(ids.get(i));n.setPos(2047+i*2,-60,0);TricornePoseReview.expect(n.getItemBySlot(EquipmentSlot.CHEST).is(PirateSash.ITEM),"SASH-05 fixture lost chest slot");}});
        for(int i=0;i<ids.size();i++)for(String view:List.of("front","side","back")){
            double x=2047+i*2;String camera=switch(view){case "side"->(x+(i==0?-2.5:2.5))+" -60.6 0.0 "+(i==0?-90:90)+" 5";case "back"->x+" -60.6 2.5 180 5";default->x+" -60.6 -2.5 0 5";};
            world.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+camera));
            String[] xyz=camera.split(" ");double cx=Double.parseDouble(xyz[0]),cy=Double.parseDouble(xyz[1]),cz=Double.parseDouble(xyz[2]);context.waitFor(mc->mc.player.distanceToSqr(cx,cy,cz)<.1,120);context.waitTicks(8);
            context.takeScreenshot("SASH-"+i+"-"+view);
            if(view.equals("front")){
                final var targetId=ids.get(i);
                context.runOnClient(mc->{for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && n.getUUID().equals(targetId))verifySnapshot(mc,n,"front-case-"+targetId);});
            }
            if(Boolean.getBoolean("piratesmca.sashReplayOriginal")){
                final int target=i;
                context.runOnClient(mc->{for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && n.getUUID().equals(ids.get(target))){
                    var renderer=(net.conczin.mca.client.render.VillagerLikeEntityMCARenderer<?>)mc.getEntityRenderDispatcher().getRenderer(n);
                    System.out.println("QA_SASH_REPLAY_VIEW view="+view+" id="+n.getUUID()+" bodyYaw="+n.yBodyRot+" pitch="+n.getXRot()+" body="+renderer.getModel().body.storePose()+" root="+renderer.getModel().root().storePose());
                }});
            }
        }
        context.runOnClient(mc->{for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && ids.contains(n.getUUID())){verifySnapshot(mc,n,"views");deterministicWakeRegression(mc,n);}});
        System.out.println("QA_SASH_VIEWS count=6 genders=2");
        if(Boolean.getBoolean("piratesmca.sashReplayOriginal"))TricornePoseReview.expect(legacyFrontClips>0,"FIT replay did not reproduce the old hip intersection");
        System.out.println("QA_SASH_ANCHOR_REGRESSION legacy_front_clips="+legacyFrontClips+" corrected_clips=0");
    }
}
