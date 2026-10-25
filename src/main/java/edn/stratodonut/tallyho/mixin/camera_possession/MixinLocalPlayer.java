package edn.stratodonut.tallyho.mixin.camera_possession;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {
   @WrapOperation(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isControlledCamera()Z"))
   private boolean railways$isControlledCamera(LocalPlayer instance, Operation<Integer> original) {
      return instance.isLocalPlayer() || CameraPossessionController.isPossessingConductor(instance);
   }
}
