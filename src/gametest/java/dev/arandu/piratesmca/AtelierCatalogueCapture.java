package dev.arandu.piratesmca;

import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Fresh screenshots of every shipped Atelier skin and complete armor configuration. */
final class AtelierCatalogueCapture {
    private static final List<String> VIEWS=List.of("front","side","back");
    private static final Map<String,EquipmentSlot> SLOTS=Map.of(
        "helmet",EquipmentSlot.HEAD,"chestplate",EquipmentSlot.CHEST,
        "leggings",EquipmentSlot.LEGS,"boots",EquipmentSlot.FEET);

    private static String safe(String value){return value.replace(':','_').replace('/','_');}
    private static ItemStack stack(String id){return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));}

    static void run(ClientGameTestContext c,TestSingleplayerContext w,List<PirateClientGameTests.Identity> identities){
        c.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();mc.options.particles().set(net.minecraft.server.level.ParticleStatus.MINIMAL);});
        var male=identities.stream().filter(i->i.gender().equalsIgnoreCase("male")).findFirst().orElseThrow();
        var female=identities.stream().filter(i->i.gender().equalsIgnoreCase("female")).findFirst().orElseThrow();
        w.getServer().runOnServer(s->{for(var identity:identities){var n=(VillagerEntityMCA)s.overworld().getEntity(identity.id());n.setNoAi(true);n.setPos(identity.id().equals(male.id())?2048:identity.id().equals(female.id())?2070:3000,-60,0);n.setYRot(180);for(var slot:SLOTS.values())n.setItemSlot(slot,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);}});

        w.getServer().runCommand("gamemode spectator @a");w.getServer().runCommand("time set 6000");
        w.getServer().runOnServer(s->{s.overworld().getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME,false,s);s.overworld().getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_WEATHER,false,s);});
        int captured=0;
        for(var skin:PirateAtelier.SKINS){
            var identity=skin.gender().equalsIgnoreCase("female")?female:male;
            w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(identity.id());n.setSkin(skin.id());});
            c.waitTicks(4);
            c.runOnClient(mc->{for(var entity:mc.level.entitiesForRendering())if(entity instanceof VillagerEntityMCA n&&n.getUUID().equals(identity.id())){
                PirateClientGameTests.expect(n.getSkin().equals(skin.id()),"catalog skin failed to sync: "+skin.name());
                var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);var state=(net.minecraft.client.renderer.entity.state.HumanoidRenderState)renderer.createRenderState();renderer.extractRenderState(n,state,0);
                var clothes=new net.conczin.mca.client.render.layer.ClothingLayer(renderer,(net.minecraft.client.model.HumanoidModel)renderer.getModel(),"normal");
                PirateClientGameTests.expect(clothes.getSkin(state)==null,"Profession clothes cover "+skin.name());
                System.out.println("QA_CATALOG_SKIN_RESOLVED id="+skin.id()+" gender="+n.getGenetics().getGender()+" clothing=null gear=empty");
            }});
            double x=identity.id().equals(female.id())?2070:2048;
            for(String view:VIEWS){camera(c,w,view,x);c.takeScreenshot("CATALOG-SKIN-"+skin.name()+"-"+view);captured++;}
            double feet=w.getServer().computeOnServer(s->-58.5-s.getPlayerList().getPlayers().getFirst().getEyeHeight());
            w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+x+" "+feet+" -1.1 0 0"));c.waitTicks(4);c.takeScreenshot("CATALOG-SKIN-"+skin.name()+"-head");captured++;
            w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(identity.id());for(var e:SLOTS.entrySet())n.setItemSlot(e.getValue(),stack("pirateatelier:captain_armor_pristine_normal_"+e.getKey()));});c.waitTicks(4);
            for(String view:VIEWS){camera(c,w,view,x);c.takeScreenshot("CATALOG-SKIN-EQUIPPED-"+skin.name()+"-"+view);captured++;}
            w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(identity.id());for(var slot:SLOTS.values())n.setItemSlot(slot,ItemStack.EMPTY);});
        }
        System.out.println("QA_CATALOG_SKINS_PASS count="+PirateAtelier.SKINS.length+" screenshots="+(PirateAtelier.SKINS.length*7));

        var groups=new TreeMap<String,Map<String,net.minecraft.world.item.Item>>();
        for(var pair:PirateAtelier.ITEMS.entrySet())if(pair.getValue().kind().equals("armor")){
            var id=pair.getValue().id();var suffix="_"+pair.getValue().slot();
            if(!id.endsWith(suffix))throw new AssertionError("Unexpected armor id/slot "+id+" / "+pair.getValue().slot());
            groups.computeIfAbsent(id.substring(0,id.length()-suffix.length()),k->new TreeMap<>()).put(pair.getValue().slot(),pair.getKey());
        }
        int complete=0,extraHelmets=0;
        for(var group:groups.entrySet()){
            var pieces=group.getValue();
            if(pieces.size()==4)complete++;else if(pieces.size()==1&&pieces.containsKey("helmet"))extraHelmets++;else throw new AssertionError("Incomplete armor configuration "+group.getKey());
            w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(male.id());n.setSkin(PirateAtelier.SKINS[0].id());for(var slot:SLOTS.values())n.setItemSlot(slot,ItemStack.EMPTY);for(var piece:pieces.entrySet())n.setItemSlot(SLOTS.get(piece.getKey()),new ItemStack(piece.getValue()));});
            c.waitFor(mc->{for(var entity:mc.level.entitiesForRendering())if(entity instanceof VillagerEntityMCA n&&n.getUUID().equals(male.id()))return SLOTS.entrySet().stream().allMatch(e->pieces.containsKey(e.getKey())?n.getItemBySlot(e.getValue()).is(pieces.get(e.getKey())):n.getItemBySlot(e.getValue()).isEmpty());return false;},100);
            c.runOnClient(mc->{for(var entity:mc.level.entitiesForRendering())if(entity instanceof VillagerEntityMCA n&&n.getUUID().equals(male.id()))for(var piece:pieces.entrySet()){
                var item=n.getItemBySlot(SLOTS.get(piece.getKey()));var mesh=dev.arandu.piratesmca.client.WardrobeGeometry.get(item);PirateClientGameTests.expect(mesh!=null,"Missing geometry for "+group.getKey()+"/"+piece.getKey());
                System.out.println("QA_CATALOG_ARMOR_RESOLVED item="+BuiltInRegistries.ITEM.getKey(item.getItem())+" geometry="+mesh.resource()+" texture="+mesh.texture()+" parts="+mesh.parts().size());
            }});
            for(String view:VIEWS){camera(c,w,view,2048);c.takeScreenshot("CATALOG-ARMOR-"+safe(group.getKey())+"-"+view);captured++;}
            if((captured-140)%150==0)System.out.println("QA_CATALOG_PROGRESS screenshots="+captured+" armor="+group.getKey());
        }
        PirateClientGameTests.expect(complete==1086&&extraHelmets==5,"catalog coverage changed: full sets="+complete+" extra helmets="+extraHelmets);
        System.out.println("QA_CATALOG_ARMOR_PASS full_sets="+complete+" extra_helmets="+extraHelmets+" screenshots="+((complete+extraHelmets)*3));
        System.out.println("QA_CATALOG_CAPTURE_COMPLETE screenshots="+captured);
    }

    private static void camera(ClientGameTestContext c,TestSingleplayerContext w,String view,double x){
        double feet=w.getServer().computeOnServer(s->-58.95-s.getPlayerList().getPlayers().getFirst().getEyeHeight());
        String tp=switch(view){case "side"->(x-3.4)+" "+feet+" 0.0 -90 0";case "back"->x+" "+feet+" 3.4 180 0";default->x+" "+feet+" -3.4 0 0";};
        w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+tp));c.waitTicks(2);
    }
}
