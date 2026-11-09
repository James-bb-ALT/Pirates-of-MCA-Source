package dev.arandu.piratesmca;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;
import static dev.arandu.piratesmca.PirateDialogueCatalog.Signal;

class DialogueLogicTest {
 private final PirateDialogueCatalog catalog=PirateDialogueCatalog.INSTANCE;
 @Test void allSuppliedLinesHaveBothLanguagesAndNoRawKeys()throws Exception{
  var keys=new HashSet<String>();for(var l:catalog.lines){assertTrue(l.pirateOnly());keys.add(l.translationKey());}
  for(var s:catalog.scenes){assertTrue(s.pirateOnly());assertEquals(3,s.options().size());keys.add(s.promptTranslationKey());for(var o:s.options()){assertTrue(Set.of("positive","neutral","negative").contains(o.outcome()));keys.add(o.choiceTranslationKey());keys.add(o.replyTranslationKey());}}
  assertEquals(279,keys.size());
  for(String locale:List.of("en_us","pt_br"))try(var stream=getClass().getResourceAsStream("/assets/piratesmca/lang/"+locale+".json")){
   assertNotNull(stream);var lang=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
   for(String key:keys){assertTrue(lang.has(key),locale+"/"+key);String text=lang.get(key).getAsString();assertFalse(text.isBlank());assertFalse(text.contains("\uFFFD"),key);}
   for(var scene:catalog.scenes)assertTrue(lang.has("piratesmca.talk.scene."+scene.id()),"Untranslated scene button "+locale+"/"+scene.id());
  }
 }
 @Test void everyToneContextHasSixVariations(){
  assertEquals(216,catalog.lines.size());assertEquals(9,catalog.scenes.size());
  var counts=new HashMap<String,Integer>();for(var l:catalog.lines)counts.merge(l.temperament()+"/"+l.context(),1,Integer::sum);
  assertEquals(36,counts.size());assertTrue(counts.values().stream().allMatch(n->n==6));
 }
 @Test void lastFourLinesAreAvoidedAndMemorySurvivesSaving(){
  var m=new PirateDialogueMemory();m.temperament="friendly";var random=new Random(16);
  for(int i=0;i<100;i++){var l=catalog.choose(m.temperament,"smalltalk",Set.of(),m.recent,random);assertFalse(m.recent.contains(l.id()));m.spoken(l.id(),i*100);m=PirateDialogueMemory.decode(m.encode());assertEquals("friendly",m.temperament);assertTrue(m.recent.size()<=4);}
 }
 @Test void everySceneRequiresItsRealSignal(){
  for(var s:catalog.scenes){assertFalse(PirateDialogueCatalog.eligible(s,s.temperament(),Set.of()));assertTrue(PirateDialogueCatalog.eligible(s,s.temperament(),Set.of(PirateDialogueCatalog.required(s))));
   if(PirateDialogueCatalog.required(s)!=Signal.CONSPIRACY)assertFalse(PirateDialogueCatalog.eligible(s,s.temperament().equals("friendly")?"hostile":"friendly",Set.of(PirateDialogueCatalog.required(s))));}
  assertNull(catalog.scene(null));assertNull(catalog.scene("unknown"));assertFalse(PirateDialogueCatalog.eligible(null,"friendly",Set.of()));
 }
 @Test void conspiracyWarningsAreNeverIdleGossip(){
  var wanted=Set.of("hostile.rumor.01","hostile.rumor.06");
  for(var l:catalog.lines)if(wanted.contains(l.id())){assertFalse(PirateDialogueCatalog.lineAllowed(l,Set.of()));assertTrue(PirateDialogueCatalog.lineAllowed(l,Set.of(Signal.CONSPIRACY)));}
  var scene=catalog.scene("piratesmca_hostile_mutiny_whisper");assertTrue(PirateDialogueCatalog.eligible(scene,"friendly",Set.of(Signal.CONSPIRACY)));assertFalse(PirateDialogueCatalog.eligible(scene,"hostile",Set.of(Signal.CREW)));
 }
 @Test void stormsHullDamageAndEnemyShipsHaveSeparateCues(){
  var storm=catalog.lines.stream().filter(l->l.id().equals("friendly.danger.01")).findFirst().orElseThrow();
  var hull=catalog.lines.stream().filter(l->l.id().equals("friendly.danger.05")).findFirst().orElseThrow();
  var rival=catalog.lines.stream().filter(l->l.id().equals("hostile.danger.04")).findFirst().orElseThrow();
  for(var l:List.of(storm,hull,rival))assertFalse(PirateDialogueCatalog.lineAllowed(l,Set.of(Signal.DANGER)));
  assertTrue(PirateDialogueCatalog.lineAllowed(storm,Set.of(Signal.DANGER,Signal.STORM)));
  assertTrue(PirateDialogueCatalog.lineAllowed(hull,Set.of(Signal.DANGER,Signal.DAMAGED_HULL)));
  assertTrue(PirateDialogueCatalog.lineAllowed(rival,Set.of(Signal.DANGER,Signal.RIVAL_FLAG)));
 }
 @Test void cooldownsAndOutcomesCannotBeFarmedByReplaying(){
  var m=new PirateDialogueMemory();m.spoken("hello",1000);m.lastOutcome=1000;m=PirateDialogueMemory.decode(m.encode());
  assertFalse(m.ready(1099,100));assertTrue(m.ready(1100,100));assertFalse(m.outcomeReady(2199));assertTrue(m.outcomeReady(2200));assertTrue(m.ready(900,100));
 }
 @Test void historyAndEventMemoryStayBoundedAndCorruptDataIsSafe(){
  var m=new PirateDialogueMemory();for(int i=0;i<100;i++){m.spoken("line"+i,i);m.heard("event"+i);}m=PirateDialogueMemory.decode(m.encode());assertEquals(4,m.recent.size());assertEquals(4,m.events.size());assertTrue(m.events.contains("event99"));assertFalse(m.events.contains("event0"));
  assertTrue(PirateDialogueMemory.decode("pmca_dialogue_v1:bad_base64{}!").recent.isEmpty());assertTrue(PirateDialogueMemory.decode(null).recent.isEmpty());
 }
 @Test void existingProfilesMapWithoutRerollingAppearance(){
  assertEquals("friendly",PirateDialogueCatalog.temperament("friendly"));assertEquals("hostile",PirateDialogueCatalog.temperament("bruiser"));assertEquals("hostile",PirateDialogueCatalog.temperament("suspicious"));assertEquals("gruff",PirateDialogueCatalog.temperament("captain"));assertEquals("gruff",PirateDialogueCatalog.temperament("greedy"));assertEquals("gruff",PirateDialogueCatalog.temperament(null));
 }
}
