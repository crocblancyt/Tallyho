package edn.stratodonut.tallyho.supportfire;

import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.missile.IWarhead;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import edn.stratodonut.tallyho.missile.warhead.ClusterWarhead;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClusterStrike extends OffmapSupport {
    String missileId;
    ClusterWarhead clusterWarhead;
    public static final Vec3 DOWN = new Vec3(0.0D, -1.0D, 0.0D);
    int timeOnTarget = 100;
    @Nullable
    Vec3 callPos;
    @Nullable
    Vec3 spawnPos;
    @Nullable
    String code;
    Queue<Vec3> munitionsInAir;
    int munitionIndex;

    public static ClusterStrike instant(ClusterWarhead w, Vec3 spawnPos) {
        ClusterStrike strike = new ClusterStrike("");
        strike.spawnPos = spawnPos;
        strike.timeOnTarget = 0;
        strike.clusterWarhead = w;
        return strike;
    }

    public boolean strike(@Nullable LivingEntity caller) {
        if (caller != null) {
            this.callPos = caller.position();
            this.code = caller.getDisplayName().getString();
            SupportTracker.add(caller.level(), this);
            ((Player) caller).displayClientMessage((Component)Component.literal("Cluster Strike requested. Please designate"), false);
        }
        return super.strike(caller);
    }

    public boolean strike(@Nonnull Level level) {
        SupportTracker.add(level, this);
        return true;
    }

    public ClusterStrike(String id) {
        this.munitionIndex = 0;
        this.missileId = id;
    }

    public void tick(Level world) {
        ServerLevel level;
        super.tick(world);
        if (world instanceof ServerLevel) {
            level = (ServerLevel)world;
        } else {
            return;
        }
        if (this.clusterWarhead == null) {
            MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(this.missileId);
            IWarhead iWarhead = entry.warhead;
            if (iWarhead instanceof ClusterWarhead) {
                ClusterWarhead c = (ClusterWarhead)iWarhead;
                this.clusterWarhead = c;
            } else {
                complete(world);
                return;
            }
        }
        if (this.callPos != null && this.spawnPos == null && this.code != null) {
            LaserPointEntity.find((Level)level, AABB.ofSize(this.callPos, 800.0D, 800.0D, 800.0D), this.code)
                .ifPresent(laserPointEntity -> this.spawnPos = laserPointEntity.position().add(0.0D, 300.0D, 0.0D));
        } else if (this.spawnPos != null) {
            if (this.timeOnTarget > 0) {
                this.timeOnTarget--;
                return;
            }
            if (this.munitionsInAir == null) {
                List<Vec3> munitions = new ArrayList<>();
                float side = (float)Math.sqrt(this.clusterWarhead.count());
                float d = this.clusterWarhead.dispersion() / 2.0F / side / 2.0F;
                for (float i = -side / 2.0F; i < side / 2.0F; i++) {
                    for (float j = -side / 2.0F; j < side / 2.0F; j++)
                        munitions.add(new Vec3(this.spawnPos.x + (i * d) + this.random
                                .nextFloat(-5.0F, 5.0F), this.spawnPos.y + this.random
                                .nextFloat(-5.0F, 5.0F), this.spawnPos.z + (j * d) + this.random
                                .nextFloat(-5.0F, 5.0F)));
                }
                Collections.shuffle(munitions);
                this.munitionsInAir = new LinkedList<>(munitions);
            }
            if (this.munitionsInAir.isEmpty()) {
                complete(world);
            } else {
                for (int i = 0; i < 20; i++) {
                    Vec3 pos = this.munitionsInAir.poll();
                    if (pos != null &&
                        pos.y > level.getMinBuildHeight())
                        if (pos.y >= level.getMaxBuildHeight()) {
                            this.munitionsInAir.add(pos.add(0.0D, -this.random.nextDouble(15.0D, 25.0D), 0.0D));
                        } else {
                            BlockHitResult result = level.clip(new ClipContext(pos, pos
                                    .add(0.0D, -this.random.nextDouble(15.0D, 25.0D), 0.0D), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                            pos = result.getLocation();
                            if (result.getType() != HitResult.Type.MISS) {
                                this.clusterWarhead.submunition().detonate((Level)level, pos, DOWN);
                            } else {
                                this.munitionsInAir.add(pos);
                            }
                        }
                }
            }
        }
    }

    public void appendHoverText(List<Component> components) {
        components.add(Component.literal("Cluster Strike").setStyle(Style.EMPTY
                .withColor(ChatFormatting.DARK_RED)));
        components.add(Component.literal(this.missileId).setStyle(Style.EMPTY
                .withColor(ChatFormatting.RED)));
        components.add(Component.literal("On your Laser").setStyle(Style.EMPTY
                .withColor(ChatFormatting.RED)));
        components.add(Component.literal("Time on Target: 5s").setStyle(Style.EMPTY
                .withColor(ChatFormatting.RED)));
    }

    public void addToNBT(CompoundTag nbt) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", "Cluster");
        tag.putString("MissileId", this.missileId);
        nbt.put("StrikeTag", (Tag)tag);
    }
}