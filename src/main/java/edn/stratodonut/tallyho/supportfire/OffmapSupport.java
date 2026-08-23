package edn.stratodonut.tallyho.supportfire;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class OffmapSupport {
    Random random = new Random();

    @Nullable
    UUID callerUUID = null;

    private boolean dropping = false;
    private boolean complete = false;

    public enum DropPattern {
        CIRCLE, LINE;
    }

    public boolean strike(LivingEntity caller) {
        if (caller instanceof Player) {
            Player p = (Player)caller;
            this.callerUUID = p.getUUID();
        }
        return true;
    }

    public void tick(Level world) {}

    public boolean isComplete() {
        return this.complete;
    }

    public boolean isDropping() {
        return this.dropping;
    }

    public void complete(@Nullable Level level) {
        this.complete = true;
    }

    public void startDrop(@Nullable Level level) {
        this.dropping = true;
    }

    public abstract void appendHoverText(List<Component> components);

    @Nullable
    public static OffmapSupport readFromNBT(CompoundTag nbt) {
        if (nbt == null) return null;
        
        if (nbt.contains("StrikeTag")) {
            CompoundTag strikeTag = nbt.getCompound("StrikeTag");
            if (strikeTag.contains("Type")) {
                String type = strikeTag.getString("Type");
                switch (type) {
                    case "Carpet":
                        return new CarpetBombStrike(strikeTag.getString("MissileId"), strikeTag.getInt("Qty"));
                    case "Precision":
                        return new LazedMissileStrike(strikeTag.getString("MissileId"));
                    case "Cluster":
                        return new ClusterStrike(strikeTag.getString("MissileId"));
                    default:
                        return null;
                }
            }
        }
        return null;
    }

    public abstract void addToNBT(CompoundTag nbt);
}