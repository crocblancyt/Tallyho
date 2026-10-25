package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
   @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
   private void railways$turn(double yaw, double pitch, CallbackInfo ci) {
      if ((Entity) (Object) this instanceof Player this$player) {
         CameraEntity2 cameraEnt;
         if ((cameraEnt = CameraPossessionController.getPossessingConductor(this$player)) != null) {
            ci.cancel();
            cameraEnt.turnView(yaw, pitch);
         } else if (this$player.getVehicle() instanceof GunMountEntity) {
         }
      }
   }

   @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void railways$stopPushing(Entity entity, CallbackInfo ci) {
      if (CameraPossessionController.getPossessingConductor((Entity) (Object) this) == entity) {
         ci.cancel();
      }
   }
}
