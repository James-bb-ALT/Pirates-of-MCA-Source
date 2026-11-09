package dev.arandu.piratesmca.testmixin;

import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.conczin.mca.client.render.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=VillagerEntityModelMCA.class,remap=false)
public abstract class VisualModelProbeMixin {
 @Inject(method="setupAnim(Lnet/conczin/mca/client/render/VillagerRenderState;)V",at=@At("TAIL"),remap=false)
 private void probe(VillagerRenderState s,CallbackInfo ci){
  if(System.getProperty("piratesmca.visualFixReview")==null||((int)s.ageInTicks)%40!=0)return;
  var m=(VillagerEntityModelMCA)(Object)this;
  System.out.println("QA_VISIBLE_MODEL tick="+s.ageInTicks+" model="+System.identityHashCode(m)+" root="+m.root().getClass().getSimpleName()+" left="+m.leftArm.storePose()+" right="+m.rightArm.storePose()+" leg="+m.leftLeg.storePose()+" visible="+m.leftLeg.visible+","+m.rightLeg.visible+" skin="+net.conczin.mca.client.render.VillagerVisuals.require(s).skin());
 }
}
