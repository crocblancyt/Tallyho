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

public class RemoteCameraEntity extends CameraEntity2 {
   private ServerPlayer fakePlayer = null;
   int timeout = 0;
   private int initialChunkLoadingDistance = 0;
   private boolean hasSentChunks = false;

   public RemoteCameraEntity(EntityType<? extends Entity> p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
   }

   public static RemoteCameraEntity spawn(ServerLevel level, BlockPos pos, float base_yaw) {
      RemoteCameraEntity result = new RemoteCameraEntity((EntityType<? extends Entity>)AllEntities.REMOTE_CAMERA.get(), level);
      result.setParams(base_yaw, CameraEntity2.PostFXType.CAMERA, new AngleLimits(70.0F, 70.0F, 70.0F), 50);
      result.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      return result;
   }

   @Override
   public void tick() {
      super.tick();
      SectionPos sectionPos = SectionPos.of(this);
      if (!sectionPos.equals(this.oldSectionPos)) {
         this.setHasSentChunks(false);
      }

      if (this.level() instanceof ServerLevel serverLevel) {
         if (this.fakePlayer == null) {
            this.fakePlayer = new RemoteCameraFakePlayer(serverLevel, this);
         }

         ServerPlayer player = this.currentlyViewing.get();
         if (player != null) {
            SectionPos chunkPos = SectionPos.of(this.blockPosition());
            int viewDistance = player.server.getPlayerList().getViewDistance();

            for (int x = chunkPos.getX() - viewDistance; x <= chunkPos.getX() + viewDistance; x++) {
               for (int z = chunkPos.getZ() - viewDistance; z <= chunkPos.getZ() + viewDistance; z++) {
                  serverLevel.getChunkSource().addRegionTicket(TicketType.FORCED, new ChunkPos(x, z), 3, new ChunkPos(x, z));
               }
            }
         }
      }
   }

   @Override
   public boolean startViewing(ServerPlayer player) {
      if (super.startViewing(player)) {
         int viewDistance = player.server.getPlayerList().getViewDistance();
         this.setChunkLoadingDistance(viewDistance);
         return true;
      } else {
         return false;
      }
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

   public void stopRiding() {
   }

   @Override
   protected boolean isShortLived() {
      return false;
   }
}
