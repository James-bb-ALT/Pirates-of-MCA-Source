package dev.arandu.piratesmca;
import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

/** Spawn-only loadout. Actual item registry + datapack opt-in, not a fixed list of armor names. */
public final class PirateSpawnEquipment {
 private PirateSpawnEquipment(){}
 public static final String DONE="pmca_spawn_equipment_v1", CLOTHES="pmca_spawn_clothes_pending";
 public static final List<EquipmentSlot> SLOTS=List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET);
 public static final TagKey<Item> EXTRA=TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath(PiratesOfMCA.ID,"spawn_equipment"));
 public static Map<EquipmentSlot,List<Item>> pools(){
  var pools=new EnumMap<EquipmentSlot,List<Item>>(EquipmentSlot.class);for(var slot:SLOTS)pools.put(slot,new ArrayList<>());
  for(var item:BuiltInRegistries.ITEM){
   var stack=new ItemStack(item);var eq=stack.get(DataComponents.EQUIPPABLE);
   if(eq==null || !pools.containsKey(eq.slot()))continue;
   if(BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(PiratesOfMCA.ID) || stack.is(EXTRA))pools.get(eq.slot()).add(item);
  }
  for(var list:pools.values())list.sort(Comparator.comparing(i->BuiltInRegistries.ITEM.getKey(i).toString()));return pools;
 }
 public static void equipOnce(VillagerEntityMCA n){
  if(n.isBaby() || !n.entityTags().contains(PiratesOfMCA.MARKER) || n.entityTags().contains(DONE) || n.entityTags().contains(PiratesOfMCA.READY))return;
  var pools=pools();
  for(var slot:SLOTS){
   if(!n.getItemBySlot(slot).isEmpty())continue; // Explicit command/manual equipment is authoritative.
   var choices=pools.get(slot);
   if(!choices.isEmpty() && n.getRandom().nextBoolean())n.setItemSlot(slot,new ItemStack(choices.get(n.getRandom().nextInt(choices.size()))));
  }
  n.addTag(DONE);
 }
 static void queueDefaultClothes(VillagerEntityMCA n){
  if(n.isClothingLocked())return;
  for(String id:PirateOutfits.IDS)if(n.entityTags().contains("pmca_look_"+id))return;
  n.addTag(CLOTHES);
 }
 static void finishDefaultClothes(VillagerEntityMCA n){
  if(!n.entityTags().contains(CLOTHES))return;
  var list=net.conczin.mca.resources.ClothingList.getInstance();if(list==null)return;
  var valid=list.clothing.keySet().stream().filter(id->id.startsWith(PiratesOfMCA.ID+":"))
   .filter(id->net.conczin.mca.resources.SkinVisualIds.isClothing(id,n.getGenetics().getGender())).sorted().toList();
  if(valid.isEmpty())return;
  String selected=valid.get(n.getRandom().nextInt(valid.size()));
  // Reuse reversible presets when this is a named overlay; legacy pirate clothes
  // retain the existing persistent outfit-index repair path.
  boolean named=false;
  for(String id:PirateOutfits.IDS)if(selected.equals(PirateOutfits.texture(id))){
   if(!PirateOutfits.apply(n,id) && !selected.equals(n.getClothes()))return;named=true;break;
  }
  if(!named){n.setClothes(selected);n.setClothingLocked(true);}
  n.removeTag(CLOTHES);
 }
}
