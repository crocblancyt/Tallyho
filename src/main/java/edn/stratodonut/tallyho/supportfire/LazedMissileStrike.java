package edn.stratodonut.tallyho.supportfire;

import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.FuzeComponent;
import edn.stratodonut.tallyho.missile.GuidanceComponent;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import edn.stratodonut.tallyho.missile.fuze.AltitudeFuze;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LazedMissileStrike extends OffmapSupport {
    String missileId;
    int dropDelay = 60;
    Vec3 callPos;
    String code;

    public LazedMissileStrike(String id) {
        this.missileId = id;
    }

    public boolean strike(LivingEntity caller) {
        this.callPos = caller.position();
        this.code = caller.getDisplayName().getString();
        SupportTracker.add(caller.level(), this);
        ((Player) caller).displayClientMessage((Component)Component.literal("Precision Strike requested. Please designate"), false);
        return super.strike(caller);
    }

    public void tick(Level world) {
        ServerLevel level;
        super.tick(world);
        if (world instanceof ServerLevel) {
            level = (ServerLevel)world;
        } else {
            return;
        }
        MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(this.missileId);
        if (entry == null) {
            complete(world);
            return;
        }
        String code = this.code;
        Optional<LaserPointEntity> targ = LaserPointEntity.find((Level)level,
            AABB.ofSize(this.callPos, 800.0D, 800.0D, 800.0D), code);
        if (targ.isPresent()) {
            startDrop(world);
            if (this.dropDelay > 0) {
                this.dropDelay--;
                return;
            }
            Vec3 spawnPos = this.callPos.add(0.0D, 400.0D, 0.0D);
            Vec3 strikeTarget = targ.get().position();
            FuzeComponent fuzeComponent = MissileRegistry.getEntry(this.missileId).getFuze();
            if (fuzeComponent instanceof AltitudeFuze) {
                AltitudeFuze af = (AltitudeFuze)fuzeComponent;
                strikeTarget = strikeTarget.add(0.0D, af.alt - strikeTarget.y, 0.0D);
            }
            Vec3 strikeDirection = strikeTarget.subtract(spawnPos).normalize();
            Vec3 displacement = strikeDirection.add(0.0D, strikeDirection.y, 0.0D);
            MountedMissileEntity m = entry.spawn(level, spawnPos, 0.0F);
            GuidanceComponent g = m.getGuidance();
            if (g != null)
                g.setCode(this.code);
            m.launch(spread(strikeDirection, 1.0F, 3.0F), 4.0F);
            spawnPos = spawnPos.add(displacement.scale(8.0D));
            strikeTarget = strikeTarget.add(displacement.scale(16.0D));
            complete(world);
        }
    }

    public void startDrop(@Nullable Level level) {
        if (level != null && !isDropping() && this.callerUUID != null) {
            Player p = level.getPlayerByUUID(this.callerUUID);
            if (p != null)
                p.displayClientMessage((Component)Component.literal("I see your lazer. Bombs away."), false);
        }
        super.startDrop(level);
    }

    public void complete(@Nullable Level level) {
        super.complete(level);
    }

    public void appendHoverText(List<Component> components) {
        components.add(Component.literal("Precision Strike").setStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)));
        components.add(Component.literal(this.missileId + " x1").setStyle(Style.EMPTY
                .withColor(ChatFormatting.WHITE)));
        components.add(Component.literal("On your Laser hold 3s, Code is your name").setStyle(Style.EMPTY
                .withColor(ChatFormatting.WHITE)));
    }

    public void addToNBT(CompoundTag nbt) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", "Precision");
        tag.putString("MissileId", this.missileId);
        nbt.put("StrikeTag", (Tag)tag);
    }

    public Vec3 spread(Vec3 dir, float vel, float spread) {
        return dir.normalize()
            .add(this.random.nextGaussian() * 0.007499999832361937D * spread, this.random
                .nextGaussian() * 0.007499999832361937D * spread, this.random
                .nextGaussian() * 0.007499999832361937D * spread)
            .scale(vel);
    }
}