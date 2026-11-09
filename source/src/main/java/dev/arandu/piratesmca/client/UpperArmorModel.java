package dev.arandu.piratesmca.client;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
/** Bone-local partial mesh; native EquipmentLayerRenderer retains trims, dyes and enchantment glint. */
public final class UpperArmorModel extends Model<HumanoidRenderState> {
 public UpperArmorModel(ModelPart source,float cut){super(new ModelPart(UpperLimbGeometry.clipped(source,cut,0),Map.of()),RenderTypes::armorCutoutNoCull);}
 @Override public void setupAnim(HumanoidRenderState state){}
}
