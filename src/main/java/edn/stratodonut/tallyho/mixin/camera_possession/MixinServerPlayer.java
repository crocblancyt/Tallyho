package edn.stratodonut.tallyho.mixin.camera_possession;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, priority = 1300)
public abstract class MixinServerPlayer {
   @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;absMoveTo(DDDFF)V"))
   private void tyh$tick(ServerPlayer player, double x, double y, double z, float yaw, float pitch, Operation<Void> op) {
      if (!CameraPossessionController.isPossessingConductor(player)) {
         op.call(new Object[]{player, x, y, z, yaw, pitch});
      }
   }

   @Inject(method = "setCamera", at = @At("HEAD"), cancellable = true)
   private void tyh$snr$snr$setCamera(Entity entityToSpectate, CallbackInfo ci) {
      if (entityToSpectate instanceof CameraEntity2) {
         ci.cancel();
      }
   }
}
