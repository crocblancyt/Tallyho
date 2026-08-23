package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.FlexibleSeatEntity;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = {ChunkMap.TrackedEntity.class}, priority = 1300)
public abstract class TrackedEntityMixin {
    @Shadow
    @Final
    Entity entity;

    @Unique
    private boolean shouldBeSent = false;

    @Inject(method = {"updatePlayer"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/world/phys/Vec3;x:D", ordinal = 0)}, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tyh$onUpdatePlayer(ServerPlayer player, CallbackInfo callback, Vec3 unused, double viewDistance) {
        if (CameraPossessionController.getPossessingConductor(player) instanceof edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity) {
            Vec3 relativePosToCamera = player.getVehicle().position().subtract(this.entity.position());
            if (relativePosToCamera.x >= -viewDistance && relativePosToCamera.x <= viewDistance && relativePosToCamera.z >= -viewDistance && relativePosToCamera.z <= viewDistance)
                this.shouldBeSent = true;
        }
    }

    @ModifyVariable(method = {"updatePlayer"}, ordinal = 0, at = @At(value = "JUMP", opcode = 153, shift = At.Shift.BEFORE, ordinal = 1))
    public boolean tyh$modifyFlag(boolean original) {
        boolean shouldBeSent = this.shouldBeSent;
        this.shouldBeSent = false;
        return (this.entity instanceof edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity || original || shouldBeSent);
    }

    @ModifyVariable(method = {"updatePlayer(Lnet/minecraft/server/level/ServerPlayer;)V"}, at = @At("STORE"), ordinal = 0)
    private double mixinUpdatePlayer(double r) {
        if (!(this.entity instanceof edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity)) {
            Entity entity = this.entity;
            if (entity instanceof FlexibleSeatEntity) {
                FlexibleSeatEntity seat = (FlexibleSeatEntity)entity;
                if (seat.getFirstPassenger() instanceof edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity)
                    return 4096.0D;
            }
        } else {
            return 4096.0D;
        }
        return r;
    }
}