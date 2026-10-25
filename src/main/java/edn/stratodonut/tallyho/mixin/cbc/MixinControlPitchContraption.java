package edn.stratodonut.tallyho.mixin.cbc;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;

@Mixin(value = ControlPitchContraption.class, remap = false)
public interface MixinControlPitchContraption {
   @Overwrite
   default void onRecoil(Vec3 vector, AbstractContraptionEntity cannon) {
      if (!cannon.level().isClientSide()) {
         if (vector.length() != 0.0) {
            Ship c = VSGameUtilsKt.getShipObjectManagingPos(cannon.level(), VectorConversionsMCKt.toJOML(cannon.position()));
            if (c instanceof ServerShip s) {
               GameTickForceApplier gt = (GameTickForceApplier)s.getAttachment(GameTickForceApplier.class);
               double mass = s.getInertiaData().getMass();
               double recoilForce = vector.length() * 100000.0;
               Vector3dc recoilVec = s.getTransform().getShipToWorldRotation().transform(VectorConversionsMCKt.toJOML(vector).normalize()).mul(recoilForce);
               Vec3 centeredPos = cannon.getAnchorVec().add(0.5, 0.5, 0.5);
               gt.applyInvariantForceToPos(recoilVec, VectorConversionsMCKt.toJOML(centeredPos).sub(s.getTransform().getPositionInShip()));
            }
         }
      }
   }
}
