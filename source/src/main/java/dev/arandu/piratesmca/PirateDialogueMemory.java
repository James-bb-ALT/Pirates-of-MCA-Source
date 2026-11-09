package dev.arandu.piratesmca;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bounded entity-tag payload: native entity saving owns persistence, without changing crew schemas. */
public final class PirateDialogueMemory {
 public static final String PREFIX="pmca_dialogue_v1:";
 public String temperament="gruff",story="";
 public long lastSpeech=-120000,lastOutcome=-120000,storyAt=-120000;
 public final ArrayDeque<String> recent=new ArrayDeque<>(),events=new ArrayDeque<>();
 public boolean ready(long now,long delay){return now<lastSpeech||now-lastSpeech>=delay;}
 public void spoken(String key,long now){lastSpeech=now;recent.remove(key);recent.addLast(key);while(recent.size()>4)recent.removeFirst();}
 public void heard(String token){events.remove(token);events.addLast(token);while(events.size()>4)events.removeFirst();}
 public boolean outcomeReady(long now){return now<lastOutcome||now-lastOutcome>=1200;}
 public String encode(){var data=Map.of("tone",temperament,"last",lastSpeech,"outcome",lastOutcome,"story",story,"storyAt",storyAt,"recent",List.copyOf(recent),"events",List.copyOf(events));return PREFIX+Base64.getUrlEncoder().withoutPadding().encodeToString(new Gson().toJson(data).getBytes(StandardCharsets.UTF_8));}
 public static PirateDialogueMemory decode(String tag){
  var result=new PirateDialogueMemory();
  try{
   if(tag==null||!tag.startsWith(PREFIX)||tag.length()>2048)return result;
   var json=com.google.gson.JsonParser.parseString(new String(Base64.getUrlDecoder().decode(tag.substring(PREFIX.length())),StandardCharsets.UTF_8)).getAsJsonObject();
   String tone=json.get("tone").getAsString();if(!Set.of("friendly","gruff","hostile").contains(tone))return result;
   result.temperament=tone;result.lastSpeech=json.get("last").getAsLong();result.lastOutcome=json.get("outcome").getAsLong();result.story=json.get("story").getAsString();result.storyAt=json.get("storyAt").getAsLong();
   for(var x:json.getAsJsonArray("recent")){String key=x.getAsString();if(key.length()<=100){result.recent.addLast(key);while(result.recent.size()>4)result.recent.removeFirst();}}
   for(var x:json.getAsJsonArray("events")){String key=x.getAsString();if(key.length()<=150){result.events.addLast(key);while(result.events.size()>4)result.events.removeFirst();}}
   return result;
  }catch(RuntimeException invalid){return new PirateDialogueMemory();}
 }
}
