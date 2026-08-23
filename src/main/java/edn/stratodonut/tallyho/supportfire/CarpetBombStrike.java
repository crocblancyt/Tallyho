package edn.stratodonut.tallyho.supportfire;

import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.GuidanceComponent;
import edn.stratodonut.tallyho.missile.MissileRegistry;
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

public class CarpetBombStrike extends OffmapSupport {
    String missileId;
    int quantity;
    OffmapSupport.DropPattern pattern = OffmapSupport.DropPattern.LINE;
    Vec3 callPos;
    Vec3 startPos;
    Vec3 dropDirection;
    int timeToNext = 60;
    int DELAY_BETWEEN_DROP = 3;
    String code;

    public CarpetBombStrike(String id, int qty) {
        this.missileId = id;
        this.quantity = qty;
    }

    public boolean strike(LivingEntity caller) {
        this.callPos = caller.position();
        this.code = caller.getDisplayName().getString();
        SupportTracker.add(caller.level(), this);
        ((Player) caller).displayClientMessage((Component)Component.literal("Carpet bomb Strike requested. Please designate"), false);
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
        if (entry == null)
            complete(world);
        String code = this.code;
        if (!isDropping()) {
            Optional<LaserPointEntity> targ = LaserPointEntity.find((Level)level,
                AABB.ofSize(this.callPos, 800.0D, 800.0D, 800.0D), code);
            if (targ.isPresent())
                if (this.startPos == null) {
                    this.startPos = targ.get().position();
                } else if (this.dropDirection == null) {
                    if (targ.get().position().distanceToSqr(this.startPos) < 32.0D)
                        return;
                    this.dropDirection = targ.get().position().subtract(this.startPos);
                    this.dropDirection = this.dropDirection.add(0.0D, this.dropDirection.y, 0.0D).normalize();
                    startDrop(world);
                }
        } else {
            if (this.timeToNext > 0) {
                this.timeToNext--;
                return;
            }
            this.timeToNext = this.DELAY_BETWEEN_DROP;
            Vec3 spawnPos = this.startPos.add(0.0D, 300.0D, 0.0D);
            Vec3 strikeTarget = this.startPos;
            Vec3 strikeDirection = strikeTarget.subtract(spawnPos).normalize();
            MountedMissileEntity m = entry.spawn(level, spawnPos, 0.0F);
            GuidanceComponent g = m.getGuidance();
            if (g != null)
                g.setCode(this.code);
            m.launch(spread(strikeDirection, 1.0F, 3.0F), 2.0F);
            this.startPos = this.startPos.add(this.dropDirection.scale(16.0D));
            this.quantity--;
            if (this.quantity == 0)
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
        components.add(Component.literal("Carpet Strike").setStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)));
        components.add(Component.literal(this.missileId + " x" + this.quantity).setStyle(Style.EMPTY
                .withColor(ChatFormatting.WHITE)));
        components.add(Component.literal("On Laze point 1 towards 2, Code is your name").setStyle(Style.EMPTY
                .withColor(ChatFormatting.WHITE)));
        components.add(Component.literal("Time on Target: 3s").setStyle(Style.EMPTY
                .withColor(ChatFormatting.WHITE)));
    }

    public void addToNBT(CompoundTag nbt) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", "Carpet");
        tag.putString("MissileId", this.missileId);
        tag.putInt("Qty", this.quantity);
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