package dev.arandu.piratesmca;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.conczin.mca.resources.data.skin.HairStyle;
import net.conczin.mca.resources.data.skin.LayeredHair;
import net.conczin.mca.client.render.VillagerVisuals;

/** Deterministic visual pose fixtures, not certification of autonomous AI. */
final class TricornePoseReview {
    static void expect(boolean value,String message){if(!value)throw new AssertionError(message);}
    static float combatProgress(Object client){
        try{return ((Number)Class.forName("net.bettercombat.api.MinecraftClient_BetterCombat").getMethod("getSwingProgress").invoke(client)).floatValue();}
        catch(ReflectiveOperationException e){throw new AssertionError("Better Combat runtime API unavailable",e);}
    }
    void run(ClientGameTestContext context){
        var eyeSave=new AtomicReference<net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave>();
        var eyeBefore=new AtomicReference<List<PirateClientGameTests.Identity>>();
        if(Boolean.getBoolean("piratesmca.emfExtensionsReview"))context.runOnClient(mc->{
            var loader=net.fabricmc.loader.api.FabricLoader.getInstance();
            for(String id:List.of("emf_compat_core","emf_compat_better_combat","bettercombat","player_animation_library","entity_model_features","entity_texture_features"))expect(loader.isModLoaded(id),"Required compatibility mod missing: "+id);
            var packs=mc.getResourceManager().listPacks().map(p->p.packId()).toList();
            for(String name:List.of("FreshAnimations_v1.11.2_26.1+.zip","FA+All_Extensions-v1.10.1.zip","FA+Player-v1.1.zip"))expect(packs.stream().anyMatch(n->n.contains(name)),"Required pack is not active: "+name);
            System.out.println("QA_HAT_EMF_EXTENSIONS active_packs="+packs);
        });
        var current=new AtomicReference<MinecraftServer>();var phase=new AtomicReference<>("idle");var ids=new ArrayList<UUID>();var seats=new ArrayList<Entity>();
        ServerTickEvents.END_SERVER_TICK.register(server->{
            if(server!=current.get())return;
            for(int i=0;i<ids.size();i++)if(server.overworld().getEntity(ids.get(i)) instanceof VillagerEntityMCA npc){
                String p=phase.get();
                if(p.equals("walk") || p.equals("run")){
                    double t=server.overworld().getGameTime()*(p.equals("run")?.24:.1);
                    npc.setPos(2047+i*2+Math.sin(t)*.6,-60,0);npc.setSprinting(p.equals("run"));
                }
                if(p.equals("attack") && server.overworld().getGameTime()%6==0)npc.swing(InteractionHand.MAIN_HAND,true);
                if(p.equals("swim")){npc.setSwimming(true);npc.setPose(Pose.SWIMMING);npc.setSprinting(true);}
                npc.setYRot(180);npc.setYHeadRot(p.equals("head_turn")?150:180);npc.setXRot(p.equals("head_turn")?25:0);
            }
        });
        try(var world=Boolean.getBoolean("piratesmca.sashReplayOriginal")?new net.fabricmc.fabric.impl.client.gametest.world.TestWorldSaveImpl(context,java.nio.file.Path.of(System.getProperty("piratesmca.sashFitSave"))).open():context.worldBuilder().create()){
            if(Boolean.getBoolean("piratesmca.playerReview")){reviewPlayer(context,world);return;}
            world.getServer().runCommand("tp @a 2048.5 -60.0 -3.5 0 -5");
            world.getServer().runCommand("time set 6000");
            String waistTag=(Boolean.getBoolean("piratesmca.sashReview") || Boolean.getBoolean("piratesmca.outfitReview"))?",\"pmca_preset_sash_red\"":"";
            String faceTag=(Boolean.getBoolean("piratesmca.eyepatchReview") || Boolean.getBoolean("piratesmca.sashReview"))?",\"pmca_preset_eyepatch_leather\"":"";
            if(!Boolean.getBoolean("piratesmca.sashReplayOriginal"))for(String gender:List.of("male","female"))world.getServer().runCommand("summon mca:"+gender+"_villager "+(gender.equals("male")?2047:2049)+" -60 0 {Age:0,NoAI:1b,Rotation:[180f,0f],Tags:[\"pmca_seapost\",\"pmca_friendly\",\"pmca_adult_required\",\"pmca_gender_"+gender+"\",\"pmca_preset_tricorne_classic\""+faceTag+waistTag+"]}");
            context.waitTicks(100);context.setScreen(()->null);
            if(Boolean.getBoolean("piratesmca.outfitReview"))world.getServer().runOnServer(s->{for(var a:PirateClientGameTests.identities(s)){var n=(VillagerEntityMCA)s.overworld().getEntity(a.id());System.out.println("QA_OUTFIT_BEFORE clothes="+n.getClothes()+" gender="+n.getGenetics().getGender()+" age="+n.getAgeState()+" marker="+n.entityTags().contains(PiratesOfMCA.MARKER)+" targetValid="+net.conczin.mca.resources.SkinVisualIds.isClothing(PirateOutfits.texture("deckhand"),n.getGenetics().getGender()));expect(PirateOutfits.apply(n,"deckhand") || n.getClothes().equals(PirateOutfits.texture("deckhand")),"OUTFIT-01 native MCA rejected uniform");};});
            context.waitTicks(10);
            var before=world.getServer().computeOnServer(PirateClientGameTests::identities);expect(before.size()==2,"Pose fixture requires two pirates");
            expect(before.stream().map(PirateClientGameTests.Identity::gender).distinct().count()==2,"Both genders must be represented");
            world.getServer().runOnServer(s->{current.set(s);for(var n:before)ids.add(n.id());ids.sort(Comparator.comparing(id->((VillagerEntityMCA)s.overworld().getEntity(id)).getGenetics().getGender().name()));});
            context.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();mc.options.particles().set(net.minecraft.server.level.ParticleStatus.MINIMAL);});
            for(String p:List.of("idle","head_turn","walk","run","attack","hurt","swim","sit","sleep")){
                phase.set("idle");
                world.getServer().runOnServer(s->{
                    var level=s.overworld();
                    for(var seat:seats)seat.discard();seats.clear();
                    for(var id:ids){var n=(VillagerEntityMCA)level.getEntity(id);n.stopRiding();if(n.isSleeping())n.stopSleeping();}
                    // Remove the previous fixture's fluid/bed, including flowed
                    // water, so later captures do not inherit its occlusion.
                    s.getCommands().performPrefixedCommand(s.createCommandSourceStack(),"fill 2035 -60 -10 2061 -57 10 air");
                    for(int i=0;i<ids.size();i++){
                        var npc=(VillagerEntityMCA)level.getEntity(ids.get(i));npc.stopRiding();if(npc.isSleeping())npc.stopSleeping();
                        npc.setSwimming(false);npc.setPose(Pose.STANDING);npc.setSprinting(false);npc.setNoGravity(true);npc.setDeltaMovement(Vec3.ZERO);npc.setPos(2047+i*2,-60,0);
                        for(int z=-1;z<=2;z++)for(int y=-60;y<=-59;y++)level.setBlock(new BlockPos(2047+i*2,y,z),Blocks.AIR.defaultBlockState(),2);
                        if(p.equals("swim"))for(int y=-60;y<=-59;y++)level.setBlock(new BlockPos(2047+i*2,y,0),Blocks.WATER.defaultBlockState(),2);
                        if(p.equals("attack"))npc.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
                        if(p.equals("hurt"))npc.hurtServer(level,level.damageSources().generic(),1);
                        if(p.equals("sit")){
                            var seat=EntityTypes.ARMOR_STAND.create(level,EntitySpawnReason.COMMAND);seat.setPos(npc.getX(),-60.7,0);seat.setInvisible(true);seat.setNoGravity(true);level.addFreshEntity(seat);seats.add(seat);
                            expect(npc.startRiding(seat,true,true),"Sit fixture could not mount seat");
                        }
                        if(p.equals("sleep")){
                            var foot=new BlockPos(2047+i*2,-60,0);var bed=Blocks.BED.pick(DyeColor.RED).defaultBlockState().setValue(BedBlock.FACING,Direction.SOUTH);
                            level.setBlock(foot,bed.setValue(BedBlock.PART,BedPart.FOOT),2);level.setBlock(foot.south(),bed.setValue(BedBlock.PART,BedPart.HEAD),2);npc.startSleeping(foot.south());
                        }
                    }
                    phase.set(p);
                });
                String camera=p.equals("sleep")?"2044.5 -58.5 -2.0 -49 30":p.equals("swim")?"2048.5 -57.0 -3.5 0 25":"2048.5 -60.0 -3.5 0 -5";
                world.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+camera));
                context.waitTicks(p.equals("hurt")?2:20);
                context.waitFor(mc->{
                    int ready=0;
                    for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && ids.contains(n.getUUID())){
                        boolean valid=switch(p){
                            case "walk"->n.walkAnimation.speed()>.01;
                            case "run"->n.walkAnimation.speed()>.01 && n.isSprinting();
                            case "attack"->n.attackAnim>.01;
                            case "hurt"->n.hurtTime>0;
                            case "swim"->n.getPose()==Pose.SWIMMING && n.getSwimAmount(1)>.5;
                            case "sit"->n.isPassenger();case "sleep"->n.isSleeping();default->true;
                        };
                        if(valid)ready++;
                    }
                    return ready==2;
                },120);
                context.runOnClient(mc->{
                    int count=0;
                    for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA npc && ids.contains(npc.getUUID())){
                        expect(before.stream().filter(a->a.id().equals(npc.getUUID())).findFirst().orElseThrow().head().equals(PirateClientGameTests.head(npc)),"Head loadout lost in pose "+p);
                        if(p.equals("swim"))expect(npc.getPose()==Pose.SWIMMING,"Client did not enter swim pose");
                        if(p.equals("sit"))expect(npc.isPassenger(),"Client did not enter riding/sit fixture");
                        if(p.equals("sleep"))expect(npc.isSleeping(),"Client did not enter sleep pose");
                        if(Boolean.getBoolean("piratesmca.sashReview"))SashClientViews.verifySnapshot(mc,npc,p);
                        System.out.println("QA_HAT_POSE phase="+p+" npc="+npc.getUUID()+" pose="+npc.getPose()+" walkSpeed="+npc.walkAnimation.speed()+" attack="+npc.attackAnim+" hurt="+npc.hurtTime+" passenger="+npc.isPassenger());count++;
                    }
                    expect(count==2,"Missing pose fixture clients");
                });
                context.takeScreenshot("HAT-POSE-"+p);
                if(p.equals("swim") && (Boolean.getBoolean("piratesmca.hairReview") || Boolean.getBoolean("piratesmca.sashReview"))){
                    // Secondary geometry-only view. Keep the forced swim pose,
                    // remove its occluding fluid; never label this autonomous swimming.
                    world.getServer().runOnServer(s->{
                        s.getCommands().performPrefixedCommand(s.createCommandSourceStack(),"fill 2035 -60 -10 2061 -57 10 air");
                        PirateClientGameTests.commandAsOp(s,"tp @s 2048.0 -60.5 -3.0 0 5");
                    });
                    context.waitTicks(10);
                    context.runOnClient(mc->{
                        int ready=0;
                        for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && ids.contains(n.getUUID())){
                            expect(n.getPose()==Pose.SWIMMING && n.getSwimAmount(1)>.5,"Dry swim fit lost the controlled swim pose");
                            expect(PirateWearables.isCosmetic(n.getItemBySlot(EquipmentSlot.HEAD)),"Dry swim fit lost head cosmetics");ready++;
                        }
                        expect(ready==2,"Dry swim fit missing an NPC");
                    });
                    context.takeScreenshot("HAT-FIT-swim-dry");
                    System.out.println("QA_HAT_DRY_SWIM_FIT entities=2 synthetic=true");
                }
            }
            phase.set("idle");
            world.getServer().runOnServer(s->{for(var id:ids){var npc=(VillagerEntityMCA)s.overworld().getEntity(id);npc.stopSleeping();npc.stopRiding();npc.setSwimming(false);npc.setPose(Pose.STANDING);npc.setPos(npc.getX(),-60,0);}for(var seat:seats)seat.discard();});
            var after=world.getServer().computeOnServer(PirateClientGameTests::identities);expect(before.equals(after),"Pose fixtures changed identity or head equipment");
            System.out.println("QA_HAT_POSES_COMPLETE count=9 entities=2 fixture_only=true");
            if(Boolean.getBoolean("piratesmca.sashReview"))SashClientViews.run(context,world,ids);
            if(Boolean.getBoolean("piratesmca.outfitReview"))OutfitClientViews.run(context,world,ids);
            if(Boolean.getBoolean("piratesmca.sashReplayOriginal")){
                System.out.println("QA_SASH_REPLAY_GENETICS "+world.getServer().computeOnServer(SashFitReview::state));
                SashFitReview.views(context,world,"after-poses-reset");return;
            }
            if(Boolean.getBoolean("piratesmca.eyepatchReview")){EyepatchClientViews.run(context,world,ids);expect(before.equals(world.getServer().computeOnServer(PirateClientGameTests::identities)),"Eyepatch views changed identity");}
            if(Boolean.getBoolean("piratesmca.hairReview")){
                var originalLayers=world.getServer().computeOnServer(s->{
                    var all=new HashMap<UUID,Map<LayeredHair.Category,String>>();
                    for(var id:ids){var n=(VillagerEntityMCA)s.overworld().getEntity(id);var layers=new EnumMap<LayeredHair.Category,String>(LayeredHair.Category.class);for(var c:LayeredHair.Category.values())layers.put(c,n.getLayeredHair(c));all.put(id,layers);}
                    return all;
                });
                // Explicit legacy textures from the supplied MCA JAR; this is not
                // coverage of every layered hairstyle or hair/pose combination.
                for(int style:List.of(0,10,20,39)){
                    world.getServer().runOnServer(s->{
                        s.getCommands().performPrefixedCommand(s.createCommandSourceStack(),"fill 2035 -60 -10 2061 -57 10 air");
                        for(int i=0;i<ids.size();i++){
                            var n=(VillagerEntityMCA)s.overworld().getEntity(ids.get(i));
                            n.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                            n.setPos(2047+i*2,-60,0);
                            String texture="mca:skins/hair/"+n.getGenetics().getGender().name().toLowerCase(Locale.ROOT)+"/"+style+".png";
                            // setHair(String) only updates the style ID in MCA 8.1.11;
                            // HairLayer reads the five actual layers, not that ID.
                            n.setHairStyle(HairStyle.singleLayer(texture,n.getGenetics().getGender(),1));
                        }
                    });
                    var expected=world.getServer().computeOnServer(PirateClientGameTests::identities);
                    for(var a:expected){
                        var original=before.stream().filter(n->n.id().equals(a.id())).findFirst().orElseThrow();
                        expect(new PirateClientGameTests.Identity(a.id(),a.name(),a.skin(),a.clothes(),original.hair(),a.gender(),a.head(),a.equipment(),a.genetics()).equals(original),"Hair selection changed another identity field");
                    }
                    for(String view:List.of("front","profile","back")){
                        String camera=switch(view){case "profile"->"2044.0 -60.0 -2.5 -48 -5";case "back"->"2048.0 -60.0 3.5 180 -5";default->"2048.0 -60.0 -3.5 0 -5";};
                        world.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+camera));
                        String[] xyz=camera.split(" ");double x=Double.parseDouble(xyz[0]),y=Double.parseDouble(xyz[1]),z=Double.parseDouble(xyz[2]);
                        context.waitFor(mc->mc.player.distanceToSqr(x,y,z)<.1,120);
                        context.waitFor(mc->expected.stream().allMatch(a->{
                            for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && n.getUUID().equals(a.id()))return n.getHairStyleId().equals(a.hair()) && n.getHairBase().equals(a.hair());
                            return false;
                        }),120);
                        context.waitTicks(8);
                        context.runOnClient(mc->{
                            for(var a:expected){
                                expect(mc.getResourceManager().getResource(Identifier.parse(a.hair())).isPresent(),"MCA hair texture missing: "+a.hair());
                                VillagerEntityMCA actual=null;
                                for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n && n.getUUID().equals(a.id()))actual=n;
                                expect(actual!=null,"Missing client hair fixture");
                                var visual=VillagerVisuals.capture(actual);
                                expect(visual.hairBase().equals(a.hair()),"Renderer snapshot did not receive selected hair");
                                expect(visual.hairBangs().isBlank() && visual.hairBack().isBlank() && visual.hairFront().isBlank() && visual.hairExtra().isBlank(),"Old layered hair is obscuring selected texture");
                                expect(a.equals(new PirateClientGameTests.Identity(actual.getUUID(),actual.getName().getString(),actual.getSkin(),actual.getClothes(),actual.getHairStyleId(),actual.getGenetics().getGender().name(),PirateClientGameTests.head(actual),PirateClientGameTests.equipment(actual),PirateClientGameTests.genetics(actual))),"Hair fixture client identity mismatch");
                                System.out.println("QA_HAT_HAIR style="+style+" view="+view+" identity="+a);
                                System.out.println("QA_HAT_RENDER_HAIR style="+style+" view="+view+" base="+visual.hairBase()+" other_layers=empty");
                            }
                        });
                        context.takeScreenshot("HAT-HAIR-"+style+"-"+view);
                    }
                }
                world.getServer().runOnServer(s->{for(var a:before){var n=(VillagerEntityMCA)s.overworld().getEntity(a.id());n.setHairStyleId(a.hair());for(var c:LayeredHair.Category.values()){n.setLayeredHair(c,originalLayers.get(a.id()).get(c));expect(n.getLayeredHair(c).equals(originalLayers.get(a.id()).get(c)),"Original hair layer was not restored");}}});
                expect(before.equals(world.getServer().computeOnServer(PirateClientGameTests::identities)),"Hair review did not restore original identity");
                System.out.println("QA_HAT_HAIR_COMPLETE textures=8 views=12 entities=2 fixture_only=true");
            }
            if(Boolean.getBoolean("piratesmca.emfExtensionsReview")){
                reviewPlayer(context,world);
            }
            if(Boolean.getBoolean("piratesmca.eyepatchReview") || Boolean.getBoolean("piratesmca.sashReview")){
                eyeBefore.set(EyepatchPersistenceReview.transitions(context,world,ids));eyeSave.set(world.getWorldSave());
            }
        }finally{current.set(null);ids.clear();context.runOnClient(mc->{mc.options.keyAttack.setDown(false);mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);if(mc.gui.hud.isHidden())mc.gui.hud.toggle();});}
        if(eyeSave.get()!=null)try(var reopened=eyeSave.get().open()){
            context.waitTicks(100);
            if(Boolean.getBoolean("piratesmca.sashReview"))reopened.getServer().runOnServer(server->{for(var a:eyeBefore.get())expect(((VillagerEntityMCA)server.overworld().getEntity(a.id())).getItemBySlot(EquipmentSlot.CHEST).is(PirateSash.ITEM),"SASH-08 reopen lost chest equipment");});
            expect(eyeBefore.get().equals(reopened.getServer().computeOnServer(PirateClientGameTests::identities)),"EYE-07 reopened world changed identity");
            PirateClientGameTests.verifyClient(context,eyeBefore.get(),"EYE-after-reopen");context.setScreen(()->null);context.takeScreenshot("EYE-after-reopen");
            System.out.println("QA_EYEPATCH_REOPEN_PASS entities=2 integrated_server=true");
        }
    }
    static void reviewPlayer(ClientGameTestContext context,net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext world){
                world.getServer().runOnServer(s->{for(var player:s.getPlayerList().getPlayers()){
                    System.out.println("QA_PLAYER_FIXTURE initially_invisible="+player.isInvisible());
                    player.removeAllEffects();player.setInvisible(false);
                }});
                world.getServer().runCommand("gamemode survival @a");
                world.getServer().runCommand("item replace entity @a weapon.mainhand with "+(Boolean.getBoolean("piratesmca.atelierReview")?"pirateatelier:naval_cutlass":"minecraft:iron_sword"));
                world.getServer().runCommand("item replace entity @a armor.head with minecraft:air");
                world.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s 2056.0 -60.0 -3.0 180 0"));
                context.waitTicks(10);
                context.waitFor(mc->!mc.player.isInvisible(),120);
                context.setScreen(()->null);
                context.runOnClient(mc->{
                    if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();
                    mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
                    System.out.println("QA_PLAYER_RENDER_DIAGNOSTIC invisible="+mc.player.isInvisible()+" spectator="+mc.player.isSpectator()+" renderer="+mc.getEntityRenderDispatcher().getRenderer(mc.player).getClass().getName()+" mca_player_skin="+net.conczin.mca.MCAClient.getPlayerData(mc.player.getUUID()).map(d->d.getSkin()).orElse("absent")+" mca_villager_renderer="+net.conczin.mca.MCAClient.getVillagerRendererData(mc.player.getUUID()).isPresent());
                });
                context.waitTicks(20);context.takeScreenshot("HAT-PLAYER-no-hat-idle");
                world.getServer().runCommand("item replace entity @a armor.head with piratesmca:"+((Boolean.getBoolean("piratesmca.eyepatchReview") || Boolean.getBoolean("piratesmca.sashReview"))?"tricorne_eyepatch":"tricorne_classic"));
                if(Boolean.getBoolean("piratesmca.sashReview"))world.getServer().runCommand("item replace entity @a armor.chest with piratesmca:sash_red");
                context.waitTicks(20);context.takeScreenshot("HAT-PLAYER-hat-idle");
                context.runOnClient(mc->{mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);mc.options.keyAttack.setDown(true);net.minecraft.client.KeyMapping.click(mc.options.keyAttack.getDefaultKey());});
                // Better Combat returns 1 at rest. Checking only >0 would pass
                // without any attack; require an actual in-progress swing.
                context.waitFor(mc->combatProgress(mc)>.01 && combatProgress(mc)<.8,160);
                context.runOnClient(mc->{
                    expect(mc.player.getItemBySlot(EquipmentSlot.HEAD).is((Boolean.getBoolean("piratesmca.eyepatchReview") || Boolean.getBoolean("piratesmca.sashReview"))?PirateWearables.TRICORNE_EYEPATCH:PirateWearables.TRICORNE),"Player hat not synchronized");
                    expect(combatProgress(mc)>0 && combatProgress(mc)<1,"Better Combat is not actively swinging");
                    if(Boolean.getBoolean("piratesmca.sashReview"))expect(mc.player.getItemBySlot(EquipmentSlot.CHEST).is(PirateSash.ITEM),"SASH-09 player chest not synchronized");
                    System.out.println("QA_BETTERCOMBAT_PLAYER_SWING progress="+combatProgress(mc)+" third_person=true player_pack=true");
                });
                context.takeScreenshot("HAT-PLAYER-bettercombat-attack");
                context.runOnClient(mc->{mc.options.keyAttack.setDown(false);mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);});
    }
}
