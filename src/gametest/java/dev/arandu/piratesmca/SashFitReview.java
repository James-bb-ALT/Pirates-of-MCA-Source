package dev.arandu.piratesmca;
import java.nio.file.Path;
import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.client.render.VillagerVisuals;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.impl.client.gametest.world.TestWorldSaveImpl;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.phys.Vec3;

/** Diagnostic only: same saved actors, exact transforms/cameras, before/after reopen. */
final class SashFitReview {
    static List<String> state(net.minecraft.server.MinecraftServer s){
        return PirateClientGameTests.identities(s).stream().map(a->{
            var n=(VillagerEntityMCA)s.overworld().getEntity(a.id());var g=n.getGenetics();
            return a+" genes="+List.of(g.getGene(Genetics.SIZE),g.getGene(Genetics.WIDTH),g.getGene(Genetics.BREAST),g.getGene(Genetics.MELANIN),g.getGene(Genetics.SKIN),g.getGene(Genetics.FACE));
        }).toList();
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    static void views(ClientGameTestContext ctx,TestSingleplayerContext world,String stage){
        world.getServer().runCommand("time set 6000");
        world.getServer().runCommand("tp @a 2047.0 -60.6 -2.5 0 5");ctx.waitTicks(80);ctx.setScreen(()->null);
        world.getServer().runOnServer(s->{
            var ids=PirateClientGameTests.identities(s);TricornePoseReview.expect(ids.size()==2,"FIT requires original two actors");
            for(var a:ids){var n=(VillagerEntityMCA)s.overworld().getEntity(a.id());n.setNoAi(true);n.setNoGravity(true);n.setDeltaMovement(Vec3.ZERO);n.stopRiding();n.stopSleeping();n.setPos(n.getGenetics().getGender().name().equals("FEMALE")?2047:2049,-60,0);n.setYRot(180);n.setYHeadRot(180);n.yBodyRot=180;n.setXRot(0);}
        });
        ctx.waitTicks(20);ctx.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();});
        for(String camera:List.of("2047.0 -60.6 -2.5 0 5","2047.0 -60.0 -3.5 0 -5","2047.0 -60.9 -2.5 0 0","2047.0 -59.5 -2.5 0 20")){
            world.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+camera));
            var xyz=camera.split(" ");double x=Double.parseDouble(xyz[0]),y=Double.parseDouble(xyz[1]),z=Double.parseDouble(xyz[2]);ctx.waitFor(mc->mc.player.distanceToSqr(x,y,z)<.1,120);ctx.waitTicks(12);
            ctx.runOnClient(mc->{for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && n.getGenetics().getGender().name().equals("FEMALE")){
                var v=VillagerVisuals.capture(n);var renderer=(LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);var model=(net.minecraft.client.model.HumanoidModel)renderer.getModel();
                System.out.println("QA_SASH_FIT stage="+stage+" camera="+camera+" id="+n.getUUID()+" breast="+v.breastSize()+" dimension="+v.dimensions()+" body="+model.body.storePose()+" root="+model.root().storePose());
            }});
            ctx.takeScreenshot("SASH-FIT-"+stage+"-camera-"+camera.split(" ")[1]);
        }
    }
    void run(ClientGameTestContext ctx){
        var save=new TestWorldSaveImpl(ctx,Path.of(System.getProperty("piratesmca.sashFitSave")));
        List<String> before;
        try(var world=save.open()){views(ctx,world,"initial");before=world.getServer().computeOnServer(SashFitReview::state);System.out.println("QA_SASH_FIT_IDENTITY "+before);}
        try(var world=save.open()){views(ctx,world,"reopened");TricornePoseReview.expect(before.equals(world.getServer().computeOnServer(SashFitReview::state)),"FIT changed identity/genetics across reopen");}
        System.out.println("QA_SASH_FIT_COMPLETE cameras=8 same_saved_actors=true genetics_unchanged=true");
    }
}
