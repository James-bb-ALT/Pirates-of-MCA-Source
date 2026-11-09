package dev.arandu.piratesmca;

import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.loader.api.FabricLoader;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.resources.BuiltInSkinCatalog;
import net.conczin.mca.resources.SkinVisualIds;
import net.minecraft.client.CameraType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Real client check of every MCA body skin with the user's EMF/ETF and 3D mods. */
final class McaSkin3DReview {
    private static final Map<EquipmentSlot,String> ARMOR=Map.of(
        EquipmentSlot.HEAD,"helmet",EquipmentSlot.CHEST,"chestplate",
        EquipmentSlot.LEGS,"leggings",EquipmentSlot.FEET,"boots");

    static void run(ClientGameTestContext c,TestSingleplayerContext w,List<PirateClientGameTests.Identity> identities){
        c.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();mc.options.particles().set(net.minecraft.server.level.ParticleStatus.MINIMAL);});
        var available=FabricLoader.getInstance();
        for(String mod:List.of("armor_3d","skinlayers3d","entity_model_features","entity_texture_features"))
            PirateClientGameTests.expect(available.isModLoaded(mod),"MCA-SKIN3D required mod failed to load: "+mod);
        System.out.println("MCA_SKIN3D_MODS_PASS armor_3d=0.9.4.2 skinlayers3d=1.11.4 emf=true etf=true");

        var catalog=BuiltInSkinCatalog.get();
        var bodies=catalog.bodySkins().values().stream().sorted(Comparator.comparing((net.conczin.mca.resources.data.skin.BodySkin s)->s.getGender().name()).thenComparing(net.conczin.mca.resources.data.skin.BodySkin::getIdentifier)).toList();
        var clothes=catalog.clothing().values();
        PirateClientGameTests.expect(bodies.size()==20,"MCA native body skin count changed: "+bodies.size());
        PirateClientGameTests.expect(clothes.size()==322,"MCA native clothing count changed: "+clothes.size());

        var genderActors=new EnumMap<Gender,PirateClientGameTests.Identity>(Gender.class);
        for(var identity:identities)genderActors.putIfAbsent(Gender.valueOf(identity.gender()),identity);
        PirateClientGameTests.expect(genderActors.keySet().containsAll(List.of(Gender.MALE,Gender.FEMALE)),"Missing adult MCA gender fixtures");
        var keep=new HashSet<UUID>();for(var identity:genderActors.values())keep.add(identity.id());
        w.getServer().runOnServer(s->{
            for(var identity:identities){var n=(VillagerEntityMCA)s.overworld().getEntity(identity.id());if(!keep.contains(identity.id()))n.discard();else{n.setNoAi(true);n.setYRot(180);}}
            for(var entry:genderActors.entrySet()){
                var identity=entry.getValue();var n=(VillagerEntityMCA)s.overworld().getEntity(identity.id());
                n.setPos(entry.getKey()==Gender.FEMALE?2070:2048,-60,0);n.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
                for(var armor:ARMOR.entrySet())n.setItemSlot(armor.getKey(),new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("pirateatelier:captain_armor_pristine_normal_"+armor.getValue()))));
            }
        });
        w.getServer().runCommand("time set 6000");

        var bodyIds=bodies.stream().map(net.conczin.mca.resources.data.skin.BodySkin::getIdentifier).collect(java.util.stream.Collectors.toSet());
        int rendered=0;
        for(var body:bodies){
            String id=body.getIdentifier();Gender gender=body.getGender();
            PirateClientGameTests.expect(bodyIds.contains(id)&&SkinVisualIds.isBodySkin(id,gender),"MCA body skin not registered for gender: "+id+" "+gender);
            var actor=genderActors.get(gender);PirateClientGameTests.expect(actor!=null,"No fixture for "+gender);
            w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(actor.id());n.setSkin(id);});c.waitTicks(6);
            c.runOnClient(mc->{
                VillagerEntityMCA n=null;for(var entity:mc.level.entitiesForRendering())if(entity instanceof VillagerEntityMCA v&&v.getUUID().equals(actor.id()))n=v;
                PirateClientGameTests.expect(n!=null,"MCA body skin actor absent: "+id);
                PirateClientGameTests.expect(n.getSkin().equals(id),"MCA body skin sync mismatch: "+id);
                var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);
                var state=(net.minecraft.client.renderer.entity.state.HumanoidRenderState)renderer.createRenderState();renderer.extractRenderState(n,state,0);
                var skinLayer=new net.conczin.mca.client.render.layer.SkinLayer(renderer,(net.minecraft.client.model.HumanoidModel)renderer.getModel());
                PirateClientGameTests.expect(skinLayer.getSkin(state).equals(Identifier.parse(id)),"MCA renderer chose wrong skin for "+id+": "+skinLayer.getSkin(state));
                PirateClientGameTests.expect(dev.arandu.piratesmca.client.WardrobeGeometry.get(n.getItemBySlot(EquipmentSlot.CHEST))!=null,"Pirate chest armor absent on "+id);
                System.out.println("MCA_SKIN3D_BODY_PASS id="+id+" gender="+gender+" armor=pirate-captain");
            });
            double x=gender==Gender.FEMALE?2070:2048;
            w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+x+" -60.4 -3.4 0 0"));c.waitTicks(2);c.takeScreenshot("MCA-SKIN3D-"+id.substring(id.lastIndexOf('/')+1).replace(".png","").replace('/','_'));rendered++;
        }

        var clothingIds=clothes.stream().map(net.conczin.mca.resources.data.skin.Clothing::getIdentifier).sorted().toList();
        int validated=0;
        for(var clothing:clothes){String id=clothing.getIdentifier();PirateClientGameTests.expect(SkinVisualIds.isClothing(id,clothing.getGender()),"MCA clothing gender validation failed: "+id);validated++;}
        c.runOnClient(mc->{for(String id:clothingIds)PirateClientGameTests.expect(mc.getResourceManager().getResource(Identifier.parse(id)).isPresent(),"MCA clothing texture missing from client resources: "+id);});
        PirateClientGameTests.expect(rendered==20&&validated==322,"Incomplete MCA native catalog run");
        System.out.println("MCA_SKIN3D_CATALOG_PASS body_skins="+rendered+" clothing_textures="+validated);

        c.runOnClient(mc->{
            var cem=Identifier.fromNamespaceAndPath("minecraft","optifine/cem/outer_armor.jem");
            PirateClientGameTests.expect(mc.getResourceManager().getResource(cem).isPresent(),"3D Armor CEM outer_armor.jem not visible through EMF resource manager");
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            System.out.println("MCA_SKIN3D_CEM_RESOURCE_PASS "+mc.getResourceManager().getResource(cem).orElseThrow().sourcePackId());
        });
        w.getServer().runCommand("gamemode survival @a");
        w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s 2056.0 -60.0 -3.0 180 0"));
        w.getServer().runOnServer(s->{var player=s.getPlayerList().getPlayers().getFirst();player.removeAllEffects();player.setInvisible(false);});
        c.waitFor(mc->!mc.player.isInvisible(),120);
        w.getServer().runCommand("item replace entity @a armor.head with minecraft:iron_helmet");
        w.getServer().runCommand("item replace entity @a armor.chest with minecraft:iron_chestplate");
        w.getServer().runCommand("item replace entity @a armor.legs with minecraft:iron_leggings");
        w.getServer().runCommand("item replace entity @a armor.feet with minecraft:iron_boots");
        c.waitTicks(20);c.takeScreenshot("MCA-SKIN3D-player-3d-armor-cem-and-skinlayers");
        for(String slot:List.of("head","chest","legs","feet"))w.getServer().runCommand("item replace entity @a armor."+slot+" with minecraft:air");
        c.waitTicks(12);c.takeScreenshot("MCA-SKIN3D-player-skinlayers-no-armor");
        c.runOnClient(mc->{PirateClientGameTests.expect(mc.options.getCameraType()==CameraType.THIRD_PERSON_FRONT,"Skin Layers player third-person review not enabled");mc.options.setCameraType(CameraType.FIRST_PERSON);});
        System.out.println("MCA_SKIN3D_CLIENT_REVIEW_PASS native_body_skins=20 native_clothing=322 rendered_screenshots="+(rendered+2));
    }
}
