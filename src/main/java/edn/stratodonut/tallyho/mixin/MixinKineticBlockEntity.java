package edn.stratodonut.tallyho.mixin;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {KineticBlockEntity.class}, priority = 10, remap = false)
public abstract class MixinKineticBlockEntity {
    @Shadow
    private int flickerTally;

    @Shadow
    public abstract float getSpeed();

    @Shadow
    public abstract int getFlickerScore();

    @Inject(method = {"getFlickerScore"}, at = {@At("HEAD")}, cancellable = true)
    private void mixinRestoreGetFlicker(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.flickerTally);
    }

    @Inject(method = {"tick"}, at = {@At("RETURN")})
    private void mixinTick(CallbackInfo ci) {
        if (this.flickerTally > 0)
            this.flickerTally = 0;
    }

    @Inject(method = {"onSpeedChanged"}, at = {@At("HEAD")})
    private void mixinSpeedChanged(float previousSpeed, CallbackInfo ci) {
        float currentSpeed = getSpeed();
        if (currentSpeed != previousSpeed)
            this.flickerTally = getFlickerScore() + 1;
    }
}