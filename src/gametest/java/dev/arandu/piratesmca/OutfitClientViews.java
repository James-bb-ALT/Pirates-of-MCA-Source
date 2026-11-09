package dev.arandu.piratesmca;
import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
final class OutfitClientViews {
 static void run(ClientGameTestContext context,TestSingleplayerContext world,List<UUID> ids){
  world.getServer().runOnServer(s->{s.getCommands().performPrefixedCommand(s.createCommandSourceStack(),"fill 2035 -60 -10 2061 -57 10 air");for(int i=0;i<ids.size();i++){var n=(VillagerEntityMCA)s.overworld().getEntity(ids.get(i));n.setPos(2047+i*2,-60,0);n.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,net.minecraft.world.item.ItemStack.EMPTY);}});
  for(int i=0;i<ids.size();i++)for(String view:List.of("front","side","back")){
   double x=2047+i*2;String camera=switch(view){case "side"->(x+(i==0?-2.5:2.5))+" -60.5 0.0 "+(i==0?-90:90)+" 0";case "back"->x+" -60.5 2.5 180 0";default->x+" -60.5 -2.5 0 0";};
   world.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+camera));String[] pos=camera.split(" ");double cx=Double.parseDouble(pos[0]),cy=Double.parseDouble(pos[1]),cz=Double.parseDouble(pos[2]);context.waitFor(mc->mc.player.distanceToSqr(cx,cy,cz)<.1,120);context.waitTicks(8);
   context.runOnClient(mc->{int found=0;for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && ids.contains(n.getUUID())){PirateClientGameTests.expect(n.getClothes().equals(PirateOutfits.texture("deckhand")),"OUTFIT-07 client lost uniform");var id=net.minecraft.resources.Identifier.parse(n.getClothes());PirateClientGameTests.expect(mc.getResourceManager().getResource(id).isPresent(),"OUTFIT-07 runtime PNG missing");var visuals=net.conczin.mca.client.render.VillagerVisuals.capture(n);PirateClientGameTests.expect(visuals.clothes().equals(n.getClothes()),"OUTFIT-07 renderer snapshot lost outfit");System.out.println("QA_OUTFIT_RENDER "+n.getUUID()+" "+visuals);found++;}PirateClientGameTests.expect(found==2,"OUTFIT-07 missing visible actors");});
   context.takeScreenshot("OUTFIT-deckhand-"+i+"-"+view);
  }
  System.out.println("QA_OUTFIT_VIEWS count=6 genders=2");
 }
}
