package dev.arandu.piratesmca;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;

/** Captures isolated skins, armor families and peg legs against the live MCA renderer. */
final class VisualAssetAudit {
    private static final List<String> VIEWS=List.of("front","back","left","right");
    private static final Map<String,EquipmentSlot> SLOTS=Map.of(
        "helmet",EquipmentSlot.HEAD,"chestplate",EquipmentSlot.CHEST,
        "leggings",EquipmentSlot.LEGS,"boots",EquipmentSlot.FEET);
    private static final List<String> FAMILIES=List.of(
        "captain_armor","first_mate_armor","quartermaster_armor","boarding_raider_armor",
        "cutlass_duelist_armor","gunner_armor","heavy_bruiser_armor","naval_officer_armor",
        "cursed_ghost_armor","treasure_keeper_armor");
    private static final double X=2048,Z=0,Y=-60;
    private static final AtomicReference<String> PHASE=new AtomicReference<>("idle");
    private static List<UUID> AUDIT_IDS=List.of();
    private static String safe(String s){return s.replace(':','_').replace('/','_');}
    private static ItemStack item(String id){return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));}

    static void run(ClientGameTestContext c,TestSingleplayerContext w,List<PirateClientGameTests.Identity> identities){
        System.out.println("QA_VISUAL_CAPTURE_BEGIN epoch_ms="+System.currentTimeMillis());
        c.runOnClient(mc->EquipmentPoseContract.run());
        var male=identities.stream().filter(i->i.gender().equalsIgnoreCase("male")).findFirst().orElseThrow();
        var female=identities.stream().filter(i->i.gender().equalsIgnoreCase("female")).findFirst().orElseThrow();
        AUDIT_IDS=identities.stream().map(PirateClientGameTests.Identity::id).toList();
        var actor=new AtomicReference<UUID>();var serverRef=new AtomicReference<net.minecraft.server.MinecraftServer>();
        ServerTickEvents.END_SERVER_TICK.register(s->{
            if(s!=serverRef.get()||actor.get()==null)return;
            if(s.overworld().getEntity(actor.get()) instanceof VillagerEntityMCA n){
                String p=PHASE.get();double t=s.overworld().getGameTime()*(p.equals("run")?.24:.1);
                if(p.equals("walk")||p.equals("run")){n.setPos(X+Math.sin(t)*.6,Y,Z);n.setSprinting(p.equals("run"));}
                if(p.equals("attack")&&s.overworld().getGameTime()%6==0)n.swing(n.getMainHandItem().isEmpty()&&!n.getOffhandItem().isEmpty()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND,true);
                float rotation=p.equals("turn")?180+(float)Math.sin(t)*75:180;n.setYRot(rotation);n.setYHeadRot(rotation);n.yBodyRot=rotation;n.setXRot(0);
            }
        });
        c.runOnClient(mc->{
            if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();mc.options.particles().set(net.minecraft.server.level.ParticleStatus.MINIMAL);mc.options.fov().set(45);
            var packNames=mc.getResourceManager().listPacks().map(p->p.packId()).toList();
            PirateClientGameTests.expect(packNames.stream().anyMatch(p->p.contains("FreshAnimations_v1.11.2_26.1+"))!=Boolean.getBoolean("piratesmca.disableFreshAnimations"),"Fresh Animations A/B pack selection mismatch");
            System.out.println("QA_VISUAL_ACTIVE_PACKS "+packNames);
        });
        w.getServer().runCommand("gamemode spectator @a");w.getServer().runCommand("time set 6000");
        w.getServer().runOnServer(s->{
            serverRef.set(s);s.overworld().getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME,false,s);
            s.overworld().getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_WEATHER,false,s);
            for(var identity:identities)if(s.overworld().getEntity(identity.id()) instanceof VillagerEntityMCA n){
                n.setNoAi(true);n.setNoGravity(true);n.setInvisible(true);n.setCustomNameVisible(false);n.setPos(X+20,Y,Z+20);
                for(var slot:SLOTS.values())n.setItemSlot(slot,ItemStack.EMPTY);
                n.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
            }
        });
        int shots=0,nativeCount=0,skinCount=0,familyCount=0;boolean pegOnly=Boolean.getBoolean("piratesmca.visualPegOnly");
        boolean focused=System.getProperty("piratesmca.visualFixReview")!=null;
        boolean full=focused&&System.getProperty("piratesmca.visualFixReview").startsWith("full");
        if(focused&&System.getProperty("piratesmca.visualFixReview").startsWith("avatar")){avatarReview(c,w);return;}
        if(focused&&System.getProperty("piratesmca.visualFixReview").startsWith("variants")){variants(c,w,male,female);return;}
        if(focused&&System.getProperty("piratesmca.visualFixReview").startsWith("heads")){heads(c,w,male,female);return;}
        if(focused&&(System.getProperty("piratesmca.visualFixReview").startsWith("prototypes")||System.getProperty("piratesmca.visualFixReview").startsWith("patch"))){prototypes(c,w,male,female,actor);return;}
        if(focused&&System.getProperty("piratesmca.visualFixReview").startsWith("weapons")){weapons(c,w,male,female,actor);return;}
        if(!pegOnly&&(!focused||full)){
            var nativeSkins=net.conczin.mca.resources.BuiltInSkinCatalog.get().bodySkins().values().stream()
                .sorted(Comparator.comparing((net.conczin.mca.resources.data.skin.BodySkin b)->b.getGender().name()).thenComparing(net.conczin.mca.resources.data.skin.BodySkin::getIdentifier)).toList();
            PirateClientGameTests.expect(nativeSkins.size()==20,"MCA body skin count changed: "+nativeSkins.size());
            for(var body:nativeSkins){
                var identity=body.getGender().name().equalsIgnoreCase("female")?female:male;actor.set(identity.id());
                clearAndPlaceNative(w,identity.id(),body.getIdentifier());verifyNativeSkin(c,identity.id(),body.getIdentifier());
                String label="MCA-SKIN-"+body.getGender().name().toLowerCase(Locale.ROOT)+"-"+body.getIdentifier().substring(body.getIdentifier().lastIndexOf('/')+1).replace(".png","");
                shots+=staticViews(c,w,label);shots+=poses(c,w,identity.id(),label,false);
                nativeCount++;
                System.out.println("QA_VISUAL_MCA_SKIN_CAPTURED id="+body.getIdentifier()+" gender="+body.getGender()+" clean=true");
            }
        }
        if(!pegOnly&&(!focused||full))for(var skin:PirateAtelier.SKINS){
            var identity=skin.gender().equalsIgnoreCase("female")?female:male;actor.set(identity.id());
            clearAndPlace(w,identity.id(),skin.id(),"",false);
            verifyActor(c,identity.id(),skin.id(),null);
            shots+=staticViews(c,w,"SKIN-"+skin.name());
            shots+=poses(c,w,identity.id(),"SKIN-"+skin.name(),false);
            skinCount++;
            System.out.println("QA_VISUAL_SKIN_CAPTURED id="+skin.id()+" gender="+skin.gender()+" clean=true");
            if(full){
                w.getServer().runOnServer(s->((VillagerEntityMCA)s.overworld().getEntity(identity.id())).setClothes(PirateOutfits.texture("deckhand")));
                c.waitTicks(8);shots+=staticViews(c,w,"CLOTHED-"+skin.name());shots+=poses(c,w,identity.id(),"CLOTHED-"+skin.name(),false);
                String base=FAMILIES.get((skinCount-1)%FAMILIES.size())+"_pristine_normal";
                clearAndPlace(w,identity.id(),skin.id(),base,true);verifyActor(c,identity.id(),skin.id(),base);
                shots+=staticViews(c,w,"EQUIPPED-"+skin.name());shots+=poses(c,w,identity.id(),"EQUIPPED-"+skin.name(),true);
            }
        }
        if(!pegOnly)for(String family:FAMILIES){
            if(focused&&(System.getProperty("piratesmca.visualFixReview").startsWith("before")||System.getProperty("piratesmca.visualFixReview").startsWith("probe"))&&!List.of("first_mate_armor","cutlass_duelist_armor","gunner_armor","cursed_ghost_armor").contains(family))continue;
          for(var wearing:full?List.of(male,female):List.of(male)){
            actor.set(wearing.id());String base=family+"_pristine_normal";
            String selected=Arrays.stream(PirateAtelier.SKINS).filter(s->s.gender().equalsIgnoreCase(wearing.gender())).findFirst().orElseThrow().id();
            String label="ARMOR-"+family+(full?"-"+wearing.gender().toLowerCase(Locale.ROOT):"");
            clearAndPlace(w,wearing.id(),selected,base,true);
            verifyActor(c,wearing.id(),selected,base);
            shots+=staticViews(c,w,label);
            shots+=poses(c,w,wearing.id(),label,true);
            if(focused)shots+=sequence(c,w,wearing.id(),label);
            familyCount++;
            System.out.println("QA_VISUAL_ARMOR_CAPTURED family="+family+" base="+base+" gender="+wearing.gender()+" isolated=true");
          }
        }
        for(String side:List.of("peg_left","peg_right")){
            actor.set(male.id());String base="captain_armor_pristine_"+side;
            if(pegOnly||focused)clearAndPlacePeg(w,male.id(),PirateAtelier.SKINS[0].id(),side);
            else clearAndPlace(w,male.id(),PirateAtelier.SKINS[0].id(),base,true);
            if(pegOnly||focused)verifyPeg(c,male.id(),side);else verifyActor(c,male.id(),PirateAtelier.SKINS[0].id(),base);
            shots+=staticViews(c,w,"PEG-"+side);
            shots+=poses(c,w,male.id(),"PEG-"+side,true);
            if(focused)shots+=sequence(c,w,male.id(),"PEG-"+side);
            c.runOnClient(mc->{
                VillagerEntityMCA n=find(mc,male.id());var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);
                var state=(net.minecraft.client.renderer.entity.state.HumanoidRenderState)renderer.createRenderState();renderer.extractRenderState(n,state,0);
                var limbs=((dev.arandu.piratesmca.client.PirateLimbsData)state).piratesmca$limbs();
                PirateClientGameTests.expect(Arrays.stream(limbs).anyMatch(a->!a.isEmpty()),"Peg leg is equipped but no prosthetic limb entered the actual render state: "+side);
                System.out.println("QA_VISUAL_PEG_RENDER_STATE side="+side+" active_parts="+Arrays.stream(limbs).filter(a->!a.isEmpty()).count()+" pose=standing");
            });
        }
        PHASE.set("idle");
        System.out.println("QA_VISUAL_AUDIT_COMPLETE mca_skins="+nativeCount+" pirate_skins="+skinCount+" families="+familyCount+" peg_sides=2 shots="+shots+" angles=front/back/left/right poses=walk/run/attack fresh_animations="+!Boolean.getBoolean("piratesmca.disableFreshAnimations")+" peg_only="+pegOnly);
        if(full)avatarReview(c,w);
    }

    private static void avatarReview(ClientGameTestContext c,TestSingleplayerContext w){
        PHASE.set("idle");w.getServer().runOnServer(s->{for(var id:AUDIT_IDS)if(s.overworld().getEntity(id) instanceof VillagerEntityMCA n)n.setInvisible(true);});
        c.runOnClient(mc->{var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(mc.player);var state=(net.minecraft.client.renderer.entity.state.AvatarRenderState)renderer.createRenderState();renderer.extractRenderState(mc.player,state,0);
            System.setProperty("piratesmca.reconstructionReview","visual-fix-"+state.skin.model().name().toLowerCase(Locale.ROOT)+"-fresh-"+!Boolean.getBoolean("piratesmca.disableFreshAnimations"));});
        ReconstructionAvatarReview.run(c,w);System.clearProperty("piratesmca.reconstructionReview");
    }

    private record GeometryGroup(String asset,String sha256_geometry_uv,List<String> aliases){}
    private static void prototypes(ClientGameTestContext c,TestSingleplayerContext w,PirateClientGameTests.Identity male,PirateClientGameTests.Identity female,AtomicReference<UUID> actor){
        var items=Map.of("tricorne_classic",PirateWearables.TRICORNE,"eyepatch_leather",PirateWearables.EYEPATCH,"tricorne_eyepatch",PirateWearables.TRICORNE_EYEPATCH,"sash_red",PirateSash.ITEM);int shots=0;
        boolean probe=System.getProperty("piratesmca.visualFixReview","").startsWith("patch");
        for(var item:items.entrySet())for(var identity:List.of(male,female)){
            if(probe&&!item.getKey().contains("eyepatch"))continue;
            String skin=Arrays.stream(PirateAtelier.SKINS).filter(s->s.gender().equalsIgnoreCase(identity.gender())).findFirst().orElseThrow().id();actor.set(identity.id());
            clearAndPlace(w,identity.id(),skin,"",false);var slot=item.getKey().equals("sash_red")?EquipmentSlot.CHEST:EquipmentSlot.HEAD;
            w.getServer().runOnServer(s->((VillagerEntityMCA)s.overworld().getEntity(identity.id())).setItemSlot(slot,new ItemStack(item.getValue())));
            c.waitFor(mc->{var n=find(mc,identity.id());return n!=null&&n.getItemBySlot(slot).is(item.getValue());},160);
            String label="PROTOTYPE-"+item.getKey()+"-"+identity.gender().toLowerCase(Locale.ROOT);
            shots+=staticViews(c,w,label);shots+=poses(c,w,identity.id(),label,false);if(!probe)shots+=sequence(c,w,identity.id(),label);
        }
        System.out.println("QA_PROTOTYPES_COMPLETE items="+(probe?2:4)+" genders=2 shots="+shots);
    }
    private static void weapons(ClientGameTestContext c,TestSingleplayerContext w,PirateClientGameTests.Identity male,PirateClientGameTests.Identity female,AtomicReference<UUID> actor){
        int shots=0;
        for(var weapon:PirateAtelier.WEAPONS)for(var identity:List.of(male,female))for(boolean left:List.of(false,true)){
            String skin=Arrays.stream(PirateAtelier.SKINS).filter(s->s.gender().equalsIgnoreCase(identity.gender())).findFirst().orElseThrow().id();actor.set(identity.id());clearAndPlace(w,identity.id(),skin,"",false);
            var slot=left?EquipmentSlot.OFFHAND:EquipmentSlot.MAINHAND;
            w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(identity.id());n.setLeftHanded(false);n.setItemSlot(slot,new ItemStack(weapon));});
            c.waitFor(mc->{var n=find(mc,identity.id());return n!=null&&n.getItemBySlot(slot).is(weapon);},160);
            String name=BuiltInRegistries.ITEM.getKey(weapon).getPath(),label="WEAPON-"+name+"-"+identity.gender().toLowerCase(Locale.ROOT)+(left?"-left":"-right");
            shots+=staticViews(c,w,label);shots+=poses(c,w,identity.id(),label,false);
            if(name.equals("naval_cutlass")||name.contains("rapier"))shots+=sequence(c,w,identity.id(),label);
        }
        System.out.println("QA_HELD_WEAPONS_COMPLETE items="+PirateAtelier.WEAPONS.size()+" genders=2 hands=2 shots="+shots);
    }
    private static void heads(ClientGameTestContext c,TestSingleplayerContext w,PirateClientGameTests.Identity male,PirateClientGameTests.Identity female){
        for(String name:List.of("captain_civilian","the_cursed_sailor","deckhand_f","captain_civilian_f","native_female")){
            boolean nativeSkin=name.equals("native_female");
            var skin=Arrays.stream(PirateAtelier.SKINS).filter(s->s.name().equals(name)).findFirst().orElse(null);
            var identity=nativeSkin||skin.gender().equals("female")?female:male;
            if(nativeSkin){clearAndPlaceNative(w,identity.id(),"mca:skins/skin/female/1.png");verifyNativeSkin(c,identity.id(),"mca:skins/skin/female/1.png");}
            else{clearAndPlace(w,identity.id(),skin.id(),"",false);verifyActor(c,identity.id(),skin.id(),null);}
            w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s 2048 -60.4 -1.2 0 0"));c.waitTicks(8);
            for(boolean closed:List.of(false,true)){
                c.waitFor(mc->{var n=find(mc,identity.id());return n!=null&&net.conczin.mca.client.render.VillagerVisuals.capture(n).isBlinking()==closed;},400);
                c.runOnClient(mc->{var n=find(mc,identity.id());System.out.println("QA_FACE_CAPTURE skin="+n.getSkin()+" native_hair="+n.getHairStyleId()+" closed="+net.conczin.mca.client.render.VillagerVisuals.capture(n).isBlinking());});
                c.takeScreenshot("HEAD-"+name+(closed?"-CLOSED":"-OPEN"));
            }
        }
        System.out.println("QA_FACE_CAPTURE_COMPLETE pirate_skins=4 native_control=1 states=open/closed shots=10");
    }
    private static void variants(ClientGameTestContext c,TestSingleplayerContext w,PirateClientGameTests.Identity male,PirateClientGameTests.Identity female){
        GeometryGroup[] groups;
        try(var in=VisualAssetAudit.class.getResourceAsStream("/data/piratesmca-tests/visual_groups.json")){
            if(in==null)throw new IllegalStateException("Build the visual geometry group manifest first");
            groups=new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),GeometryGroup[].class);
        }catch(java.io.IOException error){throw new java.io.UncheckedIOException(error);}
        int shots=0,configs=0;
        for(var group:groups)for(var identity:List.of(male,female)){
            String skin=Arrays.stream(PirateAtelier.SKINS).filter(s->s.gender().equalsIgnoreCase(identity.gender())).findFirst().orElseThrow().id();
            clearAndPlace(w,identity.id(),skin,"",false);
            var selected=new java.util.EnumMap<EquipmentSlot,net.minecraft.world.item.Item>(EquipmentSlot.class);
            for(var entry:PirateAtelier.ITEMS.entrySet())if(entry.getValue().kind().equals("armor")&&group.asset().equals(entry.getValue().asset()))selected.put(SLOTS.get(entry.getValue().slot()),entry.getKey());
            PirateClientGameTests.expect(!selected.isEmpty(),"QA variant has no registered items: "+group.asset());
            w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(identity.id());selected.forEach((slot,item)->n.setItemSlot(slot,new ItemStack(item)));});
            c.waitFor(mc->{var n=find(mc,identity.id());return n!=null&&selected.entrySet().stream().allMatch(e->n.getItemBySlot(e.getKey()).is(e.getValue()));},160);
            shots+=staticViews(c,w,"VARIANT-"+safe(group.asset())+"-"+identity.gender().toLowerCase(Locale.ROOT));configs++;
            System.out.println("QA_VISUAL_VARIANT_CAPTURED asset="+group.asset()+" geometry="+group.sha256_geometry_uv()+" aliases="+group.aliases().size()+" gender="+identity.gender()+" pieces="+selected.size());
        }
        int accessoryCount=0;
        for(var entry:PirateAtelier.ITEMS.entrySet())if(List.of("hook","peg","accessory").contains(entry.getValue().kind())){accessoryCount++;for(var identity:List.of(male,female)){
            String skin=Arrays.stream(PirateAtelier.SKINS).filter(s->s.gender().equalsIgnoreCase(identity.gender())).findFirst().orElseThrow().id();
            for(boolean left:List.of(false,true)){
                clearAndPlace(w,identity.id(),skin,"",false);
                var slot=entry.getValue().kind().equals("peg")?EquipmentSlot.FEET:(left?EquipmentSlot.OFFHAND:EquipmentSlot.MAINHAND);
                w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(identity.id());n.setLeftHanded(false);n.setItemSlot(slot,new ItemStack(entry.getKey()));if(left&&slot==EquipmentSlot.FEET)n.setItemSlot(EquipmentSlot.LEGS,item("pirateatelier:captain_armor_pristine_peg_left_leggings"));});
                c.waitFor(mc->{var n=find(mc,identity.id());return n!=null&&n.getItemBySlot(slot).is(entry.getKey());},160);
                shots+=staticViews(c,w,"ACCESSORY-"+safe(entry.getValue().id())+"-"+identity.gender().toLowerCase(Locale.ROOT)+(left?"-left":"-right"));
            }
        }}
        System.out.println("QA_VISUAL_VARIANTS_COMPLETE geometry_groups="+groups.length+" gender_configurations="+configs+" accessory_items="+accessoryCount+" sides=2 genders=2 shots="+shots);
    }

    private static void clearAndPlace(TestSingleplayerContext w,UUID id,String skin,String armor,boolean equip){
        w.getServer().runOnServer(s->{for(var other:AUDIT_IDS)if(!other.equals(id)&&s.overworld().getEntity(other) instanceof VillagerEntityMCA previous){previous.setInvisible(true);previous.setCustomNameVisible(false);previous.setPos(X+20,Y,Z+20);}
            var n=(VillagerEntityMCA)s.overworld().getEntity(id);n.setInvisible(false);n.setCustomNameVisible(false);n.setSkin(skin);n.setClothes("");n.setPos(X,Y,Z);n.setNoAi(true);n.setNoGravity(true);n.setYRot(180);n.setYHeadRot(180);n.setXRot(0);n.setSprinting(false);n.setPose(Pose.STANDING);n.setSwimming(false);
            for(var slot:SLOTS.values())n.setItemSlot(slot,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
            if(equip)for(var e:SLOTS.entrySet())n.setItemSlot(e.getValue(),item("pirateatelier:"+armor+"_"+e.getKey()));
        });
    }
    private static void clearAndPlacePeg(TestSingleplayerContext w,UUID id,String skin,String side){
        w.getServer().runOnServer(s->{for(var other:AUDIT_IDS)if(!other.equals(id)&&s.overworld().getEntity(other) instanceof VillagerEntityMCA previous){previous.setInvisible(true);previous.setCustomNameVisible(false);previous.setPos(X+20,Y,Z+20);}
            var n=(VillagerEntityMCA)s.overworld().getEntity(id);n.setInvisible(false);n.setCustomNameVisible(false);n.setSkin(skin);n.setPos(X,Y,Z);n.setNoAi(true);n.setNoGravity(true);n.setYRot(180);n.setYHeadRot(180);n.setSprinting(false);n.setPose(Pose.STANDING);
            for(var slot:SLOTS.values())n.setItemSlot(slot,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
            if(side.equals("peg_left"))n.setItemSlot(EquipmentSlot.LEGS,item("pirateatelier:captain_armor_pristine_peg_left_leggings"));
            else n.setItemSlot(EquipmentSlot.FEET,item("pirateatelier:atelier_peg_leg_wood"));
        });
    }
    private static void clearAndPlaceNative(TestSingleplayerContext w,UUID id,String skin){
        w.getServer().runOnServer(s->{for(var other:AUDIT_IDS)if(!other.equals(id)&&s.overworld().getEntity(other) instanceof VillagerEntityMCA previous){previous.setInvisible(true);previous.setCustomNameVisible(false);previous.setPos(X+20,Y,Z+20);}
            var n=(VillagerEntityMCA)s.overworld().getEntity(id);n.setInvisible(false);n.setCustomNameVisible(false);n.setSkin(skin);n.setClothes("");n.setPos(X,Y,Z);n.setNoAi(true);n.setNoGravity(true);n.setYRot(180);n.setYHeadRot(180);n.setXRot(0);n.setSprinting(false);n.setPose(Pose.STANDING);n.setSwimming(false);
            for(var slot:SLOTS.values())n.setItemSlot(slot,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
        });
    }
    private static void verifyNativeSkin(ClientGameTestContext c,UUID id,String skin){
        c.waitFor(mc->{var n=find(mc,id);return n!=null&&!n.isInvisible()&&n.getSkin().equals(skin)&&n.getClothes().isEmpty()&&SLOTS.values().stream().allMatch(slot->n.getItemBySlot(slot).isEmpty());},160);
        c.runOnClient(mc->{var n=find(mc,id);PirateClientGameTests.expect(n!=null,"MCA skin specimen missing: "+skin);
            var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);var state=(net.minecraft.client.renderer.entity.state.HumanoidRenderState)renderer.createRenderState();renderer.extractRenderState(n,state,0);
            var skinLayer=new net.conczin.mca.client.render.layer.SkinLayer(renderer,(net.minecraft.client.model.HumanoidModel)renderer.getModel());
            PirateClientGameTests.expect(skinLayer.getSkin(state).equals(Identifier.parse(skin)),"MCA renderer chose another body skin: "+skin);
            var clothes=new net.conczin.mca.client.render.layer.ClothingLayer(renderer,(net.minecraft.client.model.HumanoidModel)renderer.getModel(),"normal");
            PirateClientGameTests.expect(clothes.getSkin(state)==null,"MCA clothing layer obscures isolated body skin: "+skin);
            PirateClientGameTests.expect(n.getGenetics().getGender().name().equalsIgnoreCase(skin.contains("/female/")?"female":"male"),"MCA body skin/gender mismatch: "+skin);
        });
    }
    private static void verifyPeg(ClientGameTestContext c,UUID id,String side){
        var expectedSlot=side.equals("peg_left")?EquipmentSlot.LEGS:EquipmentSlot.FEET;
        var expected=itemId(side.equals("peg_left")?"pirateatelier:captain_armor_pristine_peg_left_leggings":"pirateatelier:atelier_peg_leg_wood");
        c.waitFor(mc->{var n=find(mc,id);return n!=null&&!n.isInvisible()&&n.getItemBySlot(expectedSlot).is(expected)&&AUDIT_IDS.stream().allMatch(other->{var e=find(mc,other);return other.equals(id)||e==null||e.isInvisible();});},160);
        c.runOnClient(mc->{var n=find(mc,id);PirateClientGameTests.expect(n!=null,"Peg specimen missing: "+side);
            PirateClientGameTests.expect(n.getItemBySlot(expectedSlot).is(expected),"Peg selection failed to sync: "+side);
            for(var slot:SLOTS.values())if(slot!=expectedSlot)PirateClientGameTests.expect(n.getItemBySlot(slot).isEmpty(),"Unrelated armor obscures peg-leg specimen: "+side+" slot="+slot);
        });
    }
    private static void verifyActor(ClientGameTestContext c,UUID id,String skin,String armorBase){
        c.waitFor(mc->{var n=find(mc,id);return n!=null&&n.getSkin().equals(skin)&&!n.isInvisible()&&AUDIT_IDS.stream().allMatch(other->{var e=find(mc,other);return other.equals(id)||e==null||e.isInvisible();})&&SLOTS.entrySet().stream().allMatch(e->{
            return armorBase==null?n.getItemBySlot(e.getValue()).isEmpty():n.getItemBySlot(e.getValue()).is(itemId("pirateatelier:"+armorBase+"_"+e.getKey()));
        });},160);
        c.runOnClient(mc->{var n=find(mc,id);PirateClientGameTests.expect(n!=null,"Visual audit entity missing: "+id);
            PirateClientGameTests.expect(n.getSkin().equals(skin),"Skin did not synchronize: "+skin);
            for(var e:SLOTS.entrySet()){
                boolean match=armorBase==null?n.getItemBySlot(e.getValue()).isEmpty():n.getItemBySlot(e.getValue()).is(itemId("pirateatelier:"+armorBase+"_"+e.getKey()));
                PirateClientGameTests.expect(match,"Equipment mismatch for "+skin+" / "+armorBase+" / "+e.getKey()+" actual="+BuiltInRegistries.ITEM.getKey(n.getItemBySlot(e.getValue()).getItem()));
            }
            var renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(n);var state=(net.minecraft.client.renderer.entity.state.HumanoidRenderState)renderer.createRenderState();renderer.extractRenderState(n,state,0);
            var clothes=new net.conczin.mca.client.render.layer.ClothingLayer(renderer,(net.minecraft.client.model.HumanoidModel)renderer.getModel(),"normal");
            if(armorBase==null)PirateClientGameTests.expect(clothes.getSkin(state)==null,"MCA profession clothes obscure isolated pirate skin: "+skin);
            var bodyLayer=new net.conczin.mca.client.render.layer.SkinLayer(renderer,(net.minecraft.client.model.HumanoidModel)renderer.getModel());
            PirateClientGameTests.expect(bodyLayer.getSkin(state).equals(dev.arandu.piratesmca.client.PirateSkinLayers.body(skin)),"Pirate layered body material did not resolve: "+skin);
        });
    }
    private static net.minecraft.world.item.Item itemId(String id){return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));}
    private static VillagerEntityMCA find(net.minecraft.client.Minecraft mc,UUID id){for(var e:mc.level.entitiesForRendering())if(e instanceof VillagerEntityMCA n&&n.getUUID().equals(id))return n;return null;}
    private static int staticViews(ClientGameTestContext c,TestSingleplayerContext w,String label){
        // Static variant scenes already wait for the selected equipment to arrive.
        // Camera() additionally waits four ticks after the server teleport. Two
        // extra rendered ticks suffice here; moving/baseline scenes retain eight.
        int settle=System.getProperty("piratesmca.visualFixReview","").startsWith("variants")?2:8;
        for(String view:VIEWS){camera(c,w,view);c.waitTicks(settle);c.takeScreenshot("AUDIT-"+safe(label)+"-"+view.toUpperCase(Locale.ROOT));}
        return VIEWS.size();
    }
    private static int poses(ClientGameTestContext c,TestSingleplayerContext w,UUID id,String label,boolean armor){
        int count=0;
        for(String p:List.of("walk","run","attack")){
            w.getServer().runOnServer(s->{var n=(VillagerEntityMCA)s.overworld().getEntity(id);n.setPos(X,Y,Z);n.setSprinting(false);n.setPose(Pose.STANDING);n.setSwimming(false);n.setYRot(180);n.setYHeadRot(180);});
            camera(c,w,"front");
            // The server tick drives real MCA walk/attack animation state; this is a controlled animation fixture, not autonomous pathfinding.
            c.runOnClient(mc->{});
            PHASE.set(p);
            c.waitFor(mc->{var n=find(mc,id);return n!=null&&(p.equals("attack")?n.attackAnim>.01:n.walkAnimation.speed()>.01&&(p.equals("walk")||n.isSprinting()));},120);
            c.runOnClient(mc->{var n=find(mc,id);PirateClientGameTests.expect(n!=null,"Animated specimen missing: "+label+" "+p);
                if(p.equals("walk"))PirateClientGameTests.expect(n.walkAnimation.speed()>.01&&!n.isSprinting(),"Walk pose is not demonstrably active: "+label);
                if(p.equals("run"))PirateClientGameTests.expect(n.walkAnimation.speed()>.01&&n.isSprinting(),"Run pose is not demonstrably active: "+label);
                if(p.equals("attack"))PirateClientGameTests.expect(n.attackAnim>.01,"Attack pose is not demonstrably active: "+label);
                System.out.println("QA_VISUAL_POSE label="+label+" pose="+p+" walk="+n.walkAnimation.speed()+" sprint="+n.isSprinting()+" attack="+n.attackAnim+" armor="+armor);
            });
            c.takeScreenshot("AUDIT-"+safe(label)+"-"+p.toUpperCase(Locale.ROOT));PHASE.set("idle");c.waitTicks(6);count++;
        }
        return count;
    }
    private static void camera(ClientGameTestContext c,TestSingleplayerContext w,String view){
        String tp=switch(view){case "back"->X+" -60.4 3.4 180 0";case "left"->(X-3.4)+" -60.4 0.0 -90 0";case "right"->(X+3.4)+" -60.4 0.0 90 0";default->X+" -60.4 -3.4 0 0";};
        w.getServer().runOnServer(s->PirateClientGameTests.commandAsOp(s,"tp @s "+tp));c.waitTicks(4);
    }
    private static int sequence(ClientGameTestContext c,TestSingleplayerContext w,UUID id,String label){
        int count=0;
        for(String action:List.of("walk","run","attack","turn")){
            PHASE.set(action);
            for(String view:VIEWS){camera(c,w,view);
                for(int frame=0;frame<3;frame++){c.waitTicks(3);c.takeScreenshot("AUDIT-"+safe(label)+"-"+action.toUpperCase(Locale.ROOT)+"-"+view.toUpperCase(Locale.ROOT)+"-"+frame);count++;}
            }
            PHASE.set("idle");c.waitTicks(8);
        }
        return count;
    }
}
