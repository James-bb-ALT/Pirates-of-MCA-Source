package dev.arandu.piratesmca;
import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.conczin.mca.resources.SkinVisualIds;
public class AtelierGameTests {
 @GameTest public void completeRegistryHasRealArmorWeaponsAndAssets(GameTestHelper h){
  WearableGameTests.expect(h,PirateAtelier.ITEMS.size()==4444 && PirateAtelier.WEAPONS.size()==10,"ATELIER-01 missing collection entries");int count=0;
  var pools=PirateSpawnEquipment.pools();for(var pair:PirateAtelier.ITEMS.entrySet())if(pair.getValue().kind().equals("armor")){
   var stack=new ItemStack(pair.getKey());var eq=stack.get(DataComponents.EQUIPPABLE);WearableGameTests.expect(h,eq!=null && eq.assetId().isPresent() && pools.get(eq.slot()).contains(pair.getKey()),"ATELIER-02 invalid armor/spawn pool: "+pair.getValue().id());
   WearableGameTests.expect(h,!stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY).modifiers().isEmpty() && stack.getMaxDamage()>new ItemStack(switch(eq.slot()){case HEAD->Items.IRON_HELMET;case CHEST->Items.IRON_CHESTPLATE;case LEGS->Items.IRON_LEGGINGS;default->Items.IRON_BOOTS;}).getMaxDamage(),"ATELIER-02 cosmetic or weaker durability");count++;
  }WearableGameTests.expect(h,count==4349,"ATELIER-02 incorrect armor count");System.out.println("QA_ATELIER_REGISTRY items=4444 armor=4349 weapons=10");h.succeed();
 }
 @GameTest(maxTicks=100) public void pirateWeaponsReplaceGeneratedVanillaWeapons(GameTestHelper h){
  var n=WearableGameTests.pirate(h,false);n.addTag("pmca_captain");h.runAtTickTime(60,()->{WearableGameTests.expect(h,PirateAtelier.WEAPONS.contains(n.getItemBySlot(EquipmentSlot.MAINHAND).getItem()),"ATELIER-03 generated generic sword");h.succeed();});
 }
 @GameTest public void allPirateSkinsHaveNativeGenderValidations(GameTestHelper h){
  WearableGameTests.expect(h,PirateAtelier.SKINS.length==38,"ATELIER-04 missing skins");for(var s:PirateAtelier.SKINS){var gender=s.gender().equals("female")?net.conczin.mca.entity.ai.relationship.Gender.FEMALE:net.conczin.mca.entity.ai.relationship.Gender.MALE;WearableGameTests.expect(h,SkinVisualIds.isBodySkin(s.id(),gender),"ATELIER-04 native body registry rejected "+s.id());}
  WearableGameTests.expect(h,Arrays.stream(PirateAtelier.SKINS).filter(s->s.gender().equals("female")).count()==19,"ATELIER-04 female family coverage missing");h.succeed();
 }
 @GameTest(maxTicks=100) public void skinsArePirateOnlyAndPersistWithoutReroll(GameTestHelper h){
  var n=WearableGameTests.pirate(h,false);h.runAtTickTime(40,()->{var skin=n.getSkin();var name=n.getName().getString();var gender=n.getGenetics().getGender();for(int i=0;i<100;i++)PirateAtelier.skinTick(n);WearableGameTests.expect(h,skin.equals(n.getSkin()) && name.equals(n.getName().getString()) && gender==n.getGenetics().getGender(),"ATELIER-05 skin or identity rerolled");h.succeed();});
 }
 @GameTest(maxTicks=90) public void fullSetIsSixteenDefenseAndTwoToughness(GameTestHelper h){
  var n=WearableGameTests.pirate(h,false);String base="pirateatelier:captain_armor_pristine_normal_";
  for(var entry:Map.of(EquipmentSlot.HEAD,"helmet",EquipmentSlot.CHEST,"chestplate",EquipmentSlot.LEGS,"leggings",EquipmentSlot.FEET,"boots").entrySet())n.setItemSlot(entry.getKey(),new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(base+entry.getValue()))));
  h.runAtTickTime(40,()->{WearableGameTests.expect(h,n.getArmorValue()==16 && n.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS)==2,"ATELIER-09 armor balance mismatch");h.succeed();});
 }
 @GameTest(maxTicks=90) public void deathLootUsesRealItemsAndCannotDuplicate(GameTestHelper h){
  var n=WearableGameTests.pirate(h,false);var p=h.makeMockServerPlayerInLevel();
  h.runAtTickTime(40,()->{var rule=net.minecraft.world.level.gamerules.GameRules.MOB_DROPS;var rules=h.getLevel().getGameRules();boolean old=rules.get(rule);
   try{
    n.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(PirateAtelier.WEAPONS.getFirst()));rules.set(rule,false,h.getLevel().getServer());PirateAtelier.dropLoot(n,h.getLevel().damageSources().playerAttack(p));
    WearableGameTests.expect(h,h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,n.getBoundingBox().inflate(3)).isEmpty(),"ATELIER-10 ignored disabled mob drops");n.removeTag("pmca_atelier_looted");rules.set(rule,true,h.getLevel().getServer());
    for(long seed=0;;seed++){n.getRandom().setSeed(seed);if(n.getRandom().nextFloat()<.5F){n.getRandom().setSeed(seed);break;}}
    n.die(h.getLevel().damageSources().playerAttack(p));int count=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,n.getBoundingBox().inflate(3)).stream().filter(e->PirateAtelier.owns(e.getItem().getItem())).mapToInt(e->e.getItem().getCount()).sum();
    WearableGameTests.expect(h,count>0 && n.entityTags().contains("pmca_atelier_looted"),"ATELIER-10 pirate death did not produce loot");PirateAtelier.dropLoot(n,h.getLevel().damageSources().playerAttack(p));int again=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,n.getBoundingBox().inflate(3)).stream().filter(e->PirateAtelier.owns(e.getItem().getItem())).mapToInt(e->e.getItem().getCount()).sum();WearableGameTests.expect(h,again==count,"ATELIER-10 duplicate loot");
   }finally{rules.set(rule,old,h.getLevel().getServer());}h.succeed();});
 }

}
