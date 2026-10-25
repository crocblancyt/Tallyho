package edn.stratodonut.tallyho.mixin.cw;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import edn.stratodonut.tallyho.mixinducks.MixinBearingControllerDuck;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.valkyrienskies.clockwork.content.contraptions.phys.bearing.data.PhysBearingData;
import org.valkyrienskies.clockwork.content.forces.contraption.BearingController;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

@Mixin(value = BearingController.class, remap = false)
public abstract class MixinBearingController implements MixinBearingControllerDuck {
   @Unique
   private boolean clockworkdev$isFacingNegativeDirection = false;
   @Unique
   @JsonIgnore
   private boolean tyh$isFacingNegativeDirection = false;

   @ModifyConstant(
      method = "computeUnlockedRotationalForce(Lorg/valkyrienskies/clockwork/content/contraptions/phys/bearing/data/PhysBearingData;Lorg/valkyrienskies/core/impl/game/ships/PhysShipImpl;Lorg/valkyrienskies/core/impl/game/ships/PhysShipImpl;)Lorg/joml/Vector3dc;",
      constant = @Constant(doubleValue = 10.0)
   )
   double mixinTick(double constant) {
      return 50.0;
   }

   @Inject(
      method = "computeLockedRotationalForce(Lorg/valkyrienskies/clockwork/content/contraptions/phys/bearing/data/PhysBearingData;Lorg/valkyrienskies/core/impl/game/ships/PhysShipImpl;Lorg/valkyrienskies/core/impl/game/ships/PhysShipImpl;)Lorg/joml/Vector3dc;",
      slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/lang/Math;signum(D)D")),
      at = @At("RETURN"),
      locals = LocalCapture.CAPTURE_FAILSOFT,
      cancellable = true
   )
   void mixinTick2(
      PhysBearingData data,
      PhysShipImpl physShip,
      PhysShipImpl otherPhysShip,
      CallbackInfoReturnable<Vector3dc> cir,
      Vector3d bearingAxisInGlobal,
      Vector3d v1,
      double torqueMassMultiplier,
      Vector3dc perpendicularAxis,
      Vector3dc bearingAxisAfterRot,
      Vector3dc perpAfterRot,
      Vector3dc perpAfterRotInPlane,
      double angleBTShipInRadians,
      Vector3dc v2,
      double angleWRespectToBearingAxis,
      double angleErr,
      Vector3dc relativeOmegaInPhysShip,
      double relativeOmegaInPhysShipParallelBearingAxis,
      double omegaErr,
      double torque
   ) {
      if (data.getBearingRPM() == 0.0F) {
         cir.setReturnValue(bearingAxisInGlobal.mul(angleErr * torqueMassMultiplier * 10.0 + omegaErr * torqueMassMultiplier * 50.0, new Vector3d()));
      } else {
         cir.setReturnValue(bearingAxisInGlobal.mul(torque, new Vector3d()));
      }
   }

   @ModifyExpressionValue(
      method = "computeLockedRotationalForce",
      at = @At(value = "INVOKE", target = "Lorg/valkyrienskies/clockwork/content/contraptions/phys/bearing/data/PhysBearingData;getBearingAngle()D"),
      remap = false
   )
   private double fixLockedMode(double original, @Local(argsOnly = true) PhysBearingData data) {
      return !this.clockworkdev$isFacingNegativeDirection
            || data.getBearingAxis().x() != -1.0 && data.getBearingAxis().y() != -1.0 && data.getBearingAxis().z() != -1.0
         ? original
         : -original;
   }

   @Override
   public void tyh$updateIsFacingNegativeDirection(boolean bl) {
      this.clockworkdev$isFacingNegativeDirection = bl;
   }
}
