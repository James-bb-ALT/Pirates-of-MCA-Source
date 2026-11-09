package dev.arandu.piratesmca.testmixin;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fabric's saved default-options snapshot predates late mod key registrations.
 * Test-only: leave those keys unbound instead of crashing getKey(null) during reset.
 * Never included in the production JAR. Standard attack/use keys retain their defaults.
 */
@Mixin(InputConstants.class)
public abstract class LateTestKeyMappingMixin {
 @Inject(method="getKey(Ljava/lang/String;)Lcom/mojang/blaze3d/platform/InputConstants$Key;",at=@At("HEAD"),cancellable=true)
 private static void piratesmca$lateTestKey(String name,CallbackInfoReturnable<InputConstants.Key> cir){
  if(name==null&&Boolean.getBoolean("piratesmca.weaponHandheldReview")){
   System.out.println("WEAPON_TEST_LATE_KEY default snapshot missing late key; left unbound in test only");
   cir.setReturnValue(InputConstants.UNKNOWN);
  }
 }
}
