package edn.stratodonut.tallyho.mixin.camera_possession;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        value = {ChunkMap.class},
        priority = 1300
)
public abstract class ChunkMapMixin {
    @Shadow
    int viewDistance;

    @Shadow
    protected abstract void updateChunkTracking(ServerPlayer var1, ChunkPos var2, MutableObject<ClientboundLevelChunkWithLightPacket> var3, boolean var4, boolean var5);

    @WrapOperation(
            method = {"getPlayers", "setViewDistance"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getLastSectionPos()Lnet/minecraft/core/SectionPos;"
            )}
    )
    private SectionPos mixinGetCameraSectionPos(ServerPlayer player, Operation<SectionPos> original) {
        return CameraPossessionController.getPossessingConductor(player) instanceof RemoteCameraEntity ? SectionPos.of(player.getCamera()) : player.getLastSectionPos();
    }

    @Inject(
            method = {"move"},
            at = {@At("TAIL")}
    )
    private void securitycraft$trackCameraLoadedChunks(ServerPlayer player, CallbackInfo callback) {
        Entity var4 = player.getCamera();
        if (var4 instanceof RemoteCameraEntity camera) {
            if (!camera.hasSentChunks()) {
                SectionPos oldPos = camera.oldSectionPos;
                SectionPos pos = SectionPos.of(camera);
                camera.oldSectionPos = pos;

                for(int i = pos.x() - this.viewDistance - 1; i <= pos.x() + this.viewDistance + 1; ++i) {
                    for(int j = pos.z() - this.viewDistance - 1; j <= pos.z() + this.viewDistance + 1; ++j) {
                        if (oldPos != null) {
                            this.updateChunkTracking(player, new ChunkPos(i, j), new MutableObject(), ChunkMap.isChunkInRange(i, j, oldPos.x(), oldPos.z(), this.viewDistance), ChunkMap.isChunkInRange(i, j, pos.x(), pos.z(), this.viewDistance));
                        } else if (ChunkMap.isChunkInRange(i, j, pos.x(), pos.z(), this.viewDistance)) {
                            this.updateChunkTracking(player, new ChunkPos(i, j), new MutableObject(), false, true);
                        }
                    }
                }

                camera.setHasSentChunks(true);
            }
        } else if (CameraEntity2.hasRecentlyDismounted(player)) {
            SectionPos pos = player.getLastSectionPos();

            for(int i = pos.x() - this.viewDistance; i <= pos.x() + this.viewDistance; ++i) {
                for(int j = pos.z() - this.viewDistance; j <= pos.z() + this.viewDistance; ++j) {
                    this.updateChunkTracking(player, new ChunkPos(i, j), new MutableObject(), false, true);
                }
            }
        }

    }
}