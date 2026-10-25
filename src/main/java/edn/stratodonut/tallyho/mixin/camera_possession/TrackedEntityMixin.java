package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.FlexibleSeatEntity;
import edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = TrackedEntity.class, priority = 1300)
public abstract class TrackedEntityMixin {
   @Shadow
   @Final
   Entity entity;
   @Unique
   private boolean shouldBeSent = false;

   @Inject(
      method = "updatePlayer",
      at = @At(value = "FIELD", target = "Lnet/minecraft/world/phys/Vec3;x:D", ordinal = 0),
      locals = LocalCapture.CAPTURE_FAILSOFT
   )
   private void tyh$onUpdatePlayer(ServerPlayer player, CallbackInfo callback, Vec3 unused, double viewDistance) {
      if (CameraPossessionController.getPossessingConductor(player) instanceof RemoteCameraEntity) {
         Vec3 relativePosToCamera = player.getCamera().position().subtract(this.entity.position());
         if (relativePosToCamera.x >= -viewDistance
            && relativePosToCamera.x <= viewDistance
            && relativePosToCamera.z >= -viewDistance
            && relativePosToCamera.z <= viewDistance) {
            this.shouldBeSent = true;
         }
      }
   }

   @ModifyVariable(method = "updatePlayer", ordinal = 0, at = @At(value = "JUMP", opcode = 153, shift = Shift.BEFORE, ordinal = 1))
   public boolean tyh$modifyFlag(boolean original) {
      boolean shouldBeSent = this.shouldBeSent;
      this.shouldBeSent = false;
      return this.entity instanceof RemoteCameraEntity || original || shouldBeSent;
   }

   @ModifyVariable(method = "updatePlayer(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("STORE"), ordinal = 0)
   private double mixinUpdatePlayer(double r) {
      return !(this.entity instanceof RemoteCameraEntity)
            && !(this.entity instanceof FlexibleSeatEntity seat && seat.getFirstPassenger() instanceof RemoteCameraEntity)
         ? r
         : 4096.0;
   }
}
