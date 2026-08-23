package edn.stratodonut.tallyho.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {RotationPropagator.class}, remap = false)
public abstract class MixinRotationPropagator {
    @ModifyConstant(method = {"propagateNewSource(Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;)V"}, constant = {@Constant(intValue = 128)})
    private static int mixinPropagateNewSource(int i) {
      return 768;
    }

    @Inject(method = {"propagateNewSource"}, at = {@At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;getFlickerScore()I")})
    private static void stopPrecisionError(KineticBlockEntity currentTE, CallbackInfo ci, @Local(ordinal = 1) float speedOfNeighbour, @Local(ordinal = 2) LocalFloatRef newSpeed) {
        if (Math.abs(speedOfNeighbour - newSpeed.get()) < 0.00390625D)
            newSpeed.set(speedOfNeighbour);
    }
}
