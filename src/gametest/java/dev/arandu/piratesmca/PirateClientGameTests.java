package dev.arandu.piratesmca;

import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;

/** Runs through the real client/server without desktop mouse automation. Test mod is never shipped. */
public class PirateClientGameTests implements FabricClientGameTest {
    record Identity(UUID id,String name,String skin,String clothes,String hair,String gender,String head,String equipment,String genetics) {}
    static String genetics(VillagerEntityMCA npc) {
        var g=npc.getGenetics();
        var result=new StringBuilder();
        for(var type:List.of(net.conczin.mca.entity.ai.Genetics.SIZE,net.conczin.mca.entity.ai.Genetics.WIDTH,
            net.conczin.mca.entity.ai.Genetics.BREAST,net.conczin.mca.entity.ai.Genetics.MELANIN,
            net.conczin.mca.entity.ai.Genetics.HEMOGLOBIN,net.conczin.mca.entity.ai.Genetics.EUMELANIN,
            net.conczin.mca.entity.ai.Genetics.PHEOMELANIN,net.conczin.mca.entity.ai.Genetics.SKIN,
            net.conczin.mca.entity.ai.Genetics.FACE,net.conczin.mca.entity.ai.Genetics.EYE_BRIGHTNESS,
            net.conczin.mca.entity.ai.Genetics.VOICE,net.conczin.mca.entity.ai.Genetics.VOICE_TONE))
            result.append(Float.floatToIntBits(g.getGene(type))).append('/');
        return result.toString();
    }
    static String equipment(VillagerEntityMCA npc){return java.util.stream.Stream.of(net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET,net.minecraft.world.entity.EquipmentSlot.MAINHAND,net.minecraft.world.entity.EquipmentSlot.OFFHAND).map(slot->{var item=npc.getItemBySlot(slot);return slot+"="+net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem())+"*"+item.getCount();}).collect(java.util.stream.Collectors.joining(";"));}
    static String head(VillagerEntityMCA npc){return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(npc.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem()).toString();}
    static List<Identity> identities(MinecraftServer s) {
        return s.overworld().getEntitiesOfClass(VillagerEntityMCA.class,new AABB(2038,-65,-10,2058,-50,16)).stream()
            .filter(n->n.entityTags().contains(PiratesOfMCA.MARKER))
            .map(n->new Identity(n.getUUID(),n.getName().getString(),n.getSkin(),n.getClothes(),n.getHairStyleId(),n.getGenetics().getGender().name(),head(n),equipment(n),genetics(n)))
            .sorted(Comparator.comparing(i->i.id().toString())).toList();
    }
    static void expect(boolean b,String message) { if(!b)throw new AssertionError(message); }
    static void tricorneViews(ClientGameTestContext context,TestSingleplayerContext world){
        context.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();mc.options.particles().set(net.minecraft.server.level.ParticleStatus.MINIMAL);});
        world.getServer().runOnServer(s->{
            expect(identities(s).stream().filter(n->n.head().equals("piratesmca:tricorne_classic")).count()==2,"HAT-02 expected exactly two preset hats");
            deniedWithoutOp(s,"pirates preset nearest clear");
        });
        for(String view:List.of("front","side","back")){
            String tp=switch(view){case "side"->"2043.5 -60.0 0.5 -90 -8";case "back"->"2048.5 -60.0 2.5 180 -10";default->"2048.5 -60.0 -3.0 0 -10";};
            world.getServer().runOnServer(s->expect(commandAsOp(s,"tp @s "+tp)==1,"Hat camera teleport failed"));
            String[] position=tp.split(" ");double x=Double.parseDouble(position[0]),y=Double.parseDouble(position[1]),z=Double.parseDouble(position[2]);
            context.waitFor(mc->mc.player.distanceToSqr(x,y,z)<.1,200);context.waitTicks(8);context.takeScreenshot("HAT-"+view);
        }
        world.getServer().runOnServer(s->expect(commandAsOp(s,"tp @s 2048.5 -60 -4.5 0 0")==1,"Hat camera restore failed"));context.waitTicks(5);
        context.runOnClient(mc->{
            var sprite=mc.getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.ITEMS)
                .getSprite(net.minecraft.resources.Identifier.fromNamespaceAndPath("piratesmca","item/tricorne_classic"));
            expect(sprite.contents().name().toString().equals("piratesmca:item/tricorne_classic"),"HAT-01 tricorne missing from the real item atlas");
            for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA npc && npc.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(PirateWearables.TRICORNE)){
                var renderer=mc.getEntityRenderDispatcher().getRenderer(npc);
                if(renderer instanceof net.conczin.mca.client.render.VillagerEntityMCARenderer r){
                    var model=r.getModel();
                    System.out.println("QA_MCA_HEAD_RENDER renderer="+renderer.getClass().getName()+" root="+model.root().getClass().getName()+" head="+model.getHead().getClass().getName()+" pivot="+model.getHead().getInitialPose()+" headScale="+model.getDimensions().getHead()+" gender="+npc.getGenetics().getGender()+" hair="+npc.getHairStyleId());
                    try {
                        var mesh=new java.util.LinkedHashMap<String,Object>();
                        var cubeField=net.minecraft.client.model.geom.ModelPart.class.getDeclaredField("cubes");cubeField.setAccessible(true);
                        for(String name:List.of("head","body","right_arm","left_arm","right_leg","left_leg")){
                            var part=model.root().getChild(name);var pose=part.getInitialPose();var cubes=new ArrayList<Object>();
                            for(Object c:(List<?>)cubeField.get(part)){var cube=(net.minecraft.client.model.geom.ModelPart.Cube)c;cubes.add(Map.of("from",List.of(cube.minX,cube.minY,cube.minZ),"to",List.of(cube.maxX,cube.maxY,cube.maxZ)));}
                            mesh.put(name,Map.of("pivot",List.of(pose.x(),pose.y(),pose.z()),"cubes",cubes));
                        }
                        java.nio.file.Files.writeString(java.nio.file.Path.of("screenshots","mca-neutral-reference-runtime.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(mesh));
                    } catch(Exception error){throw new AssertionError("MCA reference geometry audit failed",error);}
                }
            }
            System.out.println("QA_TRICORNE_VIEWS front/side/back");
        });
        context.runOnClient(mc->{if(mc.gui.hud.isHidden())mc.gui.hud.toggle();});
    }
    static void clickDialogButton(ClientGameTestContext context,String key) {
        context.runOnClient(mc -> {
            String label=net.minecraft.network.chat.Component.translatable(key).getString();
            var pending=new ArrayDeque<net.minecraft.client.gui.components.events.GuiEventListener>();
            pending.add(mc.gui.screen());
            while(!pending.isEmpty()) {
                var child=pending.removeFirst();
                if(child instanceof net.minecraft.client.gui.components.AbstractButton button && button.active && button.getMessage().getString().equals(label)) {
                    button.onPress(new net.minecraft.client.input.MouseButtonInfo(0,0));return;
                }
                if(child instanceof net.minecraft.client.gui.components.events.ContainerEventHandler container)pending.addAll(container.children());
            }
            throw new AssertionError("Dialog button missing: "+key);
        });
    }
    static void verifyClient(ClientGameTestContext context,List<Identity> expected,String stage) {
        context.waitFor(mc -> expected.stream().allMatch(identity -> {
            for(var entity:mc.level.entitiesForRendering())if(entity.getUUID().equals(identity.id()))return true;
            return false;
        }),400);
        context.runOnClient(mc -> {
            for(var identity:expected) {
                VillagerEntityMCA found=null;
                for(var entity:mc.level.entitiesForRendering())
                    if(entity instanceof VillagerEntityMCA npc && npc.getUUID().equals(identity.id()))found=npc;
                expect(found!=null,"Client entity missing at "+stage+": "+identity.id());
                var actual=new Identity(found.getUUID(),found.getName().getString(),found.getSkin(),found.getClothes(),found.getHairStyleId(),found.getGenetics().getGender().name(),head(found),equipment(found),genetics(found));
                expect(identity.equals(actual),"Client metadata mismatch at "+stage+": "+actual+" expected "+identity);
                expect(found.getAgeState()==net.conczin.mca.entity.ai.relationship.AgeState.ADULT,"Client age is not ADULT at "+stage);
            }
            System.out.println("QA_CLIENT_IDENTITY_MATCH "+stage+" count="+expected.size());
        });
    }
    static int command(MinecraftServer s,String cmd) {
        return command(s,cmd,false);
    }
    static int commandAsOp(MinecraftServer s,String cmd) {return command(s,cmd,true);}
    static int command(MinecraftServer s,String cmd,boolean op) {
        try {
        var p=s.getPlayerList().getPlayers().getFirst();
        var source=p.createCommandSourceStack().withPermission(op?net.minecraft.server.permissions.PermissionSet.ALL_PERMISSIONS
            :net.minecraft.server.permissions.PermissionSet.NO_PERMISSIONS);
        int result=s.getCommands().getDispatcher().execute(cmd,source);
        System.out.println("QA_COMMAND "+cmd+" result="+result);
        return result;
        } catch (Exception e) { throw new AssertionError("Command failed: "+cmd,e); }
    }
    static void deniedWithoutOp(MinecraftServer s,String cmd) {
        var p=s.getPlayerList().getPlayers().getFirst();
        try {
            int result=s.getCommands().getDispatcher().execute(cmd,p.createCommandSourceStack()
                .withPermission(net.minecraft.server.permissions.PermissionSet.NO_PERMISSIONS));
            expect(result==0,"Non-OP command succeeded: "+cmd);
        } catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected) {
            System.out.println("QA_PERMISSION_DENIED "+cmd);
        }
    }
    private static void runIsolatedVisualAudit(ClientGameTestContext context){
        if(System.getProperty("piratesmca.visualComparisonSave")!=null){
            var save=new net.fabricmc.fabric.impl.client.gametest.world.TestWorldSaveImpl(context,java.nio.file.Path.of(System.getProperty("piratesmca.visualComparisonSave")));
            try(var world=save.open()){
                world.getServer().runOnServer(s->net.conczin.mca.server.world.data.PlayerSaveData.get(s.getPlayerList().getPlayers().getFirst()).setEntityDataSet(true));
                context.setScreen(()->null);world.getServer().runCommand("tp @a 2048 -60 -5");context.waitTicks(120);
                world.getServer().runCommand("tp @e[type=mca:male_villager] 2048 -60 0");
                world.getServer().runCommand("tp @e[type=mca:female_villager] 2050 -60 0");context.waitTicks(40);
                var ids=world.getServer().computeOnServer(PirateClientGameTests::identities);
                expect(ids.size()==2,"Visual comparison must reuse exactly the two saved baseline NPCs");
                System.out.println("QA_COMPARISON_SAVED_IDENTITIES "+ids);
                verifyClient(context,ids,"comparison-saved-identities");VisualAssetAudit.run(context,world,ids);
            }return;
        }
        try(var world=context.worldBuilder().create()){
            world.getServer().runOnServer(s->net.conczin.mca.server.world.data.PlayerSaveData
                .get(s.getPlayerList().getPlayers().getFirst()).setEntityDataSet(true));
            context.setScreen(()->null);world.getServer().runCommand("tp @a 2048 -60 -5");
            context.waitTicks(200);context.setScreen(()->null);
            world.getServer().runCommand("summon mca:male_villager 2048 -60 0 {Age:0,NoAI:1b,Rotation:[180f,0f],Tags:[\"pmca_seapost\",\"pmca_friendly\",\"pmca_adult_required\",\"pmca_gender_male\"]}");
            world.getServer().runCommand("summon mca:female_villager 2050 -60 0 {Age:0,NoAI:1b,Rotation:[180f,0f],Tags:[\"pmca_seapost\",\"pmca_friendly\",\"pmca_adult_required\",\"pmca_gender_female\"]}");
            context.waitTicks(80);context.setScreen(()->null);
            var identities=world.getServer().computeOnServer(PirateClientGameTests::identities);
            expect(identities.size()==2,"Isolated visual harness must contain exactly two adult pirates");
            expect(identities.stream().map(Identity::gender).distinct().count()==2,"Isolated visual harness must contain both MCA genders");
            verifyClient(context,identities,"visual-isolated");
            VisualAssetAudit.run(context,world,identities);
        }
    }
    @Override public void runTest(ClientGameTestContext context) {
        // Complete the test player's MCA onboarding; otherwise DestinyScreen pauses the integrated server.
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if(mc.gui.screen() instanceof net.conczin.mca.client.gui.DestinyScreen) {
                net.conczin.mca.MCAClient.getDestinyManager().allowClosing();
                mc.gui.setScreen(null);
            }
        });
        context.runOnClient(mc -> {
            mc.options.pauseOnLostFocus=false;
            mc.options.renderDistance().set(4);
            mc.options.simulationDistance().set(6);
        });
        if(Boolean.getBoolean("piratesmca.dialogueReview")){DialogueClientReview.run(context);return;}
        if(Boolean.getBoolean("piratesmca.visualAssetAudit")){runIsolatedVisualAudit(context);return;}
        if(Boolean.getBoolean("piratesmca.weaponHandheldReview")){WeaponHandheldReview.run(context);return;}
        if(Boolean.getBoolean("piratesmca.spawnEquipmentReview")){new SpawnEquipmentClientReview().run(context);return;}
        if(Boolean.getBoolean("piratesmca.sashFitReview")){new SashFitReview().run(context);return;}
        if(Boolean.getBoolean("piratesmca.creativeReview")){new CreativeTabClientReview().run(context);return;}
        if(Boolean.getBoolean("piratesmca.poseReview")){new TricornePoseReview().run(context);return;}
        if(Boolean.getBoolean("piratesmca.clientReopen")) {
            reopenInNewProcess(context);
            return;
        }
        TestWorldSave save;
        List<Identity> before;
        UUID crew;
        try(var world=context.worldBuilder().create()) {
            save=world.getWorldSave();
            world.getServer().runOnServer(s -> net.conczin.mca.server.world.data.PlayerSaveData
                .get(s.getPlayerList().getPlayers().getFirst()).setEntityDataSet(true));
            context.runOnClient(mc->mc.options.pauseOnLostFocus=false);
            context.setScreen(()->null);
            world.getServer().runCommand("tp @a 2048 -60 -5 0 0");
            context.waitTicks(200);
            context.waitTicks(100);
            world.getServer().runCommand("summon mca:male_villager 2047 -60 0 {Age:0,NoAI:1b,Rotation:[180f,0f],Tags:[\"pmca_seapost\",\"pmca_friendly\",\"pmca_preset_tricorne_classic\",\"pmca_gender_male\",\"pmca_adult_required\"]}");
            world.getServer().runCommand("summon mca:female_villager 2049 -60 0 {Age:0,NoAI:1b,Rotation:[180f,0f],Tags:[\"pmca_seapost\",\"pmca_friendly\",\"pmca_preset_tricorne_classic\",\"pmca_gender_female\",\"pmca_adult_required\"]}");
            for(int i=0;i<46;i++) {
                boolean female=i%2==0;
                int x=2040+(i%8)*2,z=4+(i/8)*2;
                world.getServer().runCommand("summon mca:"+(female?"female":"male")+"_villager "+x+" -60 "+z
                    +" {Age:0,NoAI:1b,Rotation:[180f,0f],Tags:[\"pmca_seapost\",\"pmca_bruiser\",\"pmca_gender_"
                    +(female?"female":"male")+"\",\"pmca_adult_required\"]}");
            }
            context.waitTicks(80);
            context.setScreen(()->null);
            context.waitTicks(40);
            context.setScreen(()->null);
            before=world.getServer().computeOnServer(PirateClientGameTests::identities);
            System.out.println("QA_SERVER_IDENTITIES "+before);
            expect(before.size()==48,"48 tagged pirates must spawn");
            expect(before.stream().filter(i->i.gender().equals("MALE")).count()==24,"TC-SPAWN-01 at least twenty males");
            expect(before.stream().filter(i->i.gender().equals("FEMALE")).count()==24,"TC-SPAWN-02 at least twenty females");
            long skins=before.stream().map(Identity::skin).distinct().count(),outfits=before.stream().map(Identity::clothes).distinct().count();
            expect(skins>2 && outfits>2,"TC-SPAWN-08 skin/outfit diversity");
            long hairstyles=before.stream().map(Identity::hair).distinct().count(),genomes=before.stream().map(Identity::genetics).distinct().count();
            expect(hairstyles>2 && genomes>2,"TC-SPAWN-08 hair/genetic diversity");
            System.out.println("QA_DIVERSITY count=48 skins="+skins+" outfits="+outfits+" hair="+hairstyles+" genomes="+genomes);
            verifyClient(context,before,"initial");
            if(Boolean.getBoolean("piratesmca.mcaSkin3dReview")){McaSkin3DReview.run(context,world,before);return;}
            if(System.getProperty("piratesmca.reconstructionReview")!=null){ReconstructionReview.run(context,world,before);return;}
            if(Boolean.getBoolean("piratesmca.atelierCatalogueCapture")) {
                AtelierCatalogueCapture.run(context,world,before);
                return;
            }
            context.takeScreenshot("TC-SPAWN-initial");
            tricorneViews(context,world);
            context.runOnClient(mc -> {
                for(var entity:mc.level.entitiesForRendering())if(entity instanceof VillagerEntityMCA npc) {
                    var visuals=net.conczin.mca.client.render.VillagerVisuals.capture(npc);
                    var textureId=net.conczin.mca.client.render.DynamicSkinCache.getOrCreateStitchedSkin(visuals);
                    var texture=mc.getTextureManager().getTexture(textureId);
                    System.out.println("QA_RENDER "+npc.getUUID()+" "+visuals+" texture="+textureId);
                    expect(texture instanceof net.minecraft.client.renderer.texture.DynamicTexture,"Missing stitched dynamic texture");
                    if(texture instanceof net.minecraft.client.renderer.texture.DynamicTexture dynamic) {
                        var pixels=dynamic.getPixels();
                        try { pixels.writeToFile(java.nio.file.Path.of("screenshots","texture-"+npc.getUUID()+".png")); }
                        catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
                        int opaque=0;
                        for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++)if((pixels.getPixel(x,y)>>>24)>128)opaque++;
                        expect(opaque>500,"TC-SPAWN-07 stitched texture only has "+opaque+" opaque pixels");
                    }
                }
            });
            // Three elapsed-time checkpoints, then the same resource reload used by F3+T.
            if(!Boolean.getBoolean("piratesmca.playableSmoke")) {
                context.waitTicks(480);verifyClient(context,before,"30-seconds");
                context.waitTicks(1800);verifyClient(context,before,"120-seconds");
            } else System.out.println("QA_SMOKE_TIMED_CHECKPOINTS_NOT_RUN");
            var reload=new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.CompletableFuture<Void>>();
            context.runOnClient(mc->reload.set(mc.reloadResourcePacks()));
            context.waitFor(mc->reload.get().isDone(),1200);
            reload.get().join();
            context.waitFor(mc->mc.gui.overlay()==null,600);
            verifyClient(context,before,"after-resource-reload");
            context.takeScreenshot("TC-SPAWN-after-resource-reload");
            var flagPos=new net.minecraft.core.BlockPos(2048,-60,-3);
            world.getServer().runOnServer(s->s.overworld().setBlockAndUpdate(flagPos,PirateFlag.BLOCK.defaultBlockState()));
            world.getServer().runOnServer(s->{
                var p=s.getPlayerList().getPlayers().getFirst();var level=s.overworld();
                System.out.println("QA_FLAG_VOLUNTEERS count="+PirateFlag.volunteers(level,p).size()+" player="+p.gameMode()+" candidates="+PirateCommands.freePirates(level,p,14).stream().limit(2).map(n->n.getName().getString()+" hearts="+n.getVillagerBrain().getMemoriesForPlayer(p).getHearts()+" met="+n.entityTags().contains("pmca_bond_v102_"+p.getUUID())).toList());
            });
            context.waitTicks(10);
            context.runOnClient(mc->{
                var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(flagPos),net.minecraft.core.Direction.NORTH,flagPos,false);
                mc.gameMode.useItemOn(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND,hit);
            });
            context.waitForScreen(net.minecraft.client.gui.screens.dialog.MultiButtonDialogScreen.class);
            context.waitTicks(5);
            context.takeScreenshot("FLAG-before-founding");clickDialogButton(context,"piratesmca.flag.found");context.waitTicks(20);
            world.getServer().runOnServer(s->{
                var c=CrewRuntime.playerCrew(s.overworld(),s.getPlayerList().getPlayers().getFirst());
                expect(c!=null && c.members.size()==2 && c.flag.equals(PirateFlag.anchor(s.overworld(),flagPos)),"FLAG-02 actual client dialog did not found crew");
                expect(command(s,"pirates preset nearest clear")==1,"HAT-03 captain cannot clear own hat");
                expect(command(s,"pirates preset nearest tricorne_classic")==1,"HAT-03 captain cannot restore own hat");
                System.out.println("QA_FLAG_DIALOG_FOUNDED crew="+c.id);
            });
            context.waitForScreen(net.minecraft.client.gui.screens.dialog.MultiButtonDialogScreen.class);context.takeScreenshot("FLAG-captain-menu");
            clickDialogButton(context,"piratesmca.flag.hold");context.waitTicks(20);
            world.getServer().runOnServer(s->{
                var c=CrewRuntime.playerCrew(s.overworld(),s.getPlayerList().getPlayers().getFirst());
                expect(c.socialEvents.stream().anyMatch(e->e.reason.contains("HOLD")),"Flag order button did not reach server");
            });
            context.setScreen(()->null);
            world.getServer().runOnServer(s->{
                expect(command(s,"pirates captain found")==0,"TC-CREW-01 duplicate captaincy refused");
                expect(command(s,"pirates crew status")==1,"TC-BOOT-06 status");
                expect(command(s,"pirates crew roster")==1,"roster");
                expect(commandAsOp(s,"pirates debug identity")==1,"identity diagnostics");
                deniedWithoutOp(s,"pirates captain force");
                deniedWithoutOp(s,"pirates debug identity");
            });
            world.getServer().runCommand("tp @a 2047 -60 -3 0 0");
            world.getServer().runOnServer(s->{
                expect(command(s,"pirates officer appoint nearest first_mate")==1,"TC-CREW-07 appoint first mate");
                expect(command(s,"pirates officer appoint nearest quartermaster")==1,"TC-CREW-07 switch office");
                var c=CrewRuntime.playerCrew(s.overworld(),s.getPlayerList().getPlayers().getFirst());
                expect(c.firstMate==null && c.quartermaster!=null,"TC-CREW-07 no double office");
                var originalCrew=c.id;
                expect(command(s,"pirates captain resign")==1,"TC-CREW-10 resign");
                var player=s.getPlayerList().getPlayers().getFirst();var ledger=CrewRuntime.ledger(s.overworld());
                var free=new java.util.HashSet<UUID>();
                for(var npc:s.overworld().getEntitiesOfClass(VillagerEntityMCA.class,player.getBoundingBox().inflate(14)))
                    if(player.distanceToSqr(npc)<=14*14 && ledger.isFreeSolo(ledger.member(npc.getUUID())))free.add(npc.getUUID());
                expect(commandAsOp(s,"pirates captain force")==1,"TC-CREW-02 force free sailors");
                var forced=CrewRuntime.playerCrew(s.overworld(),player);
                if(!free.isEmpty()) {
                    expect(forced.members.equals(free),"Force must recruit exactly the free sailors within 14 blocks");
                    expect(ledger.crew(originalCrew).members.size()==2,"Force merged an existing NPC crew");
                    expect(command(s,"pirates captain resign")==1,"Resign test crew");
                    expect(commandAsOp(s,"pirates captain force")==1,"TC-CREW-02 force existing crew");
                    expect(CrewRuntime.playerCrew(s.overworld(),player).id.equals(originalCrew),"Force must claim nearest existing crew without merging vessels");
                }
            });
            world.getServer().runCommand("tp @a 2048 -60 -5 0 0");
            crew=world.getServer().computeOnServer(s->CrewRuntime.playerCrew(s.overworld(),s.getPlayerList().getPlayers().getFirst()).id);
            var traveler=before.stream().filter(n->n.head().equals("piratesmca:tricorne_classic")).findFirst().orElseThrow();
            var originalPosition=world.getServer().computeOnServer(s->{
                var entity=s.overworld().getEntity(traveler.id());entity.setNoGravity(true);return entity.position();
            });
            // Keep the destination loaded without a player; an unloaded UUID is not entity loss.
            world.getServer().runCommand("execute in minecraft:the_nether run forceload add 0 0");
            context.waitTicks(20);
            world.getServer().runCommand("execute as "+traveler.id()+" in minecraft:the_nether run tp @s 0 128 0");
            context.waitTicks(20);
            world.getServer().runOnServer(s->{
                var entity=s.getLevel(net.minecraft.world.level.Level.NETHER).getEntity(traveler.id());
                expect(entity instanceof VillagerEntityMCA,"TC-SAVE-03 dimension transfer lost NPC");
                var npc=(VillagerEntityMCA)entity;
                expect(traveler.equals(new Identity(npc.getUUID(),npc.getName().getString(),npc.getSkin(),npc.getClothes(),npc.getHairStyleId(),npc.getGenetics().getGender().name(),head(npc),equipment(npc),genetics(npc))),"Dimension transfer changed identity");
                expect(CrewRuntime.ledger(s.overworld()).member(traveler.id()).crew!=null,"Dimension transfer lost crew");
            });
            world.getServer().runCommand("execute in minecraft:the_nether as "+traveler.id()+" in minecraft:overworld run tp @s "
                +originalPosition.x+" "+originalPosition.y+" "+originalPosition.z);
            context.waitTicks(40);
            world.getServer().runOnServer(s->s.overworld().getEntity(traveler.id()).setNoGravity(false));
            world.getServer().runCommand("execute in minecraft:the_nether run forceload remove 0 0");
            expect(before.equals(world.getServer().computeOnServer(PirateClientGameTests::identities)),"TC-SAVE-03 return changed identities");
            world.getServer().runCommand("tp @a 4096 -60 4096");
            context.waitTicks(400);
            world.getServer().runOnServer(s->{
                for(var id:before)expect(s.overworld().getEntity(id.id())==null,"Chunk was not unloaded for "+id.id());
                System.out.println("QA_CHUNK_UNLOAD_CONFIRMED count="+before.size());
            });
            world.getServer().runCommand("tp @a 2048 -60 -5 0 0");
            context.waitTicks(100);
            var after=world.getServer().computeOnServer(PirateClientGameTests::identities);
            expect(before.equals(after),"TC-SPAWN-05 identity after travel: "+before+" -> "+after);
            verifyClient(context,before,"after-travel");
            context.setScreen(()->null);
            context.takeScreenshot("TC-SPAWN-after-travel");
        }
        try(var reopened=save.open()) {
            context.waitTicks(100);
            var after=reopened.getServer().computeOnServer(PirateClientGameTests::identities);
            expect(before.equals(after),"TC-SPAWN-06 identity after server restart: "+before+" -> "+after);
            verifyClient(context,before,"after-server-restart");
            reopened.getServer().runOnServer(s->{
                var p=s.getPlayerList().getPlayers().getFirst();
                var c=CrewRuntime.playerCrew(s.overworld(),p);
                expect(c!=null && crew.equals(c.id) && c.members.size()==2,"TC-CREW-06 crew survives restart");
                expect(c.flag!=null && c.flag.equals(PirateFlag.anchor(s.overworld(),new net.minecraft.core.BlockPos(2048,-60,-3))) && s.overworld().getBlockState(new net.minecraft.core.BlockPos(2048,-60,-3)).is(PirateFlag.BLOCK),"FLAG-05 flag block or claim lost on restart");
                expect(command(s,"pirates crew status")==1,"TC-BOOT-06 status after restart");
            });
            context.setScreen(()->null);
            context.takeScreenshot("TC-SPAWN-after-restart");
        }
        var checkpoint=new Properties();
        checkpoint.setProperty("save",save.getSaveDirectory().toAbsolutePath().toString());
        checkpoint.setProperty("pid",Long.toString(ProcessHandle.current().pid()));
        checkpoint.setProperty("crew",crew.toString());
        for(int i=0;i<before.size();i++) {
            var n=before.get(i);String prefix="npc."+i+".";
            checkpoint.setProperty(prefix+"id",n.id().toString());checkpoint.setProperty(prefix+"name",n.name());
            checkpoint.setProperty(prefix+"skin",n.skin());checkpoint.setProperty(prefix+"clothes",n.clothes());
            checkpoint.setProperty(prefix+"hair",n.hair());checkpoint.setProperty(prefix+"gender",n.gender());checkpoint.setProperty(prefix+"head",n.head());
            checkpoint.setProperty(prefix+"equipment",n.equipment());
            checkpoint.setProperty(prefix+"genetics",n.genetics());
        }
        try(var out=java.nio.file.Files.newOutputStream(java.nio.file.Path.of("qa-client-reopen.properties"))) { checkpoint.store(out,"Full process restart checkpoint"); }
        catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
    }
    private void reopenInNewProcess(ClientGameTestContext context) {
        var p=new Properties();
        try(var in=java.nio.file.Files.newInputStream(java.nio.file.Path.of("qa-client-reopen.properties"))) {p.load(in);}
        catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        expect(!p.getProperty("pid").equals(Long.toString(ProcessHandle.current().pid())),"Restart test must use a distinct JVM process");
        var expected=new ArrayList<Identity>();
        for(int i=0;i<48;i++) {
            String prefix="npc."+i+".";
            expected.add(new Identity(UUID.fromString(p.getProperty(prefix+"id")),p.getProperty(prefix+"name"),p.getProperty(prefix+"skin"),p.getProperty(prefix+"clothes"),p.getProperty(prefix+"hair"),p.getProperty(prefix+"gender"),p.getProperty(prefix+"head","minecraft:air"),p.getProperty(prefix+"equipment"),p.getProperty(prefix+"genetics")));
        }
        var save=new net.fabricmc.fabric.impl.client.gametest.world.TestWorldSaveImpl(context,java.nio.file.Path.of(p.getProperty("save")));
        try(var world=save.open()) {
            context.waitTicks(120);
            expect(expected.equals(world.getServer().computeOnServer(PirateClientGameTests::identities)),"TC-SPAWN-06 full process restart changed server identity");
            verifyClient(context,expected,"after-full-process-restart");
            world.getServer().runOnServer(s->{
                var c=CrewRuntime.playerCrew(s.overworld(),s.getPlayerList().getPlayers().getFirst());
                expect(c!=null && c.id.toString().equals(p.getProperty("crew")) && c.members.size()==2,"Full process restart lost player crew");
            });
            context.takeScreenshot("TC-SPAWN-after-full-process-restart");
            System.out.println("QA_FULL_PROCESS_RESTART_CONFIRMED count=48 old_pid="+p.getProperty("pid")+" new_pid="+ProcessHandle.current().pid());
        }
    }
}
