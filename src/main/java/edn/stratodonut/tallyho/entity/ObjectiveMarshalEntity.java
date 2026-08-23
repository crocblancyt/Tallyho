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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.ritchiesprojectilelib.RitchiesProjectileLib;

public class ObjectiveMarshalEntity extends Entity {
    public ObjectiveMarshalEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public void tick() {
        super.tick();
        Team team = getTeam();
        if (team == null || getName() == null)
            return;
        String name = getName().getString();
        if (name.contains(" ")) {
            remove(Entity.RemovalReason.KILLED);
            return;
        }
        if (!(level()).isClientSide && level().getServer() != null) {
            ServerLevel serverLevel = (ServerLevel)level();
            MinecraftServer server = level().getServer();
            ChunkPos cpos = chunkPosition();
            if (!isRemoved())
                RitchiesProjectileLib.queueForceLoad(serverLevel, cpos.x, cpos.z);
            List<ServerPlayer> players = serverLevel.getPlayers(serverPlayer -> (serverPlayer.position().distanceTo(position()) < 64.0D));
            Optional<CapturePointUtil.CapturePoint.Occupation> occupationStatus = players.stream().filter(serverPlayer -> (!serverPlayer.isSpectator() && !serverPlayer.isCreative() && serverPlayer.getTeam() != null)).map(serverPlayer -> {
                    double dist = serverPlayer.position().distanceTo(position());
                    int value = 1;
                    if (dist < 16.0D)
                        value = 3;
                    return team.equals(serverPlayer.getTeam()) ? new CapturePointUtil.CapturePoint.Occupation(0, value) : new CapturePointUtil.CapturePoint.Occupation(value, 0);
                }).reduce(CapturePointUtil.CapturePoint.Occupation::sum);
            Score captureProgress = CapturePointUtil.getCaptureScore(serverLevel, name);
            boolean locked = getPersistentData().contains("locked");
            if (!locked) {
                if (occupationStatus.isPresent()) {
                    int delta = occupationStatus.get().delta();
                    if (delta != 0 && Math.abs(captureProgress.getScore() + delta) <= 600)
                        captureProgress.add(delta);
                }
                boolean noBleed = getPersistentData().contains("noBleed");
                int absProg = Math.abs(captureProgress.getScore());
                if (!noBleed && absProg >= 600 && this.tickCount % 200 == 0)
                    CapturePointUtil.changeTicketScore(serverLevel, team, (captureProgress.getScore() > 0), -1);
            }
            CustomBossEvent capturePointBossBar = CapturePointUtil.updateCapturePoint(server, name, captureProgress
                    .getScore(), occupationStatus.orElse(CapturePointUtil.CapturePoint.Occupation.EMPTY), locked);
            capturePointBossBar.setPlayers(players);
        }
    }

    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        Level level = level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (getName() != null)
                CapturePointUtil.removeCapturePoint(getName().getString());
        }
    }

    protected void defineSynchedData() {}

    public void readAdditionalSaveData(CompoundTag tag) {}

    public void addAdditionalSaveData(CompoundTag tag) {}

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}