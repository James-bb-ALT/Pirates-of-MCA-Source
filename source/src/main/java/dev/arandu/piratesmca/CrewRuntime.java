package dev.arandu.piratesmca;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.registry.EntitiesMCA;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Single-server authority; only PIRATE-tagged MCA NPCs are indexed. */
public final class CrewRuntime {
    private CrewRuntime(){}
    private static final Map<MinecraftServer,State> STATES=Collections.synchronizedMap(new IdentityHashMap<>());
    private static final class State {
        final CrewLedger ledger;
        final NativeCrewData savedData;
        final Map<UUID,VillagerEntityMCA> loaded=new HashMap<>();
        final Map<UUID,Brawl> brawls=new HashMap<>();
        final Map<UUID,Long> lastAttack=new HashMap<>();
        final Map<UUID,Long> reentryGraceUntil=new HashMap<>();
        final Map<UUID,Long> lastRetreatPath=new HashMap<>();
        final Map<UUID,RetreatGoal> retreatGoals=new HashMap<>();
        State(MinecraftServer server) {
            savedData=NativeCrewData.loadWorld(server);ledger=savedData.ledger();
        }
    }
    private static final ThreadLocal<UUID> BRAWL_VICTIM=new ThreadLocal<>();
    private record Brawl(UUID left,UUID right,long expires){}
    private record RetreatGoal(net.minecraft.world.phys.Vec3 position,net.minecraft.world.entity.ai.memory.WalkTarget walk){}

