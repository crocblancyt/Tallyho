package edn.stratodonut.tallyho.mixin;

import com.codinglitch.simpleradio.core.central.Frequency;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin({Frequency.class})
public abstract class MixinFrequency {
    @Shadow
    public static String FREQUENCY_PATTERN;

    @Inject(method = {"check"}, at = {@At("HEAD")}, cancellable = true, remap = false)
    private static void mixinCheck(String frequency, CallbackInfoReturnable<Boolean> cir) {
        if (frequency == null || FREQUENCY_PATTERN == null)
            cir.setReturnValue(Boolean.valueOf(false));
    }
}
