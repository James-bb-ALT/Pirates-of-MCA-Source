package dev.arandu.piratesmca;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.resources.SkinVisualIds;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.*;
public class OutfitGameTests {
 static net.conczin.mca.entity.VillagerEntityMCA fixture(GameTestHelper h,boolean female){
  var n=WearableGameTests.pirate(h,female);n.setClothes("piratesmca:skins/clothing/"+(female?"female":"male")+"/0.png");n.setClothingLocked(true);return n;
 }
 @GameTest(maxTicks=90) public void uniformPreservesIdentityAndRestoresWardrobe(GameTestHelper h){
  var male=fixture(h,false);var female=fixture(h,true);
  h.runAtTickTime(40,()->{for(var n:java.util.List.of(male,female)){
   var clothes=n.getClothes();boolean locked=n.isClothingLocked();var skin=n.getSkin();var hair=n.getHairStyleId();var gender=n.getGenetics().getGender();var name=n.getName().getString();
   WearableGameTests.expect(h,SkinVisualIds.isClothing(PirateOutfits.texture("deckhand"),gender) && PirateOutfits.apply(n,"deckhand"),"OUTFIT-01 native MCA rejected neutral uniform");
   WearableGameTests.expect(h,!PirateOutfits.apply(n,"deckhand") && n.isClothingLocked(),"OUTFIT-02 repeated use changed snapshot");
   WearableGameTests.expect(h,skin.equals(n.getSkin()) && hair.equals(n.getHairStyleId()) && gender==n.getGenetics().getGender() && name.equals(n.getName().getString()),"OUTFIT-02 changed identity");
   WearableGameTests.expect(h,PirateOutfits.restore(n) && clothes.equals(n.getClothes()) && locked==n.isClothingLocked() && !PirateOutfits.restore(n),"OUTFIT-03 restore lost wardrobe or lock");
  }h.succeed();});
 }
 @GameTest(maxTicks=90) public void nativeSaveLoadRetainsUniformAndRestoreBookmark(GameTestHelper h){
  var n=fixture(h,true);
  h.runAtTickTime(40,()->{var old=n.getClothes();boolean locked=n.isClothingLocked();WearableGameTests.expect(h,PirateOutfits.apply(n,"deckhand"),"OUTFIT-04 apply failed");
   var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());n.saveWithoutId(out);
   var copy=EntitiesMCA.FEMALE_VILLAGER.create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.LOAD);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
   WearableGameTests.expect(h,PirateOutfits.texture("deckhand").equals(copy.getClothes()) && PirateOutfits.restore(copy) && old.equals(copy.getClothes()) && copy.isClothingLocked()==locked,"OUTFIT-04 native load lost uniform/bookmark");h.succeed();});
 }
 @GameTest(maxTicks=90) public void repairOnlyMissingAndPreserveExternalWardrobe(GameTestHelper h){
  var n=fixture(h,false);
  h.runAtTickTime(40,()->{var old=n.getClothes();PirateOutfits.apply(n,"deckhand");n.setClothes("");PirateOutfits.repairIfMissing(n);
   WearableGameTests.expect(h,n.getClothes().equals(PirateOutfits.texture("deckhand")),"OUTFIT-05 missing clothing not repaired");
   n.setClothes(old);PirateOutfits.repairIfMissing(n);WearableGameTests.expect(h,old.equals(n.getClothes()) && !PirateOutfits.restore(n),"OUTFIT-05 replaced external valid outfit");h.succeed();});
 }
 @GameTest(maxTicks=90) public void ordinaryVillagersAndChildrenAreExcluded(GameTestHelper h){
  var n=h.spawn(EntitiesMCA.FEMALE_VILLAGER,new BlockPos(1,2,1));n.setAge(0);n.setNoAi(true);
  var child=h.spawn(EntitiesMCA.MALE_VILLAGER,new BlockPos(3,2,1));child.setAge(-24000);child.setNoAi(true);
  h.runAtTickTime(40,()->{child.addTag(PiratesOfMCA.MARKER);WearableGameTests.expect(h,child.isBaby() && !PirateOutfits.apply(n,"deckhand") && !PirateOutfits.apply(child,"deckhand"),"OUTFIT-06 changed ordinary villager or child");h.succeed();});
 }
}
