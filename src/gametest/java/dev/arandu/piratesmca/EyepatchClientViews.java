package dev.arandu.piratesmca;
import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.world.entity.EquipmentSlot;

final class EyepatchClientViews {
    static void run(ClientGameTestContext context,TestSingleplayerContext world,List<UUID> ids){
        for(boolean hat:List.of(false,true)){
            world.getServer().runOnServer(s->{
                s.getCommands().performPrefixedCommand(s.createCommandSourceStack(),"fill 2035 -60 -10 2061 -57 10 air");
                for(int i=0;i<ids.size();i++){var n=(VillagerEntityMCA)s.overworld().getEntity(ids.get(i));n.setPos(2047+i*2,-60,0);PirateWearables.clear(n);PirateWearables.equipEyepatch(n);if(hat)PirateWearables.equip(n);}
            });
            var item=hat?PirateWearables.TRICORNE_EYEPATCH:PirateWearables.EYEPATCH;
            context.waitFor(mc->{int ready=0;for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && ids.contains(n.getUUID()) && n.getItemBySlot(EquipmentSlot.HEAD).is(item))ready++;return ready==2;},120);
            for(int i=0;i<ids.size();i++)for(String view:List.of("front","side","back")){
                double x=2047+i*2;String camera=switch(view){case "side"->(x+(i==0?-1.8:1.8))+" -60.0 0.0 "+(i==0?-90:90)+" -5";case "back"->x+" -60.0 1.8 180 -5";default->x+" -60.0 -1.8 0 -5";};
                world.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+camera));
                String[] xyz=camera.split(" ");double cx=Double.parseDouble(xyz[0]),cy=Double.parseDouble(xyz[1]),cz=Double.parseDouble(xyz[2]);
                context.waitFor(mc->mc.player.distanceToSqr(cx,cy,cz)<.1,120);context.waitTicks(8);
                context.takeScreenshot("EYE-"+(hat?"combined":"alone")+"-"+i+"-"+view);
            }
        }
        System.out.println("QA_EYEPATCH_VIEWS count=12 genders=2 states=2");
    }
}
