package dev.arandu.piratesmca;
import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;

final class EyepatchPersistenceReview {
    static List<PirateClientGameTests.Identity> transitions(ClientGameTestContext context,TestSingleplayerContext world,List<UUID> ids){
        var before=world.getServer().computeOnServer(PirateClientGameTests::identities);var traveler=ids.getFirst();
        world.getServer().runCommand("execute in minecraft:the_nether run forceload add 0 0");context.waitTicks(30);
        world.getServer().runCommand("execute in minecraft:the_nether run tp "+traveler+" 0.5 100.0 0.5");context.waitTicks(20);
        world.getServer().runOnServer(s->{
            var e=s.getLevel(Level.NETHER).getEntity(traveler);
            TricornePoseReview.expect(e instanceof VillagerEntityMCA n && n.getItemBySlot(EquipmentSlot.HEAD).is(PirateWearables.TRICORNE_EYEPATCH),"EYE-05 Nether lost composite");
            if(Boolean.getBoolean("piratesmca.sashReview"))TricornePoseReview.expect(((VillagerEntityMCA)e).getItemBySlot(EquipmentSlot.CHEST).is(PirateSash.ITEM),"SASH-06 Nether lost chest equipment");
        });
        world.getServer().runCommand("execute in minecraft:overworld run tp "+traveler+" 2047.0 -60.0 0.0");context.waitTicks(30);
        TricornePoseReview.expect(before.equals(world.getServer().computeOnServer(PirateClientGameTests::identities)),"EYE-05 dimension transfer changed identity");
        System.out.println("QA_EYEPATCH_DIMENSION_PASS entities=2");
        world.getServer().runCommand("tp @a 4096.0 -60.0 4096.0");context.waitTicks(400);
        world.getServer().runOnServer(s->{for(var id:ids)TricornePoseReview.expect(s.overworld().getEntity(id)==null,"EYE-06 chunk did not unload");});
        world.getServer().runCommand("tp @a 2048.0 -60.0 -3.5 0 -5");context.waitTicks(100);
        TricornePoseReview.expect(before.equals(world.getServer().computeOnServer(PirateClientGameTests::identities)),"EYE-06 chunk reload changed identity");
        if(Boolean.getBoolean("piratesmca.sashReview"))world.getServer().runOnServer(s->{for(var id:ids)TricornePoseReview.expect(((VillagerEntityMCA)s.overworld().getEntity(id)).getItemBySlot(EquipmentSlot.CHEST).is(PirateSash.ITEM),"SASH-07 chunk reload lost chest equipment");});
        PirateClientGameTests.verifyClient(context,before,"EYE-after-chunk-reload");context.setScreen(()->null);context.takeScreenshot("EYE-after-chunk-reload");
        System.out.println("QA_EYEPATCH_CHUNK_PASS entities=2");return before;
    }
}
