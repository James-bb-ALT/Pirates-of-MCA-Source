package dev.arandu.piratesmca;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Independent domain model. Server-owned, versioned, saved per-world. Never stores Minecraft entity objects. */
public final class CrewLedger {
    public enum Role { CAPTAIN, FIRST_MATE, QUARTERMASTER, CREW }
    public enum Phase { CALM, DISCONTENT, CONSPIRACY, ULTIMATUM, REBELLION, RESOLUTION, AFTERMATH }
    public enum Order { NONE, FOLLOW, HOLD, RALLY, GUARD, ATTACK, RETREAT, CEASE_FIGHT, DISMISS }
    public record FlagAnchor(String dimension,int x,int y,int z) {
        public FlagAnchor {if(dimension==null || dimension.isBlank())throw new IllegalArgumentException("Missing flag dimension");}
    }
    public record Directive(String dimension,double x,double y,double z,UUID target) {
        public Directive {
            Objects.requireNonNull(dimension,"dimension");
            if(dimension.isBlank() || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
                throw new IllegalArgumentException("Invalid order anchor");
        }
    }
    public static final class Member {
        public UUID id, crew;
        public Role role = Role.CREW;
        public String profile = "friendly";
        public int loyalty=50, fear=10, respect=50, resentment=0, morale=55, ambition=50, courage=65;
        public int greed=50,sociability=50;
        public String allegiance="NEUTRAL";
        public boolean adult=true, alive=true;
        public boolean retreating;
        public final Set<UUID> witnessedEvents=new LinkedHashSet<>();
        public Order order=Order.NONE;
        public Directive directive;
        public long lastProvocation, lastFightEnd, lastAssault, lastOrder;
        public long lastPersuasion;
        Member(UUID id, UUID crew) {this.id=id; this.crew=crew;}
    }
    public static final class SocialEdge {
        public final UUID from,to;
        public int friendship=0,trust=50,respect=50,resentment=0;
        public long lastInteraction;
        SocialEdge(UUID from,UUID to){this.from=from;this.to=to;}
    }
    public static final class Crew {
        public UUID id, captain;
        public boolean playerCaptain;
        public UUID firstMate, quartermaster, actingCaptain;
        public String anchor="";
        public FlagAnchor flag;
        public Phase phase=Phase.CALM;
        public UUID rebelLeader;
        public UUID lastDeposedCaptain;
        public long ultimatumUntil, cooldownUntil, lastEvent;
        public long ultimatumRemaining;
        public long surrenderProtectionUntil;
        public long nextConspiracyEvaluation,nextUltimatumNotice;
        public int diplomacy=0;
        public final Set<UUID> members=new LinkedHashSet<>();
        public final ArrayDeque<String> events = new ArrayDeque<>();
        public final ArrayDeque<SocialEvent> socialEvents=new ArrayDeque<>();
        Crew(UUID id) {this.id=id;}
    }
    private final Map<UUID,Crew> crews=new HashMap<>();
    private final Map<UUID,Member> members=new HashMap<>();
    private final Map<String,SocialEdge> relationships=new HashMap<>();
    private final Path file;
    private boolean dirty;
    private long clock;
    private Runnable persistence;
    public CrewLedger(Path file) {this.file=Objects.requireNonNull(file).toAbsolutePath().normalize(); load();}
    private CrewLedger(Map<String,String> state) {this.file=null;Properties p=new Properties();p.putAll(state);load(p);}
    public static CrewLedger fromState(Map<String,String> state){return new CrewLedger(state);}
    void bindPersistence(Runnable persistence){this.persistence=Objects.requireNonNull(persistence);}
    void acknowledgePersisted(){dirty=false;}
    public boolean hasChanges(){return dirty;}
    public Map<String,String> exportState() {
        Properties properties=properties();Map<String,String> state=new TreeMap<>();
        for(String key:properties.stringPropertyNames())state.put(key,properties.getProperty(key));return state;
    }
    public Collection<Crew> crews(){return crews.values();}
    public Member member(UUID id){return members.get(id);}
    public void gameTick(long tick){clock=Math.max(0,tick);}
    public SocialEdge existingRelationship(UUID from,UUID to){return relationships.get(from+"/"+to);}
    /** A /summon pirate has no vessel and must not acquire captaincy until recruited. */
    public boolean isFreeSolo(Member member) {
        if (member == null) return false;
        Crew c = crew(member.crew);
        return c != null && "UNASSIGNED".equals(c.anchor)
                && c.members.size() == 1 && c.members.contains(member.id) && !c.playerCaptain;
    }
    /** Repair Alpha 1-3's auto-appointed captain of a one-NPC provisional crew. */
    public void repairAutoCaptain(Member member) {
        if (!isFreeSolo(member)) return;
        Crew c = crew(member.crew);
        if (c.captain != null || member.role != Role.CREW) {
            c.captain = null;
            c.playerCaptain = false;
            c.firstMate = null;
            c.quartermaster = null;
            c.actingCaptain = null;
            member.role = Role.CREW;
            c.phase = Phase.CALM;
            c.rebelLeader = null;
            c.ultimatumUntil = 0;
            event(c, "SOLO_CAPTAIN_MIGRATED " + member.id);
        }
    }
    public SocialEdge relationship(UUID from,UUID to){
        if(from==null||to==null||from.equals(to))return null;
        dirty=true;
        return relationships.computeIfAbsent(from+"/"+to,k->new SocialEdge(from,to));
    }
    public void witnessMurder(UUID witness,UUID victim,UUID killer,long now){
        Member m=members.get(witness);if(m==null)return;
        SocialEvent event=murderEvent(crew(m.crew),victim,killer,now);
        applyKnowledge(m,event,true,now);
    }
    private SocialEvent murderEvent(Crew c,UUID victim,UUID killer,long now) {
        UUID id=UUID.nameUUIDFromBytes(("MURDER/"+victim+"/"+killer).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return socialEvent(c,id,"MURDER",now,killer,victim,"DIRECT_KILL",100,List.of());
    }
    private boolean applyKnowledge(Member witness,SocialEvent event,boolean direct,long now) {
        if(!witness.alive || !witness.adult || witness.id.equals(event.actor) || witness.id.equals(event.target) || !learn(witness,event.id))return false;
        if(direct)event.witnesses.add(witness.id);
        if(!claimConsequence(event,witness.id+(direct?"/DIRECT":"/RUMOR")))return false;
        if(event.kind.equals("MURDER") || event.kind.equals("CAPTAIN_ASSAULT")) {
            SocialEdge bond=existingRelationship(witness.id,event.target);
            boolean friend=bond!=null&&bond.friendship>30,enemy=bond!=null&&bond.friendship< -30;
            int penalty=enemy?0:event.kind.equals("MURDER")?(friend?60:35):(friend?7:2);
            if(!direct)penalty/=2;
            SocialEdge view=relationship(witness.id,event.actor);
            if(view!=null) {
                if(enemy){view.friendship=Math.min(100,view.friendship+(direct?5:2));view.respect=Math.min(100,view.respect+(direct?5:2));}
                else {view.friendship=Math.max(-100,view.friendship-(direct?25:12));view.trust=Math.max(0,view.trust-(direct?30:15));view.resentment=Math.min(100,view.resentment+penalty);}
                view.lastInteraction=now;
            }
            Crew c=crew(witness.crew);
            if(c!=null && Objects.equals(c.captain,event.actor) && penalty>0) {
                witness.resentment=Math.min(100,witness.resentment+penalty);
                witness.loyalty=Math.max(0,witness.loyalty-(event.kind.equals("MURDER")?(direct?15:7):1));
            }
        }
        return true;
    }
    public boolean witnessAssault(UUID witness,SocialEvent event,long now) {
        Member m=members.get(witness);
        return m!=null && m.crew.equals(event.crew) && applyKnowledge(m,event,true,now);
    }
    public boolean shareRumor(UUID from,UUID to,UUID eventId,long now) {
        Member sender=members.get(from),receiver=members.get(to);
        if(sender==null || receiver==null || !sender.alive || !sender.adult || !sender.crew.equals(receiver.crew) || !sender.witnessedEvents.contains(eventId))return false;
        Crew c=crew(sender.crew);SocialEvent source=socialEvent(c,eventId);
        if(source==null || !(source.kind.equals("MURDER") || source.kind.equals("CAPTAIN_ASSAULT")) || !applyKnowledge(receiver,source,false,now))return false;
        UUID id=UUID.nameUUIDFromBytes(("RUMOR/"+eventId+"/"+from+"/"+to).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var rumor=socialEvent(c,id,"RUMOR_SHARED",now,from,to,"source="+eventId+" credibility=0.5",source.severity/2,List.of(from,to));
        claimConsequence(rumor,to+"/LEARNED_"+eventId);return true;
    }
    public Crew crew(UUID id){return crews.get(id);}
    public Crew crewAtFlag(FlagAnchor flag){for(Crew c:crews.values())if(flag.equals(c.flag))return c;return null;}
    public boolean bindFlag(Crew c,UUID captain,FlagAnchor flag) {
        if(c==null || !c.playerCaptain || !Objects.equals(c.captain,captain))return false;
        Crew existing=crewAtFlag(flag);if(existing!=null && existing!=c)return false;
        c.flag=Objects.requireNonNull(flag);
        var event=socialEvent(c,UUID.randomUUID(),"FLAG_LINKED",clock,captain,null,flag.toString(),0,List.of(captain));
        claimConsequence(event,"RALLY_ANCHOR_UPDATED");return true;
    }
    public boolean canVolunteer(Member m,int hearts,boolean met,boolean inCombat) {
        return m!=null && isFreeSolo(m) && m.alive && m.adult && m.role==Role.CREW && m.morale>=35 && m.fear<80 && hearts>=0 && met && !inCombat;
    }
    public boolean shareLatestRumor(Member from,Member to,long now) {
        Crew c=crew(from.crew);UUID news=null;
        var iterator=c.socialEvents.descendingIterator();
        while(iterator.hasNext()) {
            var event=iterator.next();
            if((event.kind.equals("MURDER") || event.kind.equals("CAPTAIN_ASSAULT")) && from.witnessedEvents.contains(event.id) && !to.witnessedEvents.contains(event.id)){news=event.id;break;}
        }
        return news!=null && shareRumor(from.id,to.id,news,now);
    }
    public SocialEvent resourceDispute(Member a,Member b,UUID item,long now) {
        if(a==null || b==null || !a.alive || !b.alive || !a.adult || !b.adult || !a.crew.equals(b.crew))return null;
        Crew c=crew(a.crew);UUID id=UUID.nameUUIDFromBytes(("RESOURCE_DISPUTE/"+c.id+"/"+item).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var event=socialEvent(c,id,"RESOURCE_DISPUTE",now,a.id,b.id,"contested_item="+item,15,List.of(a.id,b.id));
        if(claimConsequence(event,"RIVALRY_INCREASED")) {
            for(Member member:List.of(a,b)) {
                UUID other=member==a?b.id:a.id;SocialEdge edge=relationship(member.id,other);
                edge.friendship=Math.max(-100,edge.friendship-10);edge.resentment=Math.min(100,edge.resentment+15);edge.lastInteraction=now;
                learn(member,event.id);
            }
        }
        return event;
    }
    public SocialEvent provoke(Member a,Member b,long now) {
        if(a==null || b==null || !a.alive || !b.alive || !a.adult || !b.adult || !a.crew.equals(b.crew) || a.role==Role.CAPTAIN || b.role==Role.CAPTAIN)return null;
        if(a.lastFightEnd>0 && now-a.lastFightEnd<9600 || b.lastFightEnd>0 && now-b.lastFightEnd<9600 || a.lastProvocation>0 && now-a.lastProvocation<2400)return null;
        SocialEdge edge=existingRelationship(a.id,b.id);
        if(edge==null || edge.friendship> -30 && edge.resentment<25)return null;
        a.lastProvocation=now;
        var event=socialEvent(crew(a.crew),UUID.randomUUID(),"PROVOCATION",now,a.id,b.id,"RIVALRY edge_last_interaction="+edge.lastInteraction,25,List.of(a.id,b.id));
        learn(a,event.id);learn(b,event.id);return event;
    }
    public boolean rollProvocation(SocialEvent event,float roll) {
        if(!event.kind.equals("PROVOCATION") || !Float.isFinite(roll) || roll<0 || roll>=1)throw new IllegalArgumentException("Invalid provocation roll");
        if(!claimConsequence(event,"PROVOCATION_ROLLED"))return false;
        boolean starts=roll<.15f;claimConsequence(event,starts?"FIGHT_TRIGGERED":"NO_FIGHT");return starts;
    }
    public boolean recentCaptainAbuse(Crew c,long now) {
        for(var event:c.socialEvents)if((event.kind.equals("MURDER") || event.kind.equals("CAPTAIN_ASSAULT")) && Objects.equals(c.captain,event.actor) && now>=event.tick && now-event.tick<=2400)return true;
        return false;
    }
    public Crew getOrCreate(UUID id,String anchor) {
        Crew c=crews.get(id);
        if(c==null){c=new Crew(id);crews.put(id,c);event(c,"CREW_CREATED");dirty=true;}
        if(c.anchor.isBlank() && anchor!=null&&!anchor.isBlank()){c.anchor=anchor;dirty=true;}
        return c;
    }
    /** Keep a member's social profile aligned with the final, persistent MCA profile tag. */
    public void updateProfile(Member member,String profile) {
        if(member!=null && profile!=null && !profile.isBlank() && !profile.equals(member.profile)) {
            member.profile=profile;
            dirty=true;
        }
    }
    public Member add(UUID id,UUID crew,String profile) {
        Member m=members.get(id);
        if(m==null) {
            m=new Member(id,crew);
            int seed=id.hashCode();
            m.ambition=20+Math.floorMod(seed,70);
            m.courage=35+Math.floorMod(seed>>>8,65);
            m.greed=profile.equals("greedy")?80:20+Math.floorMod(seed>>>12,65);
            m.sociability=profile.equals("friendly")?80:20+Math.floorMod(seed>>>17,65);
            m.loyalty=30+Math.floorMod(seed>>>3,55);
            m.profile=profile;
            members.put(id,m);
            dirty=true;
        }
        if (!m.crew.equals(crew) || !m.alive) return m; // Never silently reassign or resurrect a dead record
        if(getOrCreate(crew, "").members.add(id))dirty=true;
        return m;
    }
    public void captain(Crew c, UUID id, boolean player) {
        Objects.requireNonNull(id, "captain");
        if (!player && (!c.members.contains(id) || members.get(id) == null
                || !c.id.equals(members.get(id).crew) || !members.get(id).adult || !members.get(id).alive))
            throw new IllegalArgumentException("Captain must belong to this crew");
        if (player && crews.values().stream().anyMatch(other -> other != c
                && other.playerCaptain && id.equals(other.captain)))
            throw new IllegalArgumentException("Player already commands a crew");
        if(c.captain != null && !c.playerCaptain) {
            Member old=members.get(c.captain);
            if(old!=null) old.role=Role.CREW;
        }
        c.captain=id; c.playerCaptain=player;c.actingCaptain=null;
        if(id.equals(c.lastDeposedCaptain))c.surrenderProtectionUntil=0;
        clearOffice(c, id);
        if(!player) {
            Member m=members.get(id); if(m!=null) m.role=Role.CAPTAIN;
        }
        c.phase=Phase.CALM;
        c.rebelLeader=null;
        event(c,"CAPTAIN_APPOINTED "+id);
    }
    public void depose(Crew c,long now){
        c.lastDeposedCaptain=c.captain;
        if(c.captain != null && !c.playerCaptain){Member m=members.get(c.captain);if(m!=null)m.role=Role.CREW;}
        c.captain=null;c.playerCaptain=false;c.phase=Phase.AFTERMATH;c.cooldownUntil=now+24000;
        var event=socialEvent(c,UUID.randomUUID(),"CAPTAIN_DEPOSED",now,c.lastDeposedCaptain,null,"AUTHORITY_REMOVED",75,List.of());
        claimConsequence(event,"DEFINITIVE_CAPTAIN_CLEARED");
    }
    public void voluntaryDeparture(Crew c,long now,boolean surrender) {
        UUID former=c.captain;depose(c,now);c.surrenderProtectionUntil=now+1200;
        var event=socialEvent(c,UUID.randomUUID(),surrender?"CAPTAIN_YIELDED":"CAPTAIN_RESIGNED",now,former,null,"VOLUNTARY_DEPARTURE",0,List.of());
        claimConsequence(event,"SAFE_EXIT_1200_WORLD_TICKS");
    }
    public void breakDepartureProtection(Crew c,UUID attacker,UUID victim,long now) {
        if(c.surrenderProtectionUntil<=now || !Objects.equals(c.lastDeposedCaptain,attacker))return;
        c.surrenderProtectionUntil=0;
        var event=socialEvent(c,UUID.randomUUID(),"SURRENDER_BROKEN",now,attacker,victim,"FORMER_CAPTAIN_ATTACKED_CREW",50,List.of(victim));
        claimConsequence(event,"SAFE_EXIT_REVOKED");
    }
    public boolean transfer(Member m, Crew destination){
        if(m==null||destination==null||m.crew.equals(destination.id))return false;
        Crew origin=crew(m.crew);if(origin!=null){origin.members.remove(m.id);clearOffice(origin,m.id);if(m.id.equals(origin.captain))depose(origin,0);}
        m.crew=destination.id;destination.members.add(m.id);m.role=Role.CREW;m.order=Order.NONE;m.directive=null;dirty=true;
        event(destination,"MEMBER_RECRUITED "+m.id);return true;
    }
    public void officer(Crew c, UUID member,Role role){
        if(role!=Role.FIRST_MATE && role!=Role.QUARTERMASTER) throw new IllegalArgumentException("officer role");
        if(!c.members.contains(member)) throw new IllegalArgumentException("Not a crew member");
        if(!members.get(member).adult || !members.get(member).alive)throw new IllegalArgumentException("Officer must be a living adult");
        if(member.equals(c.captain)) throw new IllegalArgumentException("Captain cannot occupy an officer position");
        clearOffice(c, member);
        UUID previous=role==Role.FIRST_MATE?c.firstMate:c.quartermaster;
        if(previous!=null && members.containsKey(previous))members.get(previous).role=Role.CREW;
        if(role==Role.FIRST_MATE){c.firstMate=member;c.actingCaptain=null;} else c.quartermaster=member;
        members.get(member).role=role;
        event(c,"OFFICER_APPOINTED "+role+" "+member);
    }
    private void clearOffice(Crew c, UUID id) {
        if(id.equals(c.actingCaptain))c.actingCaptain=null;
        if (id.equals(c.firstMate)) c.firstMate = null;
        if (id.equals(c.quartermaster)) c.quartermaster = null;
    }
    public void order(Member m, Order o, long tick){order(m,o,tick,null);}
    public void order(Member m, Order o, long tick,Directive directive){
        boolean issued=o!=Order.NONE && o!=Order.DISMISS && (m.order!=o || m.lastOrder!=tick);
        m.order=o;m.lastOrder=tick;m.directive=o==Order.NONE||o==Order.DISMISS?null:directive;dirty=true;
        Crew c=crew(m.crew);
        if(issued && c!=null) {
            var event=socialEvent(c,UUID.randomUUID(),"ORDER_GIVEN",tick,c.captain,m.id,o.name()+" target="+(directive==null?"":str(directive.target())),0,List.of(m.id));
            claimConsequence(event,m.id+"/ORDER_"+o);learn(m,event.id);
        }
    }
    public void actingCaptain(Crew c,UUID id) {
        if(id!=null && (!id.equals(c.firstMate) || !validOfficer(c,id)))throw new IllegalArgumentException("Only the living first mate may act");
        if(Objects.equals(c.actingCaptain,id))return;
        c.actingCaptain=id;event(c,"ACTING_CAPTAIN "+str(id));
    }
    public void retreating(Member m,boolean retreating,UUID threat,long now) {
        if(m.retreating==retreating)return;
        m.retreating=retreating;
        var event=socialEvent(crew(m.crew),UUID.randomUUID(),retreating?"RETREAT_STARTED":"RETREAT_ENDED",now,m.id,threat,retreating?"LOW_HEALTH":"RECOVERED",retreating?25:0,List.of(m.id));
        claimConsequence(event,retreating?"COMBAT_SUSPENDED":"COMBAT_RESUMED");
    }
    public void assault(UUID victim,UUID attacker,long now){assaultRecord(victim,attacker,now);}
    public SocialEvent assaultRecord(UUID victim,UUID attacker,long now){
        Member m=members.get(victim);if(m==null || !m.alive || !m.adult)return null;
        Crew c=crew(m.crew);if(c==null || !attacker.equals(c.captain))return null;
        if(m.lastAssault>0 && now-m.lastAssault<15)return null;
        m.lastAssault=now;m.resentment=Math.min(100,m.resentment+9);
        m.loyalty=Math.max(0,m.loyalty-5);m.fear=Math.min(100,m.fear+3);
        m.respect=Math.max(0,m.respect-2);
        var event=socialEvent(c,UUID.randomUUID(),"CAPTAIN_ASSAULT",now,attacker,victim,"CAPTAIN_DISCIPLINE",25,List.of(victim));
        learn(m,event.id);claimConsequence(event,victim+"/RESENTMENT_9_FEAR_3_LOYALTY_MINUS_5");return event;
    }
    public void fight(UUID a,UUID b,long now, boolean start){
        Member ma=members.get(a), mb=members.get(b);
        if(ma==null||mb==null||!ma.crew.equals(mb.crew))return;
        Crew c=crew(ma.crew);
        if(start)event(c,"BRAWL_STARTED "+a+" "+b);
        else {ma.lastFightEnd=mb.lastFightEnd=now; ma.resentment=Math.min(100,ma.resentment+4); mb.resentment=Math.min(100,mb.resentment+4);event(c,"BRAWL_ENDED "+a+" "+b);}
    }
    public void removeDead(UUID victim,long now,String cause){
        Member m=members.get(victim);if(m==null)return;
        Crew c=crew(m.crew);if(c==null)return;
        if(!m.alive)return;
        m.alive=false;m.order=Order.NONE;m.directive=null;m.role=Role.CREW;
        c.members.remove(victim);
        if(victim.equals(c.captain))depose(c,now);
        clearOffice(c,victim);
        event(c,"DEATH "+victim+" "+cause);
    }
    public void murder(UUID victim,UUID killer,long now){
        Member m=members.get(victim);if(m==null)return;
        if(!m.alive)return;
        Crew c=crew(m.crew);if(c==null)return;
        // Only crew members known to this ledger learn the crime; witnesses are handled by runtime.
        var event=murderEvent(c,victim,killer,now);
        claimConsequence(event,victim+"/REMOVED_FROM_ACTIVE_ROSTER");
        removeDead(victim,now,"MURDER");
    }
    public void event(Crew c,String event){
        if(c==null)return;
        String[] tokens=event.split(" ");
        String kind=tokens[0].matches("[A-Z_]{1,64}")?tokens[0]:"LEGACY_NOTE";
        if(kind.equals("OFFICER_APPOINTED") || kind.equals("CAPTAIN_APPOINTED"))kind="ROLE_APPOINTED";
        UUID actor=null,target=null;
        for(int i=1;i<tokens.length;i++) {
            UUID id=uuid(tokens[i]);if(id!=null){if(actor==null)actor=id;else if(target==null){target=id;break;}}
        }
        SocialEvent record=new SocialEvent(UUID.randomUUID(),c.id,kind,clock,actor,target,event,0,List.of());
        record.appliedConsequences.add("LEGACY_OPERATION_COMPLETED");
        appendEvent(c,record,event);
    }
    private void appendEvent(Crew c,SocialEvent record,String text) {
        c.socialEvents.addLast(record);while(c.socialEvents.size()>128)c.socialEvents.removeFirst();
        c.events.addLast(text);
        while(c.events.size()>128)c.events.removeFirst();
        dirty=true;
    }
    public SocialEvent socialEvent(Crew c,UUID id) {
        for(SocialEvent event:c.socialEvents)if(event.id.equals(id))return event;
        return null;
    }
    public SocialEvent socialEvent(Crew c,UUID id,String kind,long now,UUID actor,UUID target,String reason,int severity,Collection<UUID> witnesses) {
        SocialEvent existing=socialEvent(c,id);if(existing!=null)return existing;
        var event=new SocialEvent(id,c.id,kind,now,actor,target,reason,severity,witnesses);
        appendEvent(c,event,kind+" "+str(actor)+" "+str(target)+" "+reason);return event;
    }
    public boolean claimConsequence(SocialEvent event,String key) {
        if(!event.appliedConsequences.add(key))return false;
        dirty=true;return true;
    }
    private boolean learn(Member m,UUID event) {
        if(!m.witnessedEvents.add(event))return false;
        while(m.witnessedEvents.size()>128)m.witnessedEvents.remove(m.witnessedEvents.iterator().next());
        dirty=true;return true;
    }
    private boolean eligible(Crew c, UUID id) {
        Member m=members.get(id);
        return m!=null && m.alive && m.adult && c.id.equals(m.crew) && !id.equals(c.captain);
    }
    public int conspirators(Crew c){int n=0;for(UUID id:c.members){Member m=members.get(id);if(eligible(c,id)&&m.resentment>=65&&m.fear<80)n++;}return n;}
    public int requiredRebels(Crew c){
        int eligible=eligibleCrewCount(c);
        return Math.max(2,(eligible*2+4)/5);
    }
    public int eligibleCrewCount(Crew c){int count=0;for(UUID id:c.members)if(eligible(c,id))count++;return count;}
    public boolean startConspiracy(Crew c,long tick){
        if(c.phase!=Phase.CALM && c.phase!=Phase.DISCONTENT || tick<c.cooldownUntil || c.captain==null)return false;
        int supporters=0; Member leader=null;
        for(UUID id:c.members){
            Member m=members.get(id);
            if(!eligible(c,id))continue;
            if(m.resentment>=65 && m.fear<80){supporters++;if(leader==null||m.ambition>leader.ambition)leader=m;}
        }
        if(leader==null||leader.ambition<55)return false;
        c.rebelLeader=leader.id;c.phase=Phase.CONSPIRACY;
        var event=socialEvent(c,UUID.randomUUID(),"CONSPIRACY_STARTED",tick,leader.id,c.captain,"RESENTMENT_AND_AMBITION",60,List.of(leader.id));
        leader.allegiance="CONSPIRATOR";learn(leader,event.id);claimConsequence(event,"PHASE_CONSPIRACY");
        return true;
    }
    public void evaluateDiscontent(Crew c,long now) {
        if(c.phase!=Phase.CALM && c.phase!=Phase.DISCONTENT)return;
        boolean discontent=c.members.stream().filter(id->eligible(c,id)).map(members::get).anyMatch(m->m.resentment>=40 || m.morale<35);
        Phase next=discontent?Phase.DISCONTENT:Phase.CALM;
        if(next!=c.phase){c.phase=next;socialEvent(c,UUID.randomUUID(),"DISCONTENT_CHANGED",now,null,c.captain,next.name(),discontent?40:0,List.of());}
    }
    public boolean persuadeConspirator(Member leader,Member candidate,long now) {
        if(leader==null || candidate==null || !leader.crew.equals(candidate.crew) || !leader.alive || !candidate.alive || !candidate.adult || candidate.fear>=80 || candidate.resentment<40 || candidate.lastPersuasion>0 && now-candidate.lastPersuasion<600)return false;
        Crew c=crew(leader.crew);if(c.phase!=Phase.CONSPIRACY || !leader.id.equals(c.rebelLeader) || candidate.id.equals(c.captain))return false;
        boolean knowsCause=c.socialEvents.stream().anyMatch(e->candidate.witnessedEvents.contains(e.id) && Objects.equals(e.actor,c.captain) && (e.kind.equals("MURDER") || e.kind.equals("CAPTAIN_ASSAULT")));
        SocialEdge bond=existingRelationship(candidate.id,leader.id);
        int score=(bond==null?50:bond.trust+bond.friendship/2)+leader.respect/4+candidate.resentment-candidate.fear-candidate.loyalty/2;
        if(!knowsCause || score<60)return false;
        candidate.lastPersuasion=now;candidate.resentment=Math.min(100,candidate.resentment+5);
        if(candidate.resentment>=65)candidate.allegiance="CONSPIRATOR";
        var event=socialEvent(c,UUID.randomUUID(),"CONSPIRATOR_RECRUITED",now,leader.id,candidate.id,"KNOWN_CAPTAIN_ABUSE confidence="+score,20,List.of(leader.id,candidate.id));
        claimConsequence(event,candidate.id+"/RESENTMENT_PLUS_5");return true;
    }
    public boolean beginUltimatum(Crew c,long tick){
        if(c.phase!=Phase.CONSPIRACY || conspirators(c)<requiredRebels(c))return false;
        c.phase=Phase.ULTIMATUM;c.ultimatumUntil=tick+1200;
        c.ultimatumRemaining=1200;
        event(c,"MUTINY_ULTIMATUM");return true;
    }
    public void beginRebellion(Crew c){
        if(c.phase!=Phase.ULTIMATUM)return;
        c.phase=Phase.REBELLION;
        for(UUID id:c.members){Member m=members.get(id);if(m!=null)m.allegiance=eligible(c,id)&&m.resentment>=65&&m.fear<80?"REBEL":"LOYAL";}
        event(c,"MUTINY_STARTED");
    }
    /** Only elapsed loaded time with the captain present is counted; callers never replay offline time. */
    public void tickUltimatum(Crew c,long elapsed,boolean captainPresent) {
        if(c.phase!=Phase.ULTIMATUM || !captainPresent || elapsed<=0)return;
        c.ultimatumRemaining=Math.max(0,c.ultimatumRemaining-Math.min(20,elapsed));
        dirty=true;
        if(c.ultimatumRemaining==0)beginRebellion(c);
    }
    public void finishRebellion(Crew c,long now,boolean captainWin){
        c.phase=Phase.RESOLUTION;
        socialEvent(c,UUID.randomUUID(),"MUTINY_RESOLUTION",now,c.captain,c.rebelLeader,captainWin?"CAPTAIN_PREVAILED":"CAPTAIN_DEFEATED",75,List.of());
        if(!captainWin)depose(c,now);
        else {
            for(UUID id:c.members){Member m=members.get(id);if(m!=null && m.allegiance.equals("REBEL")){m.fear=Math.min(100,m.fear+10);m.morale=Math.max(0,m.morale-15);m.allegiance="NEUTRAL";}}
            c.phase=Phase.AFTERMATH;c.cooldownUntil=now+24000;event(c,"MUTINY_DEFEATED");
        }
    }
    /** Deterministic council score from directed relationships; an officer has an advantage, not a guarantee. */
    public UUID electSuccessor(Crew c,long now) {
        UUID winner=null;long best=Long.MIN_VALUE;
        for(UUID candidate:c.members) {
            if(!eligible(c,candidate) || candidate.equals(c.lastDeposedCaptain))continue;
            Member m=members.get(candidate);
            long score=m.respect+m.ambition+(candidate.equals(c.firstMate)?20:0);
            for(UUID voter:c.members)if(eligible(c,voter) && !voter.equals(candidate)) {
                SocialEdge edge=relationships.get(voter+"/"+candidate);
                if(edge!=null)score+=edge.trust+edge.respect+edge.friendship-edge.resentment;
            }
            if(winner==null || score>best || score==best && candidate.toString().compareTo(winner.toString())<0) {
                winner=candidate;best=score;
            }
        }
        if(winner!=null){captain(c,winner,false);if(now<c.cooldownUntil)c.phase=Phase.AFTERMATH;event(c,"SUCCESSION "+winner+" council_score="+best);}
        return winner;
    }
    private Properties properties(){
        Properties p=new Properties();p.setProperty("schema", "2");
        for(Crew c:crews.values()){
            String pre="crew."+c.id+".";
            p.setProperty(pre+"anchor", c.anchor);p.setProperty(pre+"captain",str(c.captain));
            if(c.flag!=null){p.setProperty(pre+"flagDimension",c.flag.dimension());p.setProperty(pre+"flagX",Integer.toString(c.flag.x()));p.setProperty(pre+"flagY",Integer.toString(c.flag.y()));p.setProperty(pre+"flagZ",Integer.toString(c.flag.z()));}
            p.setProperty(pre+"player",Boolean.toString(c.playerCaptain));p.setProperty(pre+"mate",str(c.firstMate));p.setProperty(pre+"quartermaster",str(c.quartermaster));
            p.setProperty(pre+"actingCaptain",str(c.actingCaptain));
            p.setProperty(pre+"phase",c.phase.name());p.setProperty(pre+"leader",str(c.rebelLeader));
            p.setProperty(pre+"lastDeposedCaptain",str(c.lastDeposedCaptain));
            p.setProperty(pre+"ultimatum",Long.toString(c.ultimatumUntil));p.setProperty(pre+"cooldown",Long.toString(c.cooldownUntil));
            p.setProperty(pre+"ultimatumRemaining",Long.toString(c.ultimatumRemaining));
            p.setProperty(pre+"surrenderProtectionUntil",Long.toString(c.surrenderProtectionUntil));
            p.setProperty(pre+"members",join(c.members));
            int n=0;for(String e:c.events)p.setProperty(pre+"event."+(n++),e);
            n=0;for(SocialEvent event:c.socialEvents) {
                String ep=pre+"record."+(n++)+".";
                p.setProperty(ep+"id",event.id.toString());p.setProperty(ep+"kind",event.kind);
                p.setProperty(ep+"tick",Long.toString(event.tick));p.setProperty(ep+"actor",str(event.actor));p.setProperty(ep+"target",str(event.target));
                p.setProperty(ep+"reason",event.reason);p.setProperty(ep+"severity",Integer.toString(event.severity));
                p.setProperty(ep+"witnesses",join(event.witnesses));p.setProperty(ep+"consequences",String.join("\u001f",event.appliedConsequences));
            }
        }
        for(SocialEdge edge:relationships.values()){
            String pre="edge."+edge.from+"/"+edge.to+".";
            p.setProperty(pre+"friendship",Integer.toString(edge.friendship));
            p.setProperty(pre+"trust",Integer.toString(edge.trust));
            p.setProperty(pre+"respect",Integer.toString(edge.respect));
            p.setProperty(pre+"resentment",Integer.toString(edge.resentment));
            p.setProperty(pre+"time",Long.toString(edge.lastInteraction));
        }
        for(Member m:members.values()){
            String pre="member."+m.id+".";
            p.setProperty(pre+"crew",str(m.crew));p.setProperty(pre+"profile",m.profile);p.setProperty(pre+"role",m.role.name());
            p.setProperty(pre+"loyalty",Integer.toString(m.loyalty));p.setProperty(pre+"fear",Integer.toString(m.fear));p.setProperty(pre+"respect",Integer.toString(m.respect));
            p.setProperty(pre+"resentment",Integer.toString(m.resentment));p.setProperty(pre+"morale",Integer.toString(m.morale));p.setProperty(pre+"ambition",Integer.toString(m.ambition));p.setProperty(pre+"courage",Integer.toString(m.courage));
            p.setProperty(pre+"greed",Integer.toString(m.greed));p.setProperty(pre+"sociability",Integer.toString(m.sociability));
            p.setProperty(pre+"order",m.order.name());p.setProperty(pre+"lastFight",Long.toString(m.lastFightEnd));p.setProperty(pre+"lastAssault",Long.toString(m.lastAssault));
            p.setProperty(pre+"lastOrder",Long.toString(m.lastOrder));
            if(m.directive!=null) {
                p.setProperty(pre+"orderDimension",m.directive.dimension());
                p.setProperty(pre+"orderX",Double.toString(m.directive.x()));p.setProperty(pre+"orderY",Double.toString(m.directive.y()));p.setProperty(pre+"orderZ",Double.toString(m.directive.z()));
                p.setProperty(pre+"orderTarget",str(m.directive.target()));
            }
            p.setProperty(pre+"lastProvocation",Long.toString(m.lastProvocation));
            p.setProperty(pre+"lastPersuasion",Long.toString(m.lastPersuasion));
            p.setProperty(pre+"allegiance",m.allegiance);
            p.setProperty(pre+"adult",Boolean.toString(m.adult));p.setProperty(pre+"alive",Boolean.toString(m.alive));
            p.setProperty(pre+"retreating",Boolean.toString(m.retreating));
            p.setProperty(pre+"witnessedEvents",join(m.witnessedEvents));
        }
        return p;
    }
    public void save(){
        if(!dirty)return;
        if(persistence!=null){persistence.run();dirty=false;return;}
        if(file==null)throw new IllegalStateException("No persistence adapter bound");
        Properties p=properties();
        try {
            Files.createDirectories(file.getParent());
            Path tmp=file.resolveSibling(file.getFileName()+".tmp");
            if(Files.exists(file)) {
                Path original=file.resolveSibling(file.getFileName()+".pre-alpha6.bak");
                if(!Files.exists(original))Files.copy(file,original);
                Files.copy(file,file.resolveSibling(file.getFileName()+".bak"),StandardCopyOption.REPLACE_EXISTING);
            }
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();p.store(bytes,"Pirates of MCA per-world social state v2");
            try(var channel=java.nio.channels.FileChannel.open(tmp,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE)) {
                var buffer=java.nio.ByteBuffer.wrap(bytes.toByteArray());
                while(buffer.hasRemaining())channel.write(buffer);
                channel.force(true);
            }
            try{Files.move(tmp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException ex){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}
            dirty=false;
        }catch(IOException e){throw new UncheckedIOException("Failed to save crew ledger; previous file and backup preserved: "+file,e);}
    }
    private void load(){
        if(!Files.isRegularFile(file))return;
        Properties p=new Properties();
        try(InputStream in=Files.newInputStream(file)){p.load(in);}catch(IOException | IllegalArgumentException e){throw new IllegalStateException("Cannot read crew ledger; original preserved: "+file,e);}
        load(p);
    }
    private void load(Properties p){
        if(!"2".equals(p.getProperty("schema")))throw new IllegalStateException("Unsupported crew ledger schema; original preserved: "+file);
        boolean importedHistory=false;
        Set<UUID> crewIds=new HashSet<>(),memberIds=new HashSet<>();
        for(String key:p.stringPropertyNames()){
            String[] bits=key.split("\\.");
            if(bits.length<3)continue;
            try{
                if(bits[0].equals("crew"))crewIds.add(UUID.fromString(bits[1]));
                if(bits[0].equals("member"))memberIds.add(UUID.fromString(bits[1]));
            }catch(IllegalArgumentException invalid){throw new IllegalStateException("Invalid crew/member key; original preserved: "+key,invalid);}
        }
        for(UUID id:crewIds){
            Crew c=new Crew(id);String pre="crew."+id+".";
            c.anchor=p.getProperty(pre+"anchor","");c.captain=storedUuid(p.getProperty(pre+"captain"));
            if(p.containsKey(pre+"flagDimension"))try{c.flag=new FlagAnchor(p.getProperty(pre+"flagDimension"),Integer.parseInt(p.getProperty(pre+"flagX")),Integer.parseInt(p.getProperty(pre+"flagY")),Integer.parseInt(p.getProperty(pre+"flagZ")));}
            catch(RuntimeException invalid){throw new IllegalStateException("Invalid flag anchor; original preserved: "+file,invalid);}
            c.playerCaptain=Boolean.parseBoolean(p.getProperty(pre+"player","false"));
            c.firstMate=uuid(p.getProperty(pre+"mate"));c.quartermaster=uuid(p.getProperty(pre+"quartermaster"));
            c.actingCaptain=uuid(p.getProperty(pre+"actingCaptain"));
            c.phase=parse(Phase.class,p.getProperty(pre+"phase"),Phase.CALM);
            c.rebelLeader=uuid(p.getProperty(pre+"leader"));
            c.lastDeposedCaptain=uuid(p.getProperty(pre+"lastDeposedCaptain"));
            c.ultimatumUntil=num(p.getProperty(pre+"ultimatum"));c.cooldownUntil=num(p.getProperty(pre+"cooldown"));
            c.surrenderProtectionUntil=num(p.getProperty(pre+"surrenderProtectionUntil"));
            c.ultimatumRemaining=c.phase==Phase.ULTIMATUM
                ?Math.clamp(num(p.getProperty(pre+"ultimatumRemaining","1200")),0,1200):0;
            for(String item:p.getProperty(pre+"members","").split(",")){UUID member=uuid(item);if(member!=null)c.members.add(member);}
            for(int i=0;i<128;i++){String e=p.getProperty(pre+"event."+i);if(e==null)break;c.events.add(e);}
            for(int i=0;i<128;i++) {
                String ep=pre+"record."+i+".";if(!p.containsKey(ep+"id"))break;
                try {
                    var witnesses=new LinkedHashSet<UUID>();
                    for(String item:p.getProperty(ep+"witnesses","").split(",")){UUID witness=uuid(item);if(witness!=null)witnesses.add(witness);}
                    var event=new SocialEvent(UUID.fromString(p.getProperty(ep+"id")),id,p.getProperty(ep+"kind"),num(p.getProperty(ep+"tick")),uuid(p.getProperty(ep+"actor")),uuid(p.getProperty(ep+"target")),p.getProperty(ep+"reason",""),bound(p,ep+"severity",0),witnesses);
                    for(String consequence:p.getProperty(ep+"consequences","").split("\u001f"))if(!consequence.isBlank())event.appliedConsequences.add(consequence);
                    c.socialEvents.addLast(event);
                } catch(RuntimeException invalid){throw new IllegalStateException("Invalid social event; original preserved: "+file,invalid);}
            }
            if(c.socialEvents.isEmpty() && !c.events.isEmpty()) {
                int index=0;for(String text:c.events) {
                    UUID eventId=UUID.nameUUIDFromBytes(("LEGACY/"+c.id+"/"+(index++)+"/"+text).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    var event=new SocialEvent(eventId,c.id,"LEGACY_NOTE",0,null,null,"IMPORTED_LEGACY: "+text,0,List.of());
                    event.appliedConsequences.add("LEGACY_ALREADY_APPLIED");c.socialEvents.add(event);
                }
                importedHistory=true;
            }
            crews.put(id,c);
        }
        for(UUID id:memberIds){
            String pre="member."+id+".";UUID crew=uuid(p.getProperty(pre+"crew"));if(crew==null)continue;
            Member m=new Member(id,crew);m.profile=p.getProperty(pre+"profile","friendly");m.role=parse(Role.class,p.getProperty(pre+"role"),Role.CREW);
            m.loyalty=bound(p,pre+"loyalty",50);m.fear=bound(p,pre+"fear",10);m.respect=bound(p,pre+"respect",50);
            m.resentment=bound(p,pre+"resentment",0);m.morale=bound(p,pre+"morale",55);m.ambition=bound(p,pre+"ambition",50);m.courage=bound(p,pre+"courage",65);
            m.greed=bound(p,pre+"greed",m.profile.equals("greedy")?80:50);m.sociability=bound(p,pre+"sociability",m.profile.equals("friendly")?80:50);
            m.order=parse(Order.class,p.getProperty(pre+"order"),Order.NONE);m.lastFightEnd=num(p.getProperty(pre+"lastFight"));m.lastAssault=num(p.getProperty(pre+"lastAssault"));
            m.lastOrder=num(p.getProperty(pre+"lastOrder"));m.lastProvocation=num(p.getProperty(pre+"lastProvocation"));
            m.lastPersuasion=num(p.getProperty(pre+"lastPersuasion"));
            if(p.containsKey(pre+"orderDimension"))try {
                m.directive=new Directive(p.getProperty(pre+"orderDimension"),Double.parseDouble(p.getProperty(pre+"orderX")),Double.parseDouble(p.getProperty(pre+"orderY")),Double.parseDouble(p.getProperty(pre+"orderZ")),uuid(p.getProperty(pre+"orderTarget")));
            } catch(RuntimeException invalid){throw new IllegalStateException("Invalid order directive; original preserved: "+file,invalid);}
            m.allegiance=p.getProperty(pre+"allegiance","NEUTRAL");
            m.adult=Boolean.parseBoolean(p.getProperty(pre+"adult","true"));m.alive=Boolean.parseBoolean(p.getProperty(pre+"alive","true"));
            m.retreating=Boolean.parseBoolean(p.getProperty(pre+"retreating","false"));
            for(String item:p.getProperty(pre+"witnessedEvents","").split(",")){UUID eventId=storedUuid(item);if(eventId!=null)m.witnessedEvents.add(eventId);}
            while(m.witnessedEvents.size()>128)m.witnessedEvents.remove(m.witnessedEvents.iterator().next());
            members.put(id,m);
        }
        Set<String> edges=new HashSet<>();
        for(String key:p.stringPropertyNames())if(key.startsWith("edge.")){
            int i=key.indexOf('.',5);if(i>0)edges.add(key.substring(5,i));
        }
        for(String key:edges){
            String[] pair=key.split("/");if(pair.length!=2)continue;
            UUID from=uuid(pair[0]),to=uuid(pair[1]);if(from==null||to==null)continue;
            SocialEdge e=relationship(from,to);String pre="edge."+key+".";
            e.friendship=Math.clamp((int)num(p.getProperty(pre+"friendship")),-100,100);
            e.trust=bound(p,pre+"trust",50);e.respect=bound(p,pre+"respect",50);
            e.resentment=bound(p,pre+"resentment",0);e.lastInteraction=num(p.getProperty(pre+"time"));
        }
        dirty=importedHistory;
        repairOfficesAfterLoad();
    }
    private void repairOfficesAfterLoad() {
        Set<UUID> playerCaptains=new HashSet<>();
        Set<FlagAnchor> flagClaims=new HashSet<>();
        for(Member m:members.values()) {
            Crew c=crews.get(m.crew);
            if(c==null || m.alive && !c.members.contains(m.id))
                throw new IllegalStateException("Orphaned crew member; original preserved: "+file+" member="+m.id);
        }
        for(Crew c:crews.values()) {
            if(c.flag!=null && !flagClaims.add(c.flag))throw new IllegalStateException("Duplicate flag claim; original preserved: "+file);
            if(c.playerCaptain && c.captain==null)
                throw new IllegalStateException("Missing player captain UUID; original preserved: "+file);
            if(c.captain!=null && !c.playerCaptain) {
                Member captain=members.get(c.captain);
                if(captain==null || !c.members.contains(c.captain) || !captain.adult || !captain.alive)
                    throw new IllegalStateException("Invalid NPC captain; original preserved: "+file);
            }
            if(c.playerCaptain && c.captain!=null && !playerCaptains.add(c.captain))
                throw new IllegalStateException("Ambiguous player captain in multiple crews; original preserved: "+file);
            for(UUID id:c.members) {
                Member m=members.get(id);
                if(m==null || !c.id.equals(m.crew))throw new IllegalStateException("Inconsistent crew roster; original preserved: "+file+" member="+id);
            }
            UUID oldMate=c.firstMate,oldQuarter=c.quartermaster;
            if(!validOfficer(c,c.firstMate))c.firstMate=null;
            if(!validOfficer(c,c.quartermaster))c.quartermaster=null;
            if(!Objects.equals(c.actingCaptain,c.firstMate) || !validOfficer(c,c.actingCaptain))c.actingCaptain=null;
            if(c.firstMate!=null && c.firstMate.equals(c.quartermaster)) {
                if(members.get(c.firstMate).role==Role.QUARTERMASTER)c.firstMate=null;
                else c.quartermaster=null;
            }
            for(UUID id:c.members) {
                Member m=members.get(id);
                Role expected=id.equals(c.captain)&&!c.playerCaptain?Role.CAPTAIN
                    :id.equals(c.firstMate)?Role.FIRST_MATE:id.equals(c.quartermaster)?Role.QUARTERMASTER:Role.CREW;
                if(m.role!=expected){m.role=expected;dirty=true;}
            }
            if(!Objects.equals(oldMate,c.firstMate) || !Objects.equals(oldQuarter,c.quartermaster))
                event(c,"OFFICES_MIGRATED_ALPHA6");
        }
    }
    private boolean validOfficer(Crew c,UUID id) {
        Member m=members.get(id);
        return id!=null && !id.equals(c.captain) && c.members.contains(id) && m!=null && m.adult && m.alive;
    }
    private static int bound(Properties p,String key,int d){try{return Math.clamp(Integer.parseInt(p.getProperty(key)),0,100);}catch(Exception e){return d;}}
    private static long num(String v){try{return Long.parseLong(v);}catch(Exception e){return 0;}}
    private static String str(UUID u){return u==null?"":u.toString();}
    private static UUID uuid(String s){try{return UUID.fromString(s);}catch(Exception e){return null;}}
    private static UUID storedUuid(String s){
        if(s==null || s.isBlank())return null;
        try{return UUID.fromString(s);}catch(IllegalArgumentException invalid){throw new IllegalStateException("Invalid persisted UUID; original preserved: "+s,invalid);}
    }
    private static <E extends Enum<E>> E parse(Class<E> t,String s,E fallback){try{return Enum.valueOf(t,s);}catch(Exception e){return fallback;}}
    private static String join(Collection<UUID> u){StringJoiner j=new StringJoiner(",");for(UUID x:u)j.add(x.toString());return j.toString();}
}
