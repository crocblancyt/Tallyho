package edn.stratodonut.tallyho.entity;

import edn.stratodonut.tallyho.objectives.CapturePointUtil;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.ritchiesprojectilelib.RitchiesProjectileLib;

public class ObjectiveMarshalEntity extends Entity {
   public ObjectiveMarshalEntity(EntityType<?> p_19870_, Level p_19871_) {
      super(p_19870_, p_19871_);
   }

   public void tick() {
      super.tick();
      Team team = this.getTeam();
      if (team != null && this.getCustomName() != null) {
         String name = this.getCustomName().getString();
         if (name.contains(" ")) {
            this.remove(RemovalReason.KILLED);
         } else {
            if (!this.level().isClientSide && this.level().getServer() != null) {
               ServerLevel serverLevel = (ServerLevel)this.level();
               MinecraftServer server = this.level().getServer();
               ChunkPos cpos = this.chunkPosition();
               if (!this.isRemoved()) {
                  RitchiesProjectileLib.queueForceLoad(serverLevel, cpos.x, cpos.z);
               }

               List<ServerPlayer> players = serverLevel.getPlayers(
                  serverPlayer -> serverPlayer.position().subtract(this.position()).horizontalDistance() < 64.0
               );
               Optional<CapturePointUtil.CapturePoint.Occupation> occupationStatus = players.stream()
                  .filter(serverPlayer -> !serverPlayer.isSpectator() && !serverPlayer.isCreative() && serverPlayer.getTeam() != null)
                  .map(
                     serverPlayer -> {
                        double dist = serverPlayer.position().subtract(this.position()).horizontalDistance();
                        int value = 1;
                        if (dist < 16.0) {
                           value = 3;
                        }

                        return team.equals(serverPlayer.getTeam())
                           ? new CapturePointUtil.CapturePoint.Occupation(0, value)
                           : new CapturePointUtil.CapturePoint.Occupation(value, 0);
                     }
                  )
                  .reduce(CapturePointUtil.CapturePoint.Occupation::sum);
               Score captureProgress = CapturePointUtil.getCaptureScore(serverLevel, name);
               boolean locked = this.getTags().contains("locked");
               if (!locked) {
                  if (occupationStatus.isPresent()) {
                     int delta = occupationStatus.get().delta();
                     if (delta != 0 && Math.abs(captureProgress.getScore() + delta) <= 600) {
                        captureProgress.add(delta);
                     }
                  }

                  boolean noBleed = this.getTags().contains("noBleed");
                  int absProg = Math.abs(captureProgress.getScore());
                  if (!noBleed && absProg >= 600 && this.tickCount % 200 == 0) {
                     CapturePointUtil.changeTicketScore(serverLevel, team, captureProgress.getScore() > 0, -1);
                  }
               }

               CustomBossEvent capturePointBossBar = CapturePointUtil.updateCapturePoint(
                  server, name, captureProgress.getScore(), occupationStatus.orElse(CapturePointUtil.CapturePoint.Occupation.EMPTY), locked
               );
               capturePointBossBar.setPlayers(players);
            }
         }
      }
   }

   public void remove(RemovalReason p_146834_) {
      super.remove(p_146834_);
      if (this.level() instanceof ServerLevel serverLevel && this.getCustomName() != null) {
         CapturePointUtil.removeCapturePoint(this.getCustomName().getString());
      }
   }

   protected void defineSynchedData() {
   }

   public void readAdditionalSaveData(CompoundTag p_20052_) {
   }

   public void addAdditionalSaveData(CompoundTag p_20139_) {
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }
}