    public static void initialize() {
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity,source) -> {
            if(entity instanceof VillagerEntityMCA npc && npc.level() instanceof ServerLevel level && isPirate(npc))
                afterDeath(level,npc,source.getEntity());
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            State state=STATES.get(level.getServer());
            if(state==null)return;
            Brawl b=state.brawls.get(entity.getUUID());
            if(b!=null)stopBrawl(level,b,level.getGameTime());
            state.loaded.remove(entity.getUUID(),entity);
            state.lastAttack.remove(entity.getUUID());
            state.lastRetreatPath.remove(entity.getUUID());
            state.retreatGoals.remove(entity.getUUID());
            var member=state.ledger.member(entity.getUUID());
            if(member!=null && state.loaded.keySet().stream().noneMatch(id->{var m=state.ledger.member(id);return m!=null&&m.crew.equals(member.crew);}))state.reentryGraceUntil.remove(member.crew);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            State state=STATES.get(server);
            if(state!=null && server.getTickCount()%100==0)state.ledger.save();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            State state=STATES.get(server);if(state!=null)state.ledger.save();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            STATES.remove(server);
            BRAWL_VICTIM.remove();
            PirateBehavior.clearRuntime();
        });
    }
    private static State state(ServerLevel level) {
        return STATES.computeIfAbsent(level.getServer(),State::new);
    }
    public static CrewLedger ledger(ServerLevel level){var ledger=state(level).ledger;ledger.gameTick(level.getGameTime());return ledger;}
    static int loadedCount(ServerLevel level){return state(level).loaded.size();}
    static boolean inBrawl(ServerLevel level,UUID id){return state(level).brawls.containsKey(id);}
    static net.minecraft.world.phys.Vec3 retreatGoal(ServerLevel level,UUID id){var goal=state(level).retreatGoals.get(id);return goal==null?null:goal.position;}
    private static UUID stableCrewId(ServerLevel level,String origin){
        String stable=level.dimension().toString()+"/"+origin;
        return UUID.nameUUIDFromBytes(stable.getBytes(StandardCharsets.UTF_8));
    }
    private static String crewOrigin(VillagerEntityMCA npc){
        for(String tag:npc.entityTags())if(tag.startsWith("pmca_origin_"))return tag;
        return null;
    }
    private static UUID taggedCrew(VillagerEntityMCA npc){
        for(String tag:npc.entityTags())if(tag.startsWith("pmca_crew_")){
            try{return UUID.fromString(tag.substring("pmca_crew_".length()));}catch(IllegalArgumentException ignored){}
        }
        return null;
    }
    /** Keep the entity tag consistent with the authoritative per-world ledger. */
    public static void syncCrewTag(VillagerEntityMCA npc, UUID id) {
        String wanted = "pmca_crew_" + id;
        boolean correct = npc.entityTags().contains(wanted);
        for (String tag : new ArrayList<>(npc.entityTags())) {
            if (tag.startsWith("pmca_crew_") && !tag.equals(wanted)) {
                npc.removeTag(tag);
                correct = false;
            }
        }
        if (!correct) npc.addTag(wanted);
    }
    private static CrewLedger.Member register(ServerLevel level,VillagerEntityMCA npc,String profile){
        if(npc.isBaby() || !npc.entityTags().contains(PiratesOfMCA.MARKER))return null;
        CrewLedger ledger=ledger(level);
        CrewLedger.Member existing=ledger.member(npc.getUUID());
        if (existing != null) {
            if(!existing.alive)return null;
            // An NPC can change crews. A saved member always wins over an old NBT tag.
            ledger.getOrCreate(existing.crew, "");
            ledger.add(npc.getUUID(),existing.crew,profile);
            ledger.updateProfile(existing,profile);
            ledger.repairAutoCaptain(existing);
            syncCrewTag(npc,existing.crew);
            return existing;
        }
        UUID id=taggedCrew(npc);
        String origin=crewOrigin(npc);
        if(id==null)
            id=origin!=null?stableCrewId(level,origin):UUID.nameUUIDFromBytes((level.dimension().toString()+"/solo/"+npc.getUUID()).getBytes(StandardCharsets.UTF_8));
        CrewLedger.Crew crew=ledger.getOrCreate(id,Objects.toString(origin,"UNASSIGNED"));
        CrewLedger.Member member=ledger.add(npc.getUUID(),id,profile);
        // Only natural seapost crews have automatic NPC captains. /summon creates
        // unassigned free sailors until a player founds or claims a crew.
        if(origin!=null && !crew.playerCaptain && crew.captain==null)
            ledger.captain(crew,npc.getUUID(),false);
        else ledger.repairAutoCaptain(member);
        syncCrewTag(npc,id);
        return member;
    }
    /** Make a just-summoned tagged adult discoverable before its first scheduled AI tick. */
    public static CrewLedger.Member prepareMember(ServerLevel level,VillagerEntityMCA npc){
        if(!npc.isAlive()||npc.isBaby()||!npc.entityTags().contains(PiratesOfMCA.MARKER))return null;
        String profile="friendly";
        for(String candidate:List.of("captain","bruiser","greedy","suspicious","friendly"))
            if(npc.entityTags().contains("pmca_"+candidate)){profile=candidate;break;}
        return register(level,npc,profile);
    }
    public static void enforceGender(VillagerEntityMCA npc){
        Gender expected=null;
        if(npc.entityTags().contains("pmca_gender_male"))expected=Gender.MALE;
        else if(npc.entityTags().contains("pmca_gender_female"))expected=Gender.FEMALE;
        else if(npc.getType()==EntitiesMCA.MALE_VILLAGER)expected=Gender.MALE;
        else if(npc.getType()==EntitiesMCA.FEMALE_VILLAGER)expected=Gender.FEMALE;
        // Never use initializeSkin(false) here: it overwrites the outfit and
        // can assign an empty skin before the MCA skin lists are available.
        if(expected != null && npc.getGenetics().getGender()!=expected){
            npc.getGenetics().setGender(expected);
            // Generate a matching body skin lazily in PirateBehavior.
            npc.setSkin("");
            npc.refreshDimensions();
        }
        npc.addTag("pmca_gender_fixed_v203");
    }
    public static boolean isPirate(VillagerEntityMCA npc){
        return npc.entityTags().contains(PiratesOfMCA.MARKER) && !npc.isBaby()
                && npc.getProfession()==PirateProfession.PIRATE;
    }
    public static void tick(ServerLevel level,VillagerEntityMCA npc,String profile){
        if(npc.isBaby())return;
        enforceGender(npc);
        CrewLedger.Member m=register(level,npc,profile);
        if(m==null)return;
        state(level).loaded.put(npc.getUUID(),npc);
        long now=level.getGameTime();
        CrewLedger ledger=ledger(level);
        CrewLedger.Crew c=ledger.crew(m.crew);
        if(c==null)return;
        if(m.retreating && npc.getHealth()>=npc.getMaxHealth()*.5f)ledger.retreating(m,false,null,now);
        else if(!m.retreating && npc.getHealth()<=npc.getMaxHealth()*.25f) {
            var threat=nearestMonster(level,npc,12);if(threat!=null)ledger.retreating(m,true,threat.getUUID(),now);
        }
        doOrder(level,npc,m,c,now);
        if(npc.tickCount%20==0){
            // MCA's guard brain must never turn a nonlethal crew dispute into
            // lethal retaliation. Only a declared mutiny permits friendly fire.
            if(c.phase!=CrewLedger.Phase.REBELLION){
                Object target=npc.getBrain().getMemoryInternal(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET).orElse(null);
                if(target instanceof Entity e && (e.getUUID().equals(c.captain)
                        || c.members.contains(e.getUUID()))) {
                    npc.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
                    npc.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.HURT_BY_ENTITY);
                }
            }
            // Fallback for hostile mobs not immediately picked by MCA guard
            // sensors. Reuses MCA attacks (no duplicative AI task installs).
            if(m.order==CrewLedger.Order.NONE && !state(level).brawls.containsKey(npc.getUUID()))
                {if(m.retreating)retreatFromMonster(level,npc,now);else defendAgainstMonsters(level,npc,now);}
            advanceMutiny(level,ledger,c,now);
        }
        if(npc.tickCount%100==0){
            socialMeeting(level,npc,m,c,now);
            tryProvocation(level,npc,m,c,now);
        }
        Brawl b=state(level).brawls.get(npc.getUUID());
        if(b!=null && npc.tickCount%5==0)processBrawl(level,npc,b,now);
        if(npc.tickCount%40==0)PirateDialogues.tick(level,npc);
    }
    private static ServerPlayer captainPlayer(ServerLevel level,CrewLedger.Crew c){
        if(!c.playerCaptain||c.captain==null)return null;
        for(ServerPlayer p:level.players())if(p.getUUID().equals(c.captain))return p;
        return null;
    }
    static LivingEntity effectiveCommander(ServerLevel level,CrewLedger.Crew c) {
        LivingEntity captain=c.captain==null?null:c.playerCaptain?level.getServer().getPlayerList().getPlayer(c.captain):state(level).loaded.get(c.captain);
        if(captain!=null && captain.isAlive()) {
            ledger(level).actingCaptain(c,null);return captain.level()==level?captain:null;
        }
        var mate=state(level).loaded.get(c.firstMate);
        if(mate!=null && mate.isAlive()) {
            ledger(level).actingCaptain(c,mate.getUUID());return mate.level()==level?mate:null;
        }
        ledger(level).actingCaptain(c,null);return null;
    }
    private static void cancelOrder(ServerLevel level,CrewLedger.Member m,CrewLedger.Crew c,long now,String reason) {
        ledger(level).event(c,"ORDER_ENDED "+m.id+" "+m.order+" "+reason);
        ledger(level).order(m,CrewLedger.Order.NONE,now);
    }
    static void doOrder(ServerLevel level,VillagerEntityMCA npc,CrewLedger.Member m,CrewLedger.Crew c,long now){
        LivingEntity commander=effectiveCommander(level,c);
        if(m.order==CrewLedger.Order.NONE || m.order==CrewLedger.Order.DISMISS)return;
        if(now-m.lastOrder>12000){cancelOrder(level,m,c,now,"EXPIRED");npc.getNavigation().stop();return;}
        if(m.order==CrewLedger.Order.CEASE_FIGHT){
            Brawl b=state(level).brawls.get(npc.getUUID());
            if(b!=null)stopBrawl(level,b,now);
            ledger(level).order(m,CrewLedger.Order.NONE,now);return;
        }
        if(m.retreating && m.order!=CrewLedger.Order.RETREAT && retreatFromMonster(level,npc,now))return;
        if(m.order==CrewLedger.Order.RETREAT){
            clearCombatBrain(npc);
            if(commander!=null && commander!=npc && npc.distanceToSqr(commander)<=64*64)npc.getNavigation().moveTo(commander,1.25);
            else if(m.directive!=null && m.directive.dimension().equals(level.dimension().toString()))
                {if(npc.distanceToSqr(m.directive.x(),m.directive.y(),m.directive.z())<=64*64)npc.getNavigation().moveTo(m.directive.x(),m.directive.y(),m.directive.z(),1.25);else cancelOrder(level,m,c,now,"DESTINATION_TOO_FAR");}
            return;
        }
        if(m.directive==null) {
            if(m.order==CrewLedger.Order.ATTACK){cancelOrder(level,m,c,now,"MISSING_TARGET");return;}
            ledger(level).order(m,m.order,m.lastOrder,new CrewLedger.Directive(level.dimension().toString(),npc.getX(),npc.getY(),npc.getZ(),null));
        }
        var directive=m.directive;
        if(!directive.dimension().equals(level.dimension().toString())) {
            cancelOrder(level,m,c,now,"DIMENSION_CHANGED");clearCombatBrain(npc);npc.getNavigation().stop();return;
        }
        double anchorDistance=npc.distanceToSqr(directive.x(),directive.y(),directive.z());
        if((m.order==CrewLedger.Order.RALLY || m.order==CrewLedger.Order.GUARD) && anchorDistance>64*64){cancelOrder(level,m,c,now,"DESTINATION_TOO_FAR");clearCombatBrain(npc);npc.getNavigation().stop();return;}
        switch(m.order){
            case FOLLOW -> {
                clearCombatBrain(npc);
                if(commander!=null && commander!=npc) {
                    double d=npc.distanceToSqr(commander);
                    if(d>64*64){cancelOrder(level,m,c,now,"COMMANDER_TOO_FAR");npc.getNavigation().stop();}
                    else if(d>16)npc.getNavigation().moveTo(commander,1.12);else if(d<6)npc.getNavigation().stop();
                }
            }
            case RALLY -> {
                clearCombatBrain(npc);
                if(anchorDistance>4)npc.getNavigation().moveTo(directive.x(),directive.y(),directive.z(),1.2);
                else {npc.getNavigation().stop();cancelOrder(level,m,c,now,"ARRIVED");}
            }
            case HOLD -> {
                clearCombatBrain(npc);
                if(anchorDistance>1)npc.getNavigation().moveTo(directive.x(),directive.y(),directive.z(),1.05);
                else npc.getNavigation().stop();
            }
            case GUARD, ATTACK -> {
                if(m.order==CrewLedger.Order.GUARD && anchorDistance>12*12){clearCombatBrain(npc);npc.getNavigation().moveTo(directive.x(),directive.y(),directive.z(),1.1);return;}
                if(npc.getHealth()<=npc.getMaxHealth()*0.25f){clearCombatBrain(npc);return;}
                Monster threat;
                if(m.order==CrewLedger.Order.ATTACK) {
                    Entity target=directive.target()==null?null:level.getEntity(directive.target());
                    if(!(target instanceof Monster monster) || !monster.isAlive() || npc.distanceToSqr(monster)>64*64) {
                        cancelOrder(level,m,c,now,"TARGET_UNAVAILABLE");clearCombatBrain(npc);return;
                    }
                    threat=monster;
                } else {
                    threat=nearestMonster(level,npc,11);
                    if(threat!=null && threat.distanceToSqr(directive.x(),directive.y(),directive.z())>12*12)threat=null;
                }
                if(threat!=null){
                    npc.getNavigation().moveTo(threat,1.2);
                    if(npc.distanceToSqr(threat)<6.0 && npc.getSensing().hasLineOfSight(threat) && attackReady(level,npc,now,23)){npc.swing(InteractionHand.MAIN_HAND);npc.doHurtTarget(level,threat);}
                } else if(m.order==CrewLedger.Order.GUARD && anchorDistance>4) {
                    clearCombatBrain(npc);npc.getNavigation().moveTo(directive.x(),directive.y(),directive.z(),1.1);
                }
            }
            default -> {}
        }
    }
    private static void clearCombatBrain(VillagerEntityMCA npc) {
        npc.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
        npc.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.HURT_BY_ENTITY);
        npc.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);
    }
    private static boolean retreatFromMonster(ServerLevel level,VillagerEntityMCA npc,long now) {
        var threat=nearestMonster(level,npc,12);if(threat==null)return false;
        clearCombatBrain(npc);
        Long last=state(level).lastRetreatPath.get(npc.getUUID());
        if(last==null || now-last>=40) {
            state(level).lastRetreatPath.put(npc.getUUID(),now);
            double dx=npc.getX()-threat.getX(),dz=npc.getZ()-threat.getZ();
            double angle=dx*dx+dz*dz<.01?Math.floorMod(npc.getUUID().hashCode(),360)*Math.PI/180:Math.atan2(dz,dx);
            for(double offset:new double[]{0,Math.PI/4,-Math.PI/4,Math.PI/2,-Math.PI/2}) {
                var position=net.minecraft.core.BlockPos.containing(npc.getX()+8*Math.cos(angle+offset),npc.getY(),npc.getZ()+8*Math.sin(angle+offset));
                var path=npc.getNavigation().createPath(position,1);
                if(path==null || !path.canReach())continue;
                var away=new net.minecraft.world.phys.Vec3(position.getX()+.5,position.getY(),position.getZ()+.5);
                var walk=new net.minecraft.world.entity.ai.memory.WalkTarget(away,1.25f,1);
                state(level).retreatGoals.put(npc.getUUID(),new RetreatGoal(away,walk));
                npc.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,walk);
                npc.getNavigation().moveTo(path,1.25);break;
            }
        }
        return true;
    }
    /** Run after MCA's own tick so its fight activity cannot replace an injured pirate's escape path. */
    public static void enforceMovementOverride(ServerLevel level,VillagerEntityMCA npc) {
        if(!isPirate(npc))return;
        State state=STATES.get(level.getServer());if(state==null)return;
        var member=state.ledger.member(npc.getUUID());if(member==null)return;
        if(!member.retreating) {state.retreatGoals.remove(npc.getUUID());state.lastRetreatPath.remove(npc.getUUID());return;}
        npc.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
        npc.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.HURT_BY_ENTITY);
        npc.getBrain().setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.IDLE);
        var goal=state.retreatGoals.get(npc.getUUID());
        if(goal!=null && npc.distanceToSqr(goal.position)>1)npc.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,goal.walk);
        else {npc.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET);npc.getNavigation().stop();}
    }
    private static void defendAgainstMonsters(ServerLevel level,VillagerEntityMCA npc,long now){
        if(npc.getHealth()<=npc.getMaxHealth()*0.30f)return; // MCA can retreat when injured
        if(npc.getBrain().getMemoryInternal(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET).isPresent())return;
        Monster threat=nearestMonster(level,npc,9);
        if(threat==null)return;
        double dist=npc.distanceToSqr(threat);
        if(dist>5)npc.getNavigation().moveTo(threat,1.12);
        if(dist<5 && attackReady(level,npc,now,21)){
            npc.swing(InteractionHand.MAIN_HAND);
            npc.doHurtTarget(level,threat);
        }
    }
    private static Monster nearestMonster(ServerLevel level,VillagerEntityMCA npc,double range){
        Monster result=null;double closest=range*range;
        for(Monster monster:level.getEntitiesOfClass(Monster.class,npc.getBoundingBox().inflate(range))){
            if(!monster.isAlive())continue;double d=npc.distanceToSqr(monster);
            if(d<closest && npc.getSensing().hasLineOfSight(monster)){closest=d;result=monster;}
        }
        return result;
    }
    private static void tryProvocation(ServerLevel level,VillagerEntityMCA npc,CrewLedger.Member m,CrewLedger.Crew c,long now){
        if(m.role==CrewLedger.Role.QUARTERMASTER) {
            for(var other:level.getEntitiesOfClass(VillagerEntityMCA.class,npc.getBoundingBox().inflate(8))) {
                var otherMember=ledger(level).member(other.getUUID());
                Brawl fight=state(level).brawls.get(other.getUUID());
                if(fight!=null && otherMember!=null && otherMember.crew.equals(c.id)) {
                    stopBrawl(level,fight,now);ledger(level).event(c,"BRAWL_SEPARATED "+m.id);
                }
            }
            return;
        }
        if(state(level).brawls.containsKey(npc.getUUID()))return;
        if(m.role==CrewLedger.Role.CAPTAIN || c.phase==CrewLedger.Phase.REBELLION)return;
        if(m.lastFightEnd>0 && now-m.lastFightEnd<9600 || m.lastProvocation>0 && now-m.lastProvocation<2400)return;
        for(VillagerEntityMCA other:level.getEntitiesOfClass(VillagerEntityMCA.class,npc.getBoundingBox().inflate(8))){
            if(other==npc||!isPirate(other) || npc.distanceToSqr(other)>8*8 || !npc.getSensing().hasLineOfSight(other) || state(level).brawls.containsKey(other.getUUID()))continue;
            CrewLedger.Member opponent=ledger(level).member(other.getUUID());
            if(opponent==null||!m.crew.equals(opponent.crew)||opponent.role==CrewLedger.Role.CAPTAIN)continue;
            SocialEvent provocation=ledger(level).provoke(m,opponent,now);
            if(provocation==null)continue;
            if(ledger(level).rollProvocation(provocation,npc.getRandom().nextFloat()) && startBrawl(level,npc,other,now))
                ledger(level).claimConsequence(provocation,"BRAWL_STARTED");
            return;
        }
    }
    static void socialMeeting(ServerLevel level,VillagerEntityMCA npc,CrewLedger.Member m,CrewLedger.Crew c,long now) {
        VillagerEntityMCA partner=null;double nearest=8*8;
        for(var other:level.getEntitiesOfClass(VillagerEntityMCA.class,npc.getBoundingBox().inflate(8))) {
            if(!other.isAlive() || !isPirate(other) || npc.getUUID().compareTo(other.getUUID())>=0 || !npc.getSensing().hasLineOfSight(other))continue;
            var om=ledger(level).member(other.getUUID());if(om==null || !m.crew.equals(om.crew))continue;
            double distance=npc.distanceToSqr(other);if(distance<nearest){nearest=distance;partner=other;}
        }
        if(partner==null)return;
        var peer=ledger(level).member(partner.getUUID());
        ledger(level).shareLatestRumor(m,peer,now);ledger(level).shareLatestRumor(peer,m,now);
        ledger(level).persuadeConspirator(m,peer,now);ledger(level).persuadeConspirator(peer,m,now);
        if(m.greed<65 && peer.greed<65)return;
        for(var item:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,npc.getBoundingBox().inflate(4))) {
            if(!item.isAlive() || npc.distanceToSqr(item)>16 || partner.distanceToSqr(item)>16)continue;
            var stack=item.getItem();
            if(stack.is(net.minecraft.world.item.Items.GOLD_INGOT) || stack.is(net.minecraft.world.item.Items.EMERALD) || stack.is(net.minecraft.world.item.Items.DIAMOND)) {
                ledger(level).resourceDispute(m,peer,item.getUUID(),now);break;
            }
        }
    }
    static boolean startBrawl(ServerLevel level,VillagerEntityMCA a,VillagerEntityMCA b,long now) {
        if(a==b || a.level()!=level || b.level()!=level || !a.isAlive() || !b.isAlive() || !isPirate(a) || !isPirate(b))return false;
        var ma=ledger(level).member(a.getUUID());var mb=ledger(level).member(b.getUUID());
        if(ma==null || mb==null || !ma.alive || !mb.alive || !ma.adult || !mb.adult || !ma.crew.equals(mb.crew) || ma.role==CrewLedger.Role.CAPTAIN || mb.role==CrewLedger.Role.CAPTAIN)return false;
        if(state(level).brawls.containsKey(a.getUUID()) || state(level).brawls.containsKey(b.getUUID()))return false;
        Brawl fight=new Brawl(a.getUUID(),b.getUUID(),now+600);
        state(level).brawls.put(a.getUUID(),fight);state(level).brawls.put(b.getUUID(),fight);
        ledger(level).fight(a.getUUID(),b.getUUID(),now,true);
        return true;
    }
    private static boolean attackReady(ServerLevel level,VillagerEntityMCA npc,long now,long interval) {
        Long last=state(level).lastAttack.get(npc.getUUID());
        if(last!=null && now-last<interval)return false;
        state(level).lastAttack.put(npc.getUUID(),now);return true;
    }
    private static void processBrawl(ServerLevel level,VillagerEntityMCA npc,Brawl b,long now){
        UUID otherId=npc.getUUID().equals(b.left)?b.right:b.left;
        VillagerEntityMCA other=state(level).loaded.get(otherId);
        if(other==null || other.level()!=level || other.isRemoved() || !other.isAlive() || npc.distanceToSqr(other)>20*20 || now>=b.expires
                || npc.getHealth()<=npc.getMaxHealth()*.25 || other.getHealth()<=other.getMaxHealth()*.25){
            stopBrawl(level,b,now);return;
        }
        npc.getNavigation().moveTo(other,1.07);
        npc.getLookControl().setLookAt(other,25,25);
        if(npc.distanceToSqr(other)<5 && attackReady(level,npc,now,20)){
            npc.swing(InteractionHand.MAIN_HAND);
            npc.doHurtTarget(level,other);
        }
    }
    private static void stopBrawl(ServerLevel level,Brawl b,long now){
        boolean existed=state(level).brawls.remove(b.left)!=null;
        state(level).brawls.remove(b.right);
        if(existed)ledger(level).fight(b.left,b.right,now,false);
        VillagerEntityMCA a=state(level).loaded.get(b.left),c=state(level).loaded.get(b.right);
        if(a!=null)a.getNavigation().stop();if(c!=null)c.getNavigation().stop();
    }
    public static float scaleBrawlDamage(VillagerEntityMCA victim,net.minecraft.world.damagesource.DamageSource source,float damage){
        Brawl b=state((ServerLevel)victim.level()).brawls.get(victim.getUUID());
        Entity attacker=source.getEntity();
        UUID opponent=b==null?null:(victim.getUUID().equals(b.left)?b.right:b.left);
        return BrawlDamagePolicy.cap(victim.getHealth(),victim.getMaxHealth(),damage,
            attacker!=null && attacker.getUUID().equals(opponent));
    }
    /** Keep secondary weapon effects in the same synchronous combat context, even for MCA guard attacks. */
    public static boolean attackWithContext(VillagerEntityMCA attacker,Entity target,java.util.function.Supplier<Boolean> attack) {
        if(!(attacker.level() instanceof ServerLevel level))return attack.get();
        if(!mayAttack(attacker,target))return false;
        Brawl b=state(level).brawls.get(attacker.getUUID());
        UUID other=b==null?null:(attacker.getUUID().equals(b.left)?b.right:b.left);
        UUID previous=BRAWL_VICTIM.get();
        if(target.getUUID().equals(other))BRAWL_VICTIM.set(target.getUUID());
        else BRAWL_VICTIM.remove();
        try {return attack.get();}
        finally {if(previous==null)BRAWL_VICTIM.remove();else BRAWL_VICTIM.set(previous);}
    }
    public static boolean isBrawlSecondaryTarget(Entity entity) {return entity.getUUID().equals(BRAWL_VICTIM.get());}
    public static boolean mayAttack(VillagerEntityMCA attacker,Entity target) {
        if(!(attacker.level() instanceof ServerLevel level))return true;
        if(!isPirate(attacker))return true;
        if(target instanceof VillagerEntityMCA child && (child.isBaby()
                || child.getAgeState()!=net.conczin.mca.entity.ai.relationship.AgeState.ADULT))return false;
        var ledger=ledger(level);var member=ledger.member(attacker.getUUID());
        var crew=member==null?null:ledger.crew(member.crew);
        if(crew==null)return true;
        UUID victim=target.getUUID();
        if(victim.equals(crew.lastDeposedCaptain) && level.getGameTime()<crew.surrenderProtectionUntil)return false;
        Brawl b=state(level).brawls.get(attacker.getUUID());
        boolean duel=b!=null && victim.equals(attacker.getUUID().equals(b.left)?b.right:b.left);
        if(!duel && target instanceof Monster && (member.retreating || attacker.getHealth()<=attacker.getMaxHealth()*.25f))return false;
        if(!duel && crew.phase!=CrewLedger.Phase.REBELLION) {
            if(member.order==CrewLedger.Order.RETREAT || member.order==CrewLedger.Order.FOLLOW || member.order==CrewLedger.Order.RALLY || member.order==CrewLedger.Order.HOLD)return false;
            if(member.order==CrewLedger.Order.ATTACK && (member.directive==null || !victim.equals(member.directive.target())))return false;
            if(member.order==CrewLedger.Order.GUARD && member.directive!=null && target.distanceToSqr(member.directive.x(),member.directive.y(),member.directive.z())>12*12)return false;
        }
        if(!victim.equals(crew.captain) && !crew.members.contains(victim))return true;
        if(attacker.getUUID().equals(crew.captain))return true; // Captain discipline can hurt and has social consequences.
        if(duel)return true;
        if(crew.phase!=CrewLedger.Phase.REBELLION)return false;
        boolean attackerRebel=member.resentment>=65 && member.fear<80 && member.adult && member.alive;
        var other=ledger.member(victim);
        boolean victimRebel=!victim.equals(crew.captain) && other!=null && other.resentment>=65 && other.fear<80 && other.adult && other.alive;
        return attackerRebel!=victimRebel;
    }
    public static void afterDamage(ServerLevel level,VillagerEntityMCA victim,Entity attacker,float amount){
        if(attacker==null||amount<=0||!victim.isAlive()||!isPirate(victim))return;
        CrewLedger ledger=ledger(level);
        CrewLedger.Member member=ledger.member(victim.getUUID());
        CrewLedger.Crew crew=member==null?null:ledger.crew(member.crew);
        if(crew!=null)ledger.breakDepartureProtection(crew,attacker.getUUID(),victim.getUUID(),level.getGameTime());
        if(crew!=null&&attacker.getUUID().equals(crew.captain)){
            // Prevent reflex revenge from guard AI before a formal mutiny.
            if(crew.phase!=CrewLedger.Phase.REBELLION){
                victim.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.HURT_BY_ENTITY);
                victim.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
            }
        }
        long now=level.getGameTime();
        SocialEvent assault=ledger.assaultRecord(victim.getUUID(),attacker.getUUID(),now);
        if(assault!=null && crew!=null){
            // Witnessed captain abuse slowly radicalizes crewmates; a single victim alone is not a mutiny.
            for(VillagerEntityMCA witness:level.getEntitiesOfClass(VillagerEntityMCA.class,victim.getBoundingBox().inflate(16))){
                if(witness==victim||!witness.isAlive()||!isPirate(witness)||witness.distanceToSqr(victim)>16*16||!witness.getSensing().hasLineOfSight(victim))continue;
                CrewLedger.Member wm=ledger.member(witness.getUUID());
                if(wm!=null&&wm.crew.equals(crew.id))ledger.witnessAssault(wm.id,assault,now);
            }
        }
    }
    private static void afterDeath(ServerLevel level,VillagerEntityMCA victim,Entity attacker) {
        CrewLedger ledger=ledger(level);long now=level.getGameTime();
        {
            CrewLedger.Member killed=ledger.member(victim.getUUID());
            if(killed==null || !killed.alive)return;
            CrewLedger.Crew ship=killed==null?null:ledger.crew(killed.crew);
            if(ship!=null){
                if(attacker!=null)ledger.breakDepartureProtection(ship,attacker.getUUID(),victim.getUUID(),now);
                boolean internal=attacker instanceof ServerPlayer || attacker instanceof VillagerEntityMCA;
                if(internal){
                    for(VillagerEntityMCA witness:level.getEntitiesOfClass(VillagerEntityMCA.class,victim.getBoundingBox().inflate(16))){
                        if(witness==victim || !witness.isAlive() || !isPirate(witness) || witness.distanceToSqr(victim)>16*16)continue;
                        CrewLedger.Member wm=ledger.member(witness.getUUID());
                        if(wm!=null && wm.crew.equals(ship.id) && witness.getSensing().hasLineOfSight(victim))
                            ledger.witnessMurder(witness.getUUID(),victim.getUUID(),attacker.getUUID(),level.getGameTime());
                    }
                    ledger.murder(victim.getUUID(),attacker.getUUID(),now);
                } else ledger.removeDead(victim.getUUID(),now,"EXTERNAL");
                if(ship.captain==null)promoteSuccessor(ledger,ship,now);
            }
        }
    }
    private static void advanceMutiny(ServerLevel level,CrewLedger ledger,CrewLedger.Crew c,long now){
        if(c.captain==null || c.lastEvent==now)return;
        if(c.playerCaptain) {
            ServerPlayer online=level.getServer().getPlayerList().getPlayer(c.captain);
            if(online!=null && online.level()!=level)return;
            if(online==null && (c.phase==CrewLedger.Phase.ULTIMATUM || c.phase==CrewLedger.Phase.REBELLION)){state(level).reentryGraceUntil.remove(c.id);return;}
        } else {
            VillagerEntityMCA captain=state(level).loaded.get(c.captain);
            if(captain==null || captain.level()!=level){state(level).reentryGraceUntil.remove(c.id);return;} // Unloaded is not dead.
        }
        if(c.phase==CrewLedger.Phase.ULTIMATUM || c.phase==CrewLedger.Phase.REBELLION) {
            if(!state(level).reentryGraceUntil.containsKey(c.id)) {
                state(level).reentryGraceUntil.put(c.id,now+200);
                var captain=captainPlayer(level,c);if(captain!=null)captain.sendSystemMessage(net.minecraft.network.chat.Component.literal("[Piratas] Motim retomado: 10 segundos de seguranca ao carregar. /pirates captain yield"));
            }
            if(now<state(level).reentryGraceUntil.get(c.id))return;
        }
        long elapsed=c.lastEvent==0?0:Math.clamp(now-c.lastEvent,0,20);
        c.lastEvent=now;
        if(c.phase==CrewLedger.Phase.AFTERMATH){if(now>=c.cooldownUntil)c.phase=CrewLedger.Phase.CALM;else return;}
        if((c.phase==CrewLedger.Phase.CALM || c.phase==CrewLedger.Phase.DISCONTENT) && now>=c.nextConspiracyEvaluation){
            c.nextConspiracyEvaluation=now+100;ledger.evaluateDiscontent(c,now);ledger.startConspiracy(c,now);
        }
        if(c.phase==CrewLedger.Phase.CONSPIRACY){
            if(c.playerCaptain && captainPlayer(level,c)==null)return;
            int presentSupporters=0;
            for(UUID id:c.members) {
                var member=ledger.member(id);var entity=state(level).loaded.get(id);
                if(member!=null && member.alive && member.adult && !id.equals(c.captain)
                        && member.resentment>=65 && member.fear<80 && entity!=null
                        && entity.isAlive() && entity.level()==level)presentSupporters++;
            }
            if(presentSupporters<ledger.requiredRebels(c))return;
            LivingEntity captainEntity=c.playerCaptain?captainPlayer(level,c):state(level).loaded.get(c.captain);
            boolean vulnerable=captainEntity!=null && captainEntity.getHealth()<=captainEntity.getMaxHealth()*.5f;
            if(!ledger.recentCaptainAbuse(c,now) && !vulnerable && presentSupporters*2<=ledger.eligibleCrewCount(c))return;
            if(ledger.beginUltimatum(c,now)) {
                elapsed=0;
                c.nextUltimatumNotice=now+200;state(level).reentryGraceUntil.put(c.id,0L);
                ServerPlayer captain=captainPlayer(level,c);
                if(captain!=null)captain.sendSystemMessage(net.minecraft.network.chat.Component.literal("[Piratas] Ultimato: voce tem 60 segundos carregados. /pirates captain yield"));
            }
        }
        if(c.phase==CrewLedger.Phase.ULTIMATUM){
            ServerPlayer captain=captainPlayer(level,c);
            if(c.playerCaptain && captain==null)return; // no offline ultimatum
            if(c.playerCaptain && captain!=null && now>=c.nextUltimatumNotice) {
                c.nextUltimatumNotice=now+200;
                captain.sendSystemMessage(net.minecraft.network.chat.Component.literal("[Piratas] Motim: a tripulacao exige sua rendicao. /pirates captain yield ("+c.ultimatumRemaining/20+"s)"));
            }
            ledger.tickUltimatum(c,elapsed,!c.playerCaptain || captain!=null);
        }
        if(c.phase!=CrewLedger.Phase.REBELLION)return;
        for(UUID id:c.members){Brawl fight=state(level).brawls.get(id);if(fight!=null)stopBrawl(level,fight,now);}
        if(ledger.conspirators(c)==0){ledger.finishRebellion(c,now,true);stopCrewCombat(level,c,now);return;}
        if(c.playerCaptain){
            ServerPlayer player=captainPlayer(level,c);
            if(player==null)return;
            if(!player.isAlive()){
                ledger.finishRebellion(c,now,false);
                promoteSuccessor(ledger,c,now);
                return;
            }
            int alive=0;
            for(UUID uid:c.members){
                CrewLedger.Member m=ledger.member(uid);
                VillagerEntityMCA npc=state(level).loaded.get(uid);
                if(m==null||npc==null||!m.alive||!m.adult||m.fear>=80||!npc.isAlive()||npc.level()!=level||m.resentment<65)continue;
                alive++;
                if(player.isCreative()||player.isSpectator())continue;
                npc.getNavigation().moveTo(player,1.23);
                if(npc.distanceToSqr(player)<6 && attackReady(level,npc,now,25)){npc.swing(InteractionHand.MAIN_HAND);npc.doHurtTarget(level,player);}
            }
            if(alive==0 && ledger.conspirators(c)==0)ledger.finishRebellion(c,now,true);
        } else {
            // NPC-versus-NPC mutiny: targeted fighting, distinct from non-lethal brawls.
            VillagerEntityMCA captain=state(level).loaded.get(c.captain);
            if(captain==null || captain.level()!=level)return;
            if(!captain.isAlive()){ledger.finishRebellion(c,now,false);promoteSuccessor(ledger,c,now);return;}
            long rebels=c.members.stream().map(ledger::member).filter(m->m!=null&&!m.id.equals(c.captain)&&m.alive&&m.adult&&m.resentment>=65&&m.fear<80).count();
            if(captain.getHealth()<=captain.getMaxHealth()*.25f && rebels>=2) {
                ledger.voluntaryDeparture(c,now,true);stopCrewCombat(level,c,now);promoteSuccessor(ledger,c,now);return;
            }
            for(UUID uid:c.members){
                CrewLedger.Member m=ledger.member(uid);VillagerEntityMCA npc=state(level).loaded.get(uid);
                if(m==null||npc==null||!m.alive||!m.adult||m.fear>=80||m.resentment<65||!npc.isAlive()||uid.equals(c.captain))continue;
                if(npc.level()!=level || captain.level()!=level)continue;
                npc.getNavigation().moveTo(captain,1.20);
                if(npc.distanceToSqr(captain)<6 && attackReady(level,npc,now,25)){npc.swing(InteractionHand.MAIN_HAND);npc.doHurtTarget(level,captain);}
            }
        }
    }
    public static void promoteSuccessor(CrewLedger ledger,CrewLedger.Crew c,long now){
        ledger.electSuccessor(c,now);
    }
    public static void stopCrewCombat(ServerLevel level,CrewLedger.Crew c,long now) {
        for(UUID id:c.members) {
            var npc=state(level).loaded.get(id);if(npc==null)continue;
            Brawl fight=state(level).brawls.get(id);if(fight!=null)stopBrawl((ServerLevel)npc.level(),fight,now);
            clearCombatBrain(npc);npc.getNavigation().stop();state(level).lastAttack.remove(id);
        }
    }
    public static VillagerEntityMCA nearestPirate(ServerLevel level, ServerPlayer player,int radius){
        VillagerEntityMCA best=null;double dist=radius*radius;
        for(VillagerEntityMCA npc:level.getEntitiesOfClass(VillagerEntityMCA.class,player.getBoundingBox().inflate(radius))){
            if(!isPirate(npc))continue;
            double d=player.distanceToSqr(npc);if(d<dist){dist=d;best=npc;}
        }
        return best;
    }
    /** Contextual interactions use the server's eye ray, never an arbitrary nearest NPC. */
    public static VillagerEntityMCA targetedPirate(ServerLevel level,ServerPlayer player,int radius) {
        var from=player.getEyePosition();var to=from.add(player.getLookAngle().scale(radius));
        VillagerEntityMCA chosen=null;double nearest=Double.POSITIVE_INFINITY;boolean ambiguous=false;
        for(var npc:level.getEntitiesOfClass(VillagerEntityMCA.class,player.getBoundingBox().inflate(radius))) {
            if(!npc.isAlive() || !isPirate(npc) || !player.hasLineOfSight(npc))continue;
            var hit=npc.getBoundingBox().inflate(0.1).clip(from,to);
            if(hit.isEmpty())continue;
            double d=from.distanceToSqr(hit.get());
            if(Math.abs(d-nearest)<0.01){ambiguous=true;continue;}
            if(d<nearest){chosen=npc;nearest=d;ambiguous=false;}
        }
        return ambiguous?null:chosen;
    }
    public static CrewLedger.Crew playerCrew(ServerLevel level,ServerPlayer player){
        for(CrewLedger.Crew c:ledger(level).crews())if(c.playerCaptain&&player.getUUID().equals(c.captain))return c;
        return null;
    }
    public static int issueOrder(ServerLevel level,ServerPlayer player,CrewLedger.Order order,boolean all){
        return issueOrder(level,player,order,all?"ALL":"NEARBY");
    }
    public static LivingEntity targetedHostile(ServerLevel level,ServerPlayer player,int radius) {
        var from=player.getEyePosition();var to=from.add(player.getLookAngle().scale(radius));
        LivingEntity result=null;double nearest=Double.POSITIVE_INFINITY;
        for(var monster:level.getEntitiesOfClass(Monster.class,player.getBoundingBox().inflate(radius))) {
            if(!monster.isAlive() || !player.hasLineOfSight(monster))continue;
            var hit=monster.getBoundingBox().inflate(0.1).clip(from,to);
            if(hit.isPresent() && from.distanceToSqr(hit.get())<nearest) {nearest=from.distanceToSqr(hit.get());result=monster;}
        }
        return result;
    }
    public static int issueOrder(ServerLevel level,ServerPlayer player,CrewLedger.Order order,String selection){
        return issueOrder(level,player,order,selection,null);
    }
    public static int issueOrder(ServerLevel level,ServerPlayer player,CrewLedger.Order order,String selection,LivingEntity explicitTarget){
        CrewLedger.Crew c=playerCrew(level,player);if(c==null)return 0;
        LivingEntity target=order==CrewLedger.Order.ATTACK?(explicitTarget==null?targetedHostile(level,player,24):explicitTarget):null;
        if(target!=null && (!(target instanceof Monster) || !target.isAlive() || target.level()!=level || player.distanceToSqr(target)>24*24 || !player.hasLineOfSight(target)))return 0;
        if(order==CrewLedger.Order.ATTACK && target==null)return 0;
        var directive=new CrewLedger.Directive(level.dimension().toString(),player.getX(),player.getY(),player.getZ(),target==null?null:target.getUUID());
        if((order==CrewLedger.Order.RALLY || order==CrewLedger.Order.GUARD) && c.flag!=null) {
            if(!c.flag.dimension().equals(level.dimension().identifier().toString()))return 0;
            directive=new CrewLedger.Directive(level.dimension().toString(),c.flag.x()+.5,c.flag.y(),c.flag.z()+.5,null);
        }
        var selected=selection.equals("NEAREST")?targetedPirate(level,player,6):null;
        if(selection.equals("NEAREST") && selected==null)return 0;
        int count=0;long now=level.getGameTime();
        for(VillagerEntityMCA npc:new ArrayList<>(state(level).loaded.values())){
            if(npc.level()!=level || !npc.isAlive() || !isPirate(npc))continue;
            if(selection.equals("NEAREST") && npc!=selected || selection.equals("NEARBY") && player.distanceToSqr(npc)>12*12)continue;
            CrewLedger.Member m=ledger(level).member(npc.getUUID());
            if(m==null||!m.alive||!m.adult||!m.crew.equals(c.id)||m.role==CrewLedger.Role.CAPTAIN)continue;
            if((order==CrewLedger.Order.RALLY || order==CrewLedger.Order.GUARD) && npc.distanceToSqr(directive.x(),directive.y(),directive.z())>64*64){ledger(level).event(c,"ORDER_REFUSED "+m.id+" "+order+" DESTINATION_TOO_FAR");continue;}
            int obedience=(int)(.35*m.loyalty+.25*m.respect+.15*m.fear+.25*m.morale);
            if(order==CrewLedger.Order.ATTACK)obedience-=30;
            if((order==CrewLedger.Order.ATTACK || order==CrewLedger.Order.GUARD || order==CrewLedger.Order.HOLD) && m.retreating && nearestMonster(level,npc,12)!=null) {
                ledger(level).event(c,"ORDER_REFUSED "+m.id+" "+order+" LOW_HEALTH_UNSAFE");continue;
            }
            if(order!=CrewLedger.Order.CEASE_FIGHT && order!=CrewLedger.Order.RETREAT && order!=CrewLedger.Order.DISMISS && state(level).brawls.containsKey(m.id)) {
                ledger(level).event(c,"ORDER_REFUSED "+m.id+" "+order+" IN_BRAWL");continue;
            }
            if(order!=CrewLedger.Order.DISMISS && npc.getRandom().nextInt(100)>=Math.max(10,obedience)){
                ledger(level).event(c,"ORDER_REFUSED "+npc.getUUID()+" "+order+" LOW_AUTHORITY");continue;
            }
            if(order==CrewLedger.Order.RETREAT) {
                Brawl fight=state(level).brawls.get(npc.getUUID());if(fight!=null)stopBrawl(level,fight,now);
            }
            // HOLD records the sailor's own position; GUARD/RALLY use the captain's chosen point.
            var anchor=order==CrewLedger.Order.HOLD?new CrewLedger.Directive(level.dimension().toString(),npc.getX(),npc.getY(),npc.getZ(),null):directive;
            ledger(level).order(m,order==CrewLedger.Order.DISMISS?CrewLedger.Order.NONE:order,now,anchor);
            if(order==CrewLedger.Order.DISMISS){clearCombatBrain(npc);npc.getNavigation().stop();}
            count++;
        }
        ledger(level).save();return count;
    }
}
