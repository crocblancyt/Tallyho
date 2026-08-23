package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.RemoteCameraFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;

public class RemoteCameraEntity extends CameraEntity2 {

    private ServerPlayer fakePlayer = null;
    int timeout;
    private int initialChunkLoadingDistance;
    private boolean hasSentChunks;

    public RemoteCameraEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.timeout = 0;
        this.initialChunkLoadingDistance = 0;
        this.hasSentChunks = false;
    }

    @Override
    protected void defineSynchedData() {

    }

    public static RemoteCameraEntity spawn(ServerLevel level, BlockPos pos, float baseYaw) {
        RemoteCameraEntity result = new RemoteCameraEntity((EntityType<? extends Entity>) AllEntities.REMOTE_CAMERA.get(), level);
        result.setParams(baseYaw, CameraEntity2.PostFXType.CAMERA, new AngleLimits(70.0F, 70.0F, 70.0F), 50);
        result.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        level.addFreshEntity(result);
        FlexibleSeatEntity.sitDown(level, pos, result);
        result.setYRot(baseYaw);
        return result;
    }

    @Override
    public void tick() {
        super.tick();

        SectionPos sectionPos = SectionPos.of(this);
        if (!sectionPos.equals(this.oldSectionPos)) {
            setHasSentChunks(false);
        }

        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;

            if (this.fakePlayer == null) {
                this.fakePlayer = new RemoteCameraFakePlayer(serverLevel, this);
            }

            ServerPlayer viewingPlayer = (ServerPlayer) this.currentlyViewing.get();
            if (viewingPlayer instanceof ServerPlayer) {
                ServerPlayer player = viewingPlayer;
                SectionPos chunkPos = SectionPos.of(this.blockPosition());
                int viewDistance = player.getServer().getPlayerList().getViewDistance();

                for (int x = chunkPos.getX() - viewDistance; x <= chunkPos.getX() + viewDistance; x++) {
                    for (int z = chunkPos.getZ() - viewDistance; z <= chunkPos.getZ() + viewDistance; z++) {
                        serverLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, new ChunkPos(x, z), 3, new ChunkPos(x, z));
                    }
                }
            }
        }
    }

    @Override
    public boolean startViewing(ServerPlayer player) {
        if (super.startViewing(player)) {
            int viewDistance = player.getServer().getPlayerList().getViewDistance();
            setChunkLoadingDistance(viewDistance);
            return true;
        }
        return false;
    }

    @Override
    public void stopViewing(ServerPlayer player) {
        super.stopViewing(player);
        RECENTLY_DISMOUNTED_PLAYERS.add(player);
    }

    public void setChunkLoadingDistance(int chunkLoadingDistance) {
        this.initialChunkLoadingDistance = chunkLoadingDistance;
    }

    public boolean hasSentChunks() {
        return this.hasSentChunks;
    }

    public void setHasSentChunks(boolean hasSentChunks) {
        this.hasSentChunks = hasSentChunks;
    }

    @Override
    public void onAddedToWorld() {}

    @Override
    protected boolean isShortLived() {
        return false;
    }
}