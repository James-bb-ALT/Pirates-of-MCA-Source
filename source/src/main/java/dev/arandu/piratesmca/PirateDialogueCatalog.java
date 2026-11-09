package dev.arandu.piratesmca;

import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.random.RandomGenerator;

/** Immutable pirate-only catalogue. These files deliberately do not enter MCA's global main dialogue. */
public final class PirateDialogueCatalog {
 public enum Signal { SEA,TREASURE,TRADE,CAPTAIN,CREW,LOYALTY,RUMOR,DANGER,STORM,CONSPIRACY,VOYAGE,LOST_SAILOR,CRATE_LOST,DAMAGED_HULL,TRESPASS,RIVAL_FLAG,CAPTAIN_TEST }
 public record Line(String id,boolean pirateOnly,String temperament,String context,String trigger,int weight,String translationKey){}
 public record Reply(String id,String choiceTranslationKey,String replyTranslationKey,String outcome){}
 public record Scene(String id,String temperament,boolean pirateOnly,String promptTranslationKey,List<Reply> options){}
 private record Pool(List<Line> lines){}
 private record Scenes(List<Scene> scenes){}
 public static final PirateDialogueCatalog INSTANCE=new PirateDialogueCatalog();
 public final List<Line> lines;
 public final List<Scene> scenes;
 private final Map<String,Scene> byId;
 private static <T>T read(String name,Class<T> type){
  try(var in=PirateDialogueCatalog.class.getResourceAsStream("/data/piratesmca/pirate_dialogues/"+name+".json")){
   if(in==null)throw new IllegalStateException("Missing pirate dialogue "+name);
   return new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),type);
  }catch(IOException e){throw new UncheckedIOException(e);}
 }
 private PirateDialogueCatalog(){
  lines=List.copyOf(read("pool",Pool.class).lines());scenes=List.copyOf(read("scenes",Scenes.class).scenes());
  if(lines.size()!=216||scenes.size()!=9)throw new IllegalStateException("Incomplete supplied dialogue pack");
  var ids=new HashSet<String>();var index=new HashMap<String,Scene>();
  for(var line:lines)if(!line.pirateOnly()||!ids.add(line.id())||line.weight()<1||!Set.of("friendly","gruff","hostile").contains(line.temperament()))throw new IllegalStateException("Invalid pirate line "+line.id());
  for(var scene:scenes){if(!scene.pirateOnly()||scene.options().size()!=3||index.put(scene.id(),scene)!=null)throw new IllegalStateException("Invalid pirate scene "+scene.id());required(scene);}
  byId=Map.copyOf(index);
 }
 public Scene scene(String id){return id==null?null:byId.get(id);}
 public static String temperament(String profile){return switch(Objects.toString(profile,"")){case "friendly"->"friendly";case "bruiser","suspicious","hostile"->"hostile";default->"gruff";};}
 public static Signal required(Scene scene){
  String id=scene.id();
  if(id.endsWith("voyage_invitation"))return Signal.VOYAGE;
  if(id.endsWith("lost_sailor"))return Signal.LOST_SAILOR;
  if(id.endsWith("share_the_find"))return Signal.TREASURE;
  if(id.endsWith("missing_crate"))return Signal.CRATE_LOST;
  if(id.endsWith("captain_test"))return Signal.CAPTAIN_TEST;
  if(id.endsWith("damaged_hull"))return Signal.DAMAGED_HULL;
  if(id.endsWith("deck_trespass"))return Signal.TRESPASS;
  if(id.endsWith("mutiny_whisper"))return Signal.CONSPIRACY;
  if(id.endsWith("rival_flag"))return Signal.RIVAL_FLAG;
  throw new IllegalArgumentException("Unrouted pirate scene "+id);
 }
 public static boolean eligible(Scene scene,String temperament,Set<Signal> facts){
  // An actual conspiracy warning is urgent for any loyal pirate; other stories keep their supplied tone.
  return scene!=null&&scene.pirateOnly()&&facts.contains(required(scene))&&(required(scene)==Signal.CONSPIRACY||scene.temperament().equals(temperament));
 }
 public static boolean lineAllowed(Line line,Set<Signal> facts){
  if(!line.pirateOnly())return false;
  if(line.context().equals("sea")&&!facts.contains(Signal.SEA))return false;
  if(line.context().equals("crew")&&!facts.contains(Signal.CREW))return false;
  if(line.context().equals("captain")&&!facts.contains(Signal.CAPTAIN))return false;
  if(line.context().equals("danger")&&!facts.contains(Signal.DANGER))return false;
  if(Set.of("hostile.rumor.01","hostile.rumor.06").contains(line.id())&&!facts.contains(Signal.CONSPIRACY))return false;
  if(line.id().equals("friendly.danger.05"))return facts.contains(Signal.DAMAGED_HULL);
  if(Set.of("friendly.danger.01","friendly.danger.06","gruff.danger.04","gruff.danger.05").contains(line.id()))return facts.contains(Signal.STORM);
  if(Set.of("friendly.danger.03","hostile.danger.04","hostile.danger.06","hostile.rumor.04").contains(line.id()))return facts.contains(Signal.RIVAL_FLAG);
  return true;
 }
 public Line choose(String temperament,String context,Set<Signal> facts,Collection<String> recent,RandomGenerator random){
  var options=lines.stream().filter(l->l.temperament().equals(temperament)&&l.context().equals(context)&&lineAllowed(l,facts)).toList();
  if(options.isEmpty())options=lines.stream().filter(l->l.temperament().equals(temperament)&&l.context().equals("smalltalk")).toList();
  var fresh=options.stream().filter(l->!recent.contains(l.id())).toList();if(!fresh.isEmpty())options=fresh;
  int total=options.stream().mapToInt(Line::weight).sum();if(total==0)return null;
  int roll=random.nextInt(total);for(var line:options){roll-=line.weight();if(roll<0)return line;}throw new AssertionError();
 }
}
