package dev.arandu.piratesmca;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.resources.SkinVisualIds;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;

/** Data-driven registration of the supplied clean-room collection. No crafting recipes. */
public final class PirateAtelier {
 private PirateAtelier(){}
 public record Entry(String id,String kind,String slot,String asset){}
 public record Skin(String name,String title,String id,String gender){}
 public static final Map<Item,Entry> ITEMS=new LinkedHashMap<>();
 public static final List<Item> WEAPONS=new ArrayList<>(),HOOKS=new ArrayList<>();
 public static Skin[] SKINS;
 private static <T>T read(String path,Class<T> type){try(var in=PirateAtelier.class.getResourceAsStream(path)){if(in==null)throw new IllegalStateException(path);return new Gson().fromJson(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),type);}catch(IOException e){throw new java.io.UncheckedIOException(e);}}
 public static void register(){
  var iron=ArmorMaterials.IRON;var materials=new HashMap<String,ArmorMaterial>();
  for(var e:read("/data/piratesmca/atelier/items.json",Entry[].class)){
   var id=Identifier.parse(e.id());var props=new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id));
   if(e.kind().equals("armor")){
    var material=materials.computeIfAbsent(e.asset(),asset->new ArmorMaterial(18,Map.of(ArmorType.HELMET,2,ArmorType.CHESTPLATE,7,ArmorType.LEGGINGS,5,ArmorType.BOOTS,2,ArmorType.BODY,7),9,iron.equipSound(),0.5F,0,iron.repairIngredient(),ResourceKey.create(EquipmentAssets.ROOT_ID,Identifier.parse(asset))));
    props.humanoidArmor(material,ArmorType.valueOf(e.slot().toUpperCase(java.util.Locale.ROOT)));
   }else if(e.kind().equals("weapon")){
    boolean fast=e.id().contains("rapier"),heavy=e.id().contains("falchion");props.sword(ToolMaterial.IRON,fast?2.5F:heavy?4F:3F,fast?-2F:heavy?-2.8F:-2.4F).durability(320);
   }else{props.stacksTo(1);if(e.kind().equals("peg"))props.equippable(EquipmentSlot.FEET);}
   var item=Registry.register(BuiltInRegistries.ITEM,id,new Item(props));ITEMS.put(item,e);if(e.kind().equals("weapon"))WEAPONS.add(item);if(e.kind().equals("hook"))HOOKS.add(item);
  }
  SKINS=read("/data/piratesmca/atelier/skins.json",Skin[].class);
 }
 public static boolean owns(Item item){return ITEMS.containsKey(item);}
 public static void equipWeapon(VillagerEntityMCA n){if(n.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && !WEAPONS.isEmpty())n.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(WEAPONS.get(n.getRandom().nextInt(WEAPONS.size()))));if(n.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() && !HOOKS.isEmpty() && n.getRandom().nextFloat()<.15F)n.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(HOOKS.get(n.getRandom().nextInt(HOOKS.size()))));}
 public static boolean extraSkin(String id){return id!=null && id.startsWith("pirateatelier:textures/skins/");}
 public static void skinTick(VillagerEntityMCA n){
  if(!(n.level() instanceof ServerLevel) || n.isBaby() || n.getProfession()!=PirateProfession.PIRATE || n.entityTags().contains("pmca_atelier_skin_v1"))return;
  if(!SkinVisualIds.isBodySkin(n.getSkin(),n.getGenetics().getGender()))return;
  var choices=Arrays.stream(SKINS).filter(s->SkinVisualIds.isBodySkin(s.id(),n.getGenetics().getGender())).toList();if(choices.isEmpty())return;
  // A stable half of newly adopted pirate looks keep MCA; established saved pirates opt out in setup.
  if(n.getRandom().nextBoolean())n.setSkin(choices.get(n.getRandom().nextInt(choices.size())).id());n.addTag("pmca_atelier_skin_v1");n.removeTag("pmca_atelier_skin_pending");
 }
 public static void dropLoot(VillagerEntityMCA n,net.minecraft.world.damagesource.DamageSource damage){
  if(!(n.level() instanceof ServerLevel level) || !(damage.getEntity() instanceof ServerPlayer) || n.getProfession()!=PirateProfession.PIRATE || n.entityTags().contains("pmca_atelier_looted"))return;
  n.addTag("pmca_atelier_looted");if(!level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_DROPS))return;for(var slot:List.of(EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND,EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){
   var stack=n.getItemBySlot(slot);if(owns(stack.getItem()) && n.getRandom().nextFloat()<(slot==EquipmentSlot.MAINHAND?.5F:.35F))n.spawnAtLocation(level,stack.copy());
  }
 }
}
