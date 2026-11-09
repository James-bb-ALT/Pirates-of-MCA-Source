package dev.arandu.piratesmca;
import java.util.*;
import net.conczin.mca.entity.*;
import net.conczin.mca.entity.ai.brain.tasks.EquipmentTask;
import net.conczin.mca.registry.EntitiesMCA;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.*;
public class SpawnEquipmentGameTests {
 @GameTest(maxTicks=100) public void newPiratesUseIndependentSlotsAndPirateClothes(GameTestHelper h){
  var npcs=new ArrayList<VillagerEntityMCA>();
  for(int i=0;i<48;i++){var n=WearableGameTests.pirate(h,i%2==0);n.addTag("pmca_friendly");n.getRandom().setSeed(0x9E3779B97F4A7C15L*i);PirateSpawnEquipment.equipOnce(n);npcs.add(n);}
  h.runAtTickTime(60,()->{int bareHead=0,hat=0,bareChest=0,sash=0;var pools=PirateSpawnEquipment.pools();
   for(var n:npcs){WearableGameTests.expect(h,n.getClothes().startsWith("piratesmca:") && net.conczin.mca.resources.SkinVisualIds.isClothing(n.getClothes(),n.getGenetics().getGender()),"SPAWN-EQ-01 missing default pirate clothes");
    for(var slot:PirateSpawnEquipment.SLOTS){var stack=n.getItemBySlot(slot);WearableGameTests.expect(h,stack.isEmpty() || pools.get(slot).contains(stack.getItem()),"SPAWN-EQ-02 non-pirate guard equipment");}
    if(n.getItemBySlot(EquipmentSlot.HEAD).isEmpty())bareHead++;else hat++;
    if(n.getItemBySlot(EquipmentSlot.CHEST).isEmpty())bareChest++;else sash++;
   }
   WearableGameTests.expect(h,bareHead>0 && hat>0 && bareChest>0 && sash>0,"SPAWN-EQ-02 independent empty/item choices missing");
   System.out.println("QA_SPAWN_EQUIPMENT count=48 bare_head="+bareHead+" head_item="+hat+" bare_chest="+bareChest+" chest_item="+sash+" leg_feet_from_registry=true");h.succeed();
  });
 }
 @GameTest(maxTicks=100) public void spawnRollPersistsAndDoesNotRefillEmptySlots(GameTestHelper h){
  var n=WearableGameTests.pirate(h,false);
  h.runAtTickTime(40,()->{n.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);n.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));PirateSpawnEquipment.equipOnce(n);
   var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());n.saveWithoutId(out);
   var copy=EntitiesMCA.MALE_VILLAGER.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.LOAD);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));PirateSpawnEquipment.equipOnce(copy);
   WearableGameTests.expect(h,copy.entityTags().contains(PirateSpawnEquipment.DONE) && copy.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && copy.getItemBySlot(EquipmentSlot.CHEST).is(Items.DIAMOND_CHESTPLATE),"SPAWN-EQ-03 load/reroll lost manual loadout");h.succeed();});
 }
 @GameTest public void oldPiratesAndOrdinaryVillagersNeverRerolled(GameTestHelper h){
  var old=WearableGameTests.pirate(h,false);old.addTag(PiratesOfMCA.READY);old.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));PirateSpawnEquipment.equipOnce(old);
  WearableGameTests.expect(h,old.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET) && !old.entityTags().contains(PirateSpawnEquipment.DONE),"SPAWN-EQ-04 old world modified");
  var ordinary=h.spawn(EntitiesMCA.FEMALE_VILLAGER,new BlockPos(3,2,1));ordinary.setAge(0);PirateSpawnEquipment.equipOnce(ordinary);WearableGameTests.expect(h,!ordinary.entityTags().contains(PirateSpawnEquipment.DONE),"SPAWN-EQ-04 ordinary villager changed");h.succeed();
 }
 @GameTest public void guardEquipmentTaskIsSuppressedOnlyForPirates(GameTestHelper h){
  var n=WearableGameTests.pirate(h,false);var ordinary=h.spawn(EntitiesMCA.FEMALE_VILLAGER,new BlockPos(3,2,1));ordinary.setAge(0);int[] calls={0};
  var task=new EquipmentTask(v->{calls[0]++;return true;},v->EquipmentSet.GUARD_0);
  try{var m=EquipmentTask.class.getDeclaredMethod("checkExtraStartConditions",net.minecraft.server.level.ServerLevel.class,VillagerEntityMCA.class);m.setAccessible(true);
   WearableGameTests.expect(h,Boolean.FALSE.equals(m.invoke(task,h.getLevel(),n)) && calls[0]==0,"SPAWN-EQ-05 MCA guard task not intercepted");m.invoke(task,h.getLevel(),ordinary);WearableGameTests.expect(h,calls[0]==1,"SPAWN-EQ-05 changed ordinary guard task");
  }catch(ReflectiveOperationException e){throw new AssertionError(e);}h.succeed();
 }
}
