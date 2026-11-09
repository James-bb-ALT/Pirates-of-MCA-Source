package dev.arandu.piratesmca.client;

import com.google.gson.Gson;
import java.io.*;
import java.util.*;
import net.minecraft.resources.Identifier;

/** Scoped materials keyed by saved body-skin ID. No entity data or genetics are changed. */
public final class PirateSkinLayers {
 public record Profile(String skin,String gender,String body,String face,String blink,String hair){}
 private static final Map<String,Profile> PROFILES=load();
 private PirateSkinLayers(){}
 private static Map<String,Profile> load(){
  try(var stream=PirateSkinLayers.class.getResourceAsStream("/data/piratesmca/atelier/skin_layers.json")){
   if(stream==null)throw new IllegalStateException("Missing pirate skin layer catalog");
   var out=new HashMap<String,Profile>();
   for(var p:new Gson().fromJson(new InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8),Profile[].class))
    if(out.put(p.skin(),p)!=null)throw new IllegalStateException("Duplicate pirate skin "+p.skin());
   return Map.copyOf(out);
  }catch(IOException e){throw new UncheckedIOException(e);}
 }
 public static Profile get(String skin){return PROFILES.get(skin);}
 public static Identifier body(String skin){var p=get(skin);return Identifier.parse(p==null?skin:p.body());}
}
