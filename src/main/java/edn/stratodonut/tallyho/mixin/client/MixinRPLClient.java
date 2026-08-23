package edn.stratodonut.tallyho.mixin.client;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.ritchiesprojectilelib.RPLClient;
import rbasamoyai.ritchiesprojectilelib.effects.CameraModifier;

@Mixin({RPLClient.class})
public abstract class MixinRPLClient {
    @Inject(method = {"onCameraSetup"}, at = {@At("HEAD")}, cancellable = true, remap = false)
    private static void modifyRPLShake(Camera camera, float partialTicks, CameraModifier modifier, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Boolean.valueOf(false));
    }
}
