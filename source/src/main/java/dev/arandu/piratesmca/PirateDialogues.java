package dev.arandu.piratesmca;

import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.*;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.*;
import static dev.arandu.piratesmca.PirateDialogueCatalog.Signal;

/** Server-authoritative conversations, bounded to loaded adult pirates and nearby listeners. */
public final class PirateDialogues {
 private PirateDialogues(){}
 private static final Map<MinecraftServer,State> STATES=new IdentityHashMap<>();
 private static final class Audience {long nextAutomatic;final ArrayDeque<String> events=new ArrayDeque<>();}
 private record Session(UUID token,UUID npc,UUID crew,ServerLevel level,String scene,long expires){}
 private record Visit(UUID npc,ServerLevel level,long expires){}
 private static final class State {final Map<UUID,Audience> audiences=new HashMap<>();final Map<UUID,Session> sessions=new HashMap<>();final Map<UUID,Visit> visits=new HashMap<>();}
 private static State state(ServerLevel level){return STATES.computeIfAbsent(level.getServer(),k->new State());}
 private static ServerLevel level(ServerPlayer p){return (ServerLevel)p.level();}
 private static Component text(String key){return Component.translatable("piratesmca.talk."+key);}
 public static boolean eligible(VillagerEntityMCA n){return n.isAlive()&&CrewRuntime.isPirate(n);}
 static PirateDialogueMemory memory(VillagerEntityMCA n){
  for(String tag:n.entityTags())if(tag.startsWith(PirateDialogueMemory.PREFIX))return PirateDialogueMemory.decode(tag);
  var data=new PirateDialogueMemory();var member=CrewRuntime.ledger((ServerLevel)n.level()).member(n.getUUID());
  String profile=member==null?null:member.profile;
  if(profile==null)for(String p:List.of("friendly","bruiser","suspicious","captain","greedy"))if(n.entityTags().contains("pmca_"+p)){profile=p;break;}
  data.temperament=PirateDialogueCatalog.temperament(profile);return data;
 }
 static void save(VillagerEntityMCA n,PirateDialogueMemory data){
  for(String tag:new ArrayList<>(n.entityTags()))if(tag.startsWith(PirateDialogueMemory.PREFIX))n.removeTag(tag);
  n.addTag(data.encode());
 }
 private static CrewLedger.Crew crew(VillagerEntityMCA n){var ledger=CrewRuntime.ledger((ServerLevel)n.level());var member=ledger.member(n.getUUID());return member==null?null:ledger.crew(member.crew);}
 private static boolean nearby(VillagerEntityMCA n,ServerPlayer p){return eligible(n)&&p.isAlive()&&!p.isSpectator()&&p.level()==n.level()&&p.distanceToSqr(n)<=144&&p.hasLineOfSight(n);}
 private static boolean treasure(ItemStack stack){return stack.is(Items.COMPASS)||stack.is(Items.FILLED_MAP)||stack.is(Items.MAP)||stack.is(Items.GOLD_INGOT)||stack.is(Items.EMERALD)||stack.is(Items.DIAMOND);}
 static Set<Signal> facts(VillagerEntityMCA n,ServerPlayer p){
  var result=EnumSet.noneOf(Signal.class);if(!nearby(n,p))return result;
  var level=(ServerLevel)n.level();var ledger=CrewRuntime.ledger(level);var c=crew(n);var member=ledger.member(n.getUUID());long now=level.getGameTime();var memory=memory(n);
  if(level.getBiome(n.blockPosition()).is(BiomeTags.IS_OCEAN)||level.getBiome(n.blockPosition()).is(BiomeTags.IS_BEACH)||n.getVehicle() instanceof AbstractBoat)result.add(Signal.SEA);
  if(treasure(p.getMainHandItem()))result.add(Signal.TREASURE);
  if(!memory.story.isEmpty()&&now>=memory.storyAt&&now-memory.storyAt<=1200)try{result.add(Signal.valueOf(memory.story));}catch(IllegalArgumentException ignored){}
  if(c!=null){
   if(c.captain!=null)result.add(Signal.CAPTAIN);else result.add(Signal.CAPTAIN_TEST);
   if(c.members.size()>1){result.add(Signal.CREW);result.add(Signal.LOYALTY);}
   if(c.phase==CrewLedger.Phase.CONSPIRACY||c.phase==CrewLedger.Phase.ULTIMATUM||c.phase==CrewLedger.Phase.REBELLION){
    if(ledger.conspirators(c)>0&&member!=null&&member.resentment<65&&!Set.of("REBEL","CONSPIRATOR").contains(member.allegiance)){
     for(UUID id:c.members)if(level.getEntity(id) instanceof VillagerEntityMCA other&&other!=n&&other.isAlive()&&n.distanceToSqr(other)<=144&&n.hasLineOfSight(other)){
      var conspirator=ledger.member(id);if(conspirator!=null&&conspirator.alive&&conspirator.adult&&!id.equals(c.captain)&&conspirator.resentment>=65&&conspirator.fear<80){result.add(Signal.CONSPIRACY);break;}
     }
    }
   }
   // An unloaded member is never interpreted as a missing/dead sailor.
   for(UUID id:c.members)if(level.getEntity(id) instanceof VillagerEntityMCA other&&other!=n&&eligible(other)&&other.isInWater()&&!other.isPassenger()&&n.distanceToSqr(other)>24*24){result.add(Signal.LOST_SAILOR);break;}
   if(member!=null&&member.order==CrewLedger.Order.FOLLOW&&(n.getVehicle() instanceof AbstractBoat||p.getVehicle() instanceof AbstractBoat))result.add(Signal.VOYAGE);
   if(c.flag!=null&&c.flag.dimension().equals(level.dimension().identifier().toString())){
    if(!Objects.equals(c.captain,p.getUUID())&&p.distanceToSqr(c.flag.x()+.5,c.flag.y()+.5,c.flag.z()+.5)<=64)result.add(Signal.TRESPASS);
    for(var rival:ledger.crews())if(rival!=c&&rival.flag!=null&&rival.flag.dimension().equals(c.flag.dimension())&&n.distanceToSqr(rival.flag.x()+.5,rival.flag.y()+.5,rival.flag.z()+.5)<=256){
     var pos=new net.minecraft.core.BlockPos(rival.flag.x(),rival.flag.y(),rival.flag.z());if(level.hasChunkAt(pos)&&level.getBlockState(pos).is(PirateFlag.BLOCK)){result.add(Signal.RIVAL_FLAG);break;}
    }
   }
  }
  AbstractBoat boat=n.getVehicle() instanceof AbstractBoat b?b:p.getVehicle() instanceof AbstractBoat b&&n.distanceToSqr(p)<16?b:null;
  if(boat!=null&&boat.getDamage()>0){result.add(Signal.DAMAGED_HULL);result.add(Signal.DANGER);}
  if(level.isThundering()&&result.contains(Signal.SEA)){result.add(Signal.STORM);result.add(Signal.DANGER);}
  if(!level.getEntitiesOfClass(Monster.class,n.getBoundingBox().inflate(8),m->m.isAlive()&&n.distanceToSqr(m)<=64&&n.hasLineOfSight(m)).isEmpty())result.add(Signal.DANGER);
  var attacker=n.getLastHurtByMob();
  if(CrewRuntime.inBrawl(level,n.getUUID())||n.getHealth()<n.getMaxHealth()*.5F&&attacker!=null&&attacker.isAlive()&&attacker.level()==level&&n.distanceToSqr(attacker)<=144)result.add(Signal.DANGER);
  return result;
 }
 private static void message(VillagerEntityMCA n,ServerPlayer p,String key){p.sendSystemMessage(Component.translatable("piratesmca.talk.speaker",n.getName(),Component.translatable(key)));}
 static boolean speak(VillagerEntityMCA n,ServerPlayer p,String context,long delay){
  if(!nearby(n,p))return false;var data=memory(n);long now=level(p).getGameTime();if(!data.ready(now,delay))return false;
  var line=PirateDialogueCatalog.INSTANCE.choose(data.temperament,context,facts(n,p),data.recent,new java.util.Random(n.getRandom().nextLong()));if(line==null)return false;
  message(n,p,line.translationKey());data.spoken(line.id(),now);save(n,data);return true;
 }
 public static void gift(VillagerEntityMCA n,ServerPlayer p,ItemStack accepted){if(!nearby(n,p))return;if(treasure(accepted))remember(n,Signal.TREASURE);speak(n,p,"gift",20);}
 public static void trade(VillagerEntityMCA n,ServerPlayer p){speak(n,p,"trade",20);}
 private static void remember(VillagerEntityMCA n,Signal signal){var data=memory(n);data.story=signal.name();data.storyAt=((ServerLevel)n.level()).getGameTime();save(n,data);}
 public static void boatDamaged(ServerLevel level,AbstractBoat boat){
  var nearby=level.getEntitiesOfClass(VillagerEntityMCA.class,boat.getBoundingBox().inflate(12),PirateDialogues::eligible);if(nearby.isEmpty())return;
  var crews=new HashSet<UUID>();
  for(var passenger:boat.getPassengers()){
   if(passenger instanceof VillagerEntityMCA n&&eligible(n)){var c=crew(n);if(c!=null)crews.add(c.id);}
   if(passenger instanceof ServerPlayer p){var c=CrewRuntime.playerCrew(level,p);if(c!=null)crews.add(c.id);}
  }
  if(crews.isEmpty())return;
  for(var n:nearby){var c=crew(n);if(c!=null&&crews.contains(c.id))remember(n,Signal.DAMAGED_HULL);}
 }
 private static String eventToken(VillagerEntityMCA n,PirateDialogueCatalog.Scene scene){
  var c=crew(n);Signal signal=PirateDialogueCatalog.required(scene);
  if(signal==Signal.CONSPIRACY&&c!=null){var it=c.socialEvents.descendingIterator();while(it.hasNext()){var e=it.next();if(e.kind.equals("CONSPIRACY_STARTED"))return c.id+"/"+e.id;}return c.id+"/conspiracy";}
  if(signal==Signal.CRATE_LOST||signal==Signal.TREASURE)return signal+"/"+memory(n).storyAt;
  return (c==null?n.getUUID():c.id)+"/"+signal;
 }
 static UUID offer(VillagerEntityMCA n,ServerPlayer p,String id,boolean automatic){
  if(!nearby(n,p))return null;var scene=PirateDialogueCatalog.INSTANCE.scene(id);var data=memory(n);long now=level(p).getGameTime();
  if(!PirateDialogueCatalog.eligible(scene,data.temperament,facts(n,p))||!data.ready(now,automatic?40:100))return null;
  var c=crew(n);var token=UUID.randomUUID();state(level(p)).sessions.put(p.getUUID(),new Session(token,n.getUUID(),c==null?null:c.id,level(p),id,now+600));
  message(n,p,scene.promptTranslationKey());p.sendSystemMessage(text("respond").copy().withStyle(s->s.withClickEvent(new ClickEvent.RunCommand("/pirates talk open "+token))));
  data.spoken(id,now);data.heard(eventToken(n,scene));save(n,data);return token;
 }
 private static VillagerEntityMCA resolve(ServerPlayer p,Session session){
  if(session==null||session.level()!=p.level()||level(p).getGameTime()>session.expires())return null;
  if(!(level(p).getEntity(session.npc()) instanceof VillagerEntityMCA n)||!nearby(n,p))return null;
  var c=crew(n);return Objects.equals(session.crew(),c==null?null:c.id)?n:null;
 }
 private static Session session(ServerPlayer p,UUID token){var s=state(level(p)).sessions.get(p.getUUID());return s!=null&&s.token().equals(token)?s:null;}
 static UUID pendingToken(ServerPlayer p){var s=state(level(p)).sessions.get(p.getUUID());return s==null?null:s.token();}
 private static int expired(ServerPlayer p){p.sendSystemMessage(text("expired"));return 0;}
 private static ActionButton button(Component caption,String command){return new ActionButton(new CommonButtonData(caption,300),Optional.of(new StaticAction(new ClickEvent.RunCommand(command))));}
 private static void show(ServerPlayer p,Component body,List<ActionButton> buttons){
  var close=new ActionButton(new CommonButtonData(text("close"),300),Optional.empty());
  p.openDialog(Holder.direct(new MultiActionDialog(new CommonDialogData(text("topics"),Optional.empty(),true,false,DialogAction.CLOSE,List.of(new PlainMessage(body,300)),List.of()),buttons,Optional.of(close),1)));
 }
 static int open(ServerPlayer p,UUID token){
  var s=session(p,token);var n=resolve(p,s);if(n==null)return expired(p);var scene=PirateDialogueCatalog.INSTANCE.scene(s.scene());
  if(!PirateDialogueCatalog.eligible(scene,memory(n).temperament,facts(n,p)))return expired(p);
  var buttons=new ArrayList<ActionButton>();for(var reply:scene.options())buttons.add(button(Component.translatable(reply.choiceTranslationKey()),"/pirates talk answer "+token+" "+reply.id()));
  show(p,Component.translatable(scene.promptTranslationKey()),buttons);return 1;
 }
 static int answer(ServerPlayer p,UUID token,String choice){
  var s=session(p,token);var n=resolve(p,s);if(n==null)return expired(p);var scene=PirateDialogueCatalog.INSTANCE.scene(s.scene());
  if(!PirateDialogueCatalog.eligible(scene,memory(n).temperament,facts(n,p)))return expired(p);
  var reply=scene.options().stream().filter(o->o.id().equals(choice)).findFirst().orElse(null);if(reply==null)return expired(p);
  state(level(p)).sessions.remove(p.getUUID());message(n,p,reply.replyTranslationKey());
  var data=memory(n);long now=level(p).getGameTime();
  if(data.outcomeReady(now)&&!reply.outcome().equals("neutral")){
   var ledger=CrewRuntime.ledger(level(p));var bond=ledger.relationship(n.getUUID(),p.getUUID());int delta=reply.outcome().equals("positive")?1:-2;
   bond.trust=Math.clamp(bond.trust+delta,0,100);bond.respect=Math.clamp(bond.respect+delta,0,100);bond.lastInteraction=now;ledger.save();data.lastOutcome=now;
  }
  data.spoken(reply.replyTranslationKey(),now);save(n,data);return 1;
 }
 static int topics(VillagerEntityMCA n,ServerPlayer p){
  if(!nearby(n,p))return expired(p);var c=crew(n);UUID token=UUID.randomUUID();var data=memory(n);var facts=facts(n,p);
  state(level(p)).sessions.put(p.getUUID(),new Session(token,n.getUUID(),c==null?null:c.id,level(p),null,level(p).getGameTime()+600));
  var buttons=new ArrayList<ActionButton>();
  for(String context:List.of("smalltalk","sea","treasure","captain","crew","loyalty","rumor","trade","farewell")){
   if(context.equals("sea")&&!facts.contains(Signal.SEA)||context.equals("captain")&&!facts.contains(Signal.CAPTAIN)||context.equals("crew")&&!facts.contains(Signal.CREW))continue;
   buttons.add(button(text(context),"/pirates talk topic "+token+" "+context));
  }
  for(var scene:PirateDialogueCatalog.INSTANCE.scenes)if(PirateDialogueCatalog.eligible(scene,data.temperament,facts))buttons.add(button(Component.translatable("piratesmca.talk.scene."+scene.id()),"/pirates talk scene "+token+" "+scene.id()));
  show(p,n.getName(),buttons);return 1;
 }
 private static int topic(ServerPlayer p,UUID token,String context){
  var s=session(p,token);var n=resolve(p,s);if(n==null||s.scene()!=null||!Set.of("smalltalk","sea","treasure","captain","crew","loyalty","rumor","trade","farewell").contains(context))return expired(p);
  state(level(p)).sessions.remove(p.getUUID());
  if(context.equals("trade")){n.getInteractions().handle(p,"trade");return p.containerMenu instanceof net.minecraft.world.inventory.MerchantMenu?1:0;}
  return speak(n,p,context,100)?1:0;
 }
 static void tick(ServerLevel level,VillagerEntityMCA n){
  if(!eligible(n)||n.isSleeping())return;
  ServerPlayer p=null;var c=crew(n);for(var candidate:level.players())if(nearby(n,candidate)){
   boolean nextCaptain=c!=null&&Objects.equals(c.captain,candidate.getUUID()),currentCaptain=p!=null&&c!=null&&Objects.equals(c.captain,p.getUUID());
   if(p==null||nextCaptain&&!currentCaptain||nextCaptain==currentCaptain&&n.distanceToSqr(candidate)<n.distanceToSqr(p))p=candidate;
  }
  if(p==null)return;var state=state(level);if(resolve(p,state.sessions.get(p.getUUID()))!=null)return;
  var audience=state.audiences.computeIfAbsent(p.getUUID(),k->new Audience());long now=level.getGameTime();
  var facts=facts(n,p);var data=memory(n);
  var warning=PirateDialogueCatalog.INSTANCE.scene("piratesmca_hostile_mutiny_whisper");String warningToken=eventToken(n,warning);
  boolean urgent=facts.contains(Signal.CONSPIRACY)&&!data.events.contains(warningToken)&&!audience.events.contains(warningToken);
  if(now<audience.nextAutomatic&&!urgent)return;
  if(facts.contains(Signal.DAMAGED_HULL)&&(!data.story.equals(Signal.DAMAGED_HULL.name())||now-data.storyAt>1200)){remember(n,Signal.DAMAGED_HULL);data=memory(n);}
  var scenes=new ArrayList<>(PirateDialogueCatalog.INSTANCE.scenes);scenes.sort(Comparator.comparingInt(s->PirateDialogueCatalog.required(s)==Signal.CONSPIRACY?0:1));
  for(var scene:scenes){if(PirateDialogueCatalog.required(scene)==Signal.CAPTAIN_TEST)continue;if(!PirateDialogueCatalog.eligible(scene,data.temperament,facts))continue;
   String event=eventToken(n,scene);if(data.events.contains(event)||audience.events.contains(event))continue;
   if(offer(n,p,scene.id(),true)!=null){audience.nextAutomatic=now+200;audience.events.addLast(event);while(audience.events.size()>8)audience.events.removeFirst();return;}
  }
  if(c!=null){
   var member=CrewRuntime.ledger(level).member(n.getUUID());var events=c.socialEvents.descendingIterator();
   while(events.hasNext()){
    var event=events.next();if(now<event.tick||now-event.tick>200)continue;String token="social/"+event.id;
    if(data.events.contains(token)||audience.events.contains(token))continue;
    String context=switch(event.kind){case "ORDER_GIVEN","ROLE_APPOINTED","SUCCESSION","CAPTAIN_YIELDED","CAPTAIN_RESIGNED"->"captain";case "MURDER","CAPTAIN_ASSAULT","MUTINY_RESOLUTION"->"loyalty";case "BRAWL_ENDED","RESOURCE_DISPUTE"->"crew";default->null;};
    if(context==null)continue;
    boolean publicEvent=Set.of("ROLE_APPOINTED","SUCCESSION","CAPTAIN_YIELDED","CAPTAIN_RESIGNED","MUTINY_RESOLUTION").contains(event.kind);
    if(!publicEvent&&!event.witnesses.contains(n.getUUID())&&!Objects.equals(event.actor,n.getUUID())&&!Objects.equals(event.target,n.getUUID())&&(member==null||!member.witnessedEvents.contains(event.id)))continue;
    if(speak(n,p,context,100)){var spoken=memory(n);spoken.heard(token);save(n,spoken);audience.events.addLast(token);while(audience.events.size()>8)audience.events.removeFirst();audience.nextAutomatic=now+200;return;}
   }
  }
  if(facts.contains(Signal.DANGER)&&speak(n,p,"danger",600)){audience.nextAutomatic=now+200;return;}
  if(data.ready(now,1200)&&n.getRandom().nextInt(10)==0&&speak(n,p,facts.contains(Signal.SEA)?"sea":facts.contains(Signal.CREW)?"crew":"smalltalk",1200))audience.nextAutomatic=now+600;
 }
 public static void initialize(){
  // Fail early on malformed embedded data, before any NPC is allowed to use it.
  Objects.requireNonNull(PirateDialogueCatalog.INSTANCE);
  UseEntityCallback.EVENT.register((player,world,hand,entity,hit)->{
   if(!(player instanceof ServerPlayer p)||hand!=InteractionHand.MAIN_HAND||!(entity instanceof VillagerEntityMCA n)||!nearby(n,p))return InteractionResult.PASS;
   state(level(p)).visits.put(p.getUUID(),new Visit(n.getUUID(),level(p),level(p).getGameTime()+1200));
   if(p.isShiftKeyDown()&&p.getMainHandItem().isEmpty()){topics(n,p);return InteractionResult.SUCCESS;}
   speak(n,p,"greeting",100);return InteractionResult.PASS;
  });
  PlayerBlockBreakEvents.AFTER.register((world,player,pos,block,entity)->{
   if(!(world instanceof ServerLevel level)||!(player instanceof ServerPlayer p)||!(entity instanceof net.minecraft.world.Container))return;
   for(var n:level.getEntitiesOfClass(VillagerEntityMCA.class,p.getBoundingBox().inflate(12))){if(!nearby(n,p))continue;var c=crew(n);
    if(c!=null&&c.flag!=null&&c.flag.dimension().equals(level.dimension().identifier().toString())&&pos.distSqr(new net.minecraft.core.BlockPos(c.flag.x(),c.flag.y(),c.flag.z()))<=144)remember(n,Signal.CRATE_LOST);
   }
  });
  ServerTickEvents.END_SERVER_TICK.register(server->{
   var state=STATES.get(server);if(state==null||server.getTickCount()%20!=0)return;
   for(var it=state.visits.entrySet().iterator();it.hasNext();){var e=it.next();var visit=e.getValue();var p=server.getPlayerList().getPlayer(e.getKey());
    if(p==null||p.level()!=visit.level()||visit.level().getGameTime()>visit.expires()){it.remove();continue;}
    if(!(visit.level().getEntity(visit.npc()) instanceof VillagerEntityMCA n)){it.remove();continue;}
    if(p.distanceToSqr(n)>100){if(nearby(n,p))speak(n,p,"farewell",100);it.remove();}
   }
   state.sessions.entrySet().removeIf(e->e.getValue().level().getGameTime()>e.getValue().expires());
  });
  ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{var s=STATES.get(server);if(s!=null){UUID id=handler.player.getUUID();s.sessions.remove(id);s.audiences.remove(id);s.visits.remove(id);}});
  ServerLifecycleEvents.SERVER_STOPPED.register(STATES::remove);
 }
 static void commands(com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> root){
  var talk=Commands.literal("talk").executes(ctx->{var p=ctx.getSource().getPlayerOrException();var n=CrewRuntime.nearestPirate(level(p),p,12);return n==null?expired(p):topics(n,p);});
  talk.then(Commands.literal("open").then(Commands.argument("token",com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx->{var p=ctx.getSource().getPlayerOrException();try{return open(p,UUID.fromString(com.mojang.brigadier.arguments.StringArgumentType.getString(ctx,"token")));}catch(IllegalArgumentException e){return expired(p);}})));
  for(String action:List.of("answer","topic","scene"))talk.then(Commands.literal(action).then(Commands.argument("token",com.mojang.brigadier.arguments.StringArgumentType.word()).then(Commands.argument("choice",com.mojang.brigadier.arguments.StringArgumentType.word()).executes(ctx->{
   var p=ctx.getSource().getPlayerOrException();try{UUID token=UUID.fromString(com.mojang.brigadier.arguments.StringArgumentType.getString(ctx,"token"));String choice=com.mojang.brigadier.arguments.StringArgumentType.getString(ctx,"choice");
    if(action.equals("answer"))return answer(p,token,choice);if(action.equals("topic"))return topic(p,token,choice);
    var s=session(p,token);var n=resolve(p,s);if(n==null||s.scene()!=null)return expired(p);UUID offered=offer(n,p,choice,false);return offered==null?expired(p):open(p,offered);
   }catch(IllegalArgumentException e){return expired(p);}
  }))));root.then(talk);
 }
}
