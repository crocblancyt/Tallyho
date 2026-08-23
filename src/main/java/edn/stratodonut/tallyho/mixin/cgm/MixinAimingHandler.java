package edn.stratodonut.tallyho.mixin.cgm;

import com.mrcrayfish.guns.client.handler.AimingHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = {AimingHandler.class}, remap = false)
public abstract class MixinAimingHandler {
    @Inject(method = {"isAiming"}, at = {@At("RETURN")}, cancellable = true)
    private void mixinIsAiming(CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player != null &&
            Minecraft.getInstance().player.isFallFlying())
            cir.setReturnValue(false);
    }
}