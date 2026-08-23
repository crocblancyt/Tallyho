package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class SeatSyncPacket extends SimplePacketBase {
    int seatId;
    boolean goUp;

    public SeatSyncPacket() {}

    public SeatSyncPacket(FriendlyByteBuf buf) {
        this.seatId = buf.readInt();
        this.goUp = buf.readBoolean();
    }

    public SeatSyncPacket(SeatAdjustPacket packet) {
        this.seatId = packet.seatId;
        this.goUp = packet.goUp;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.seatId);
        buffer.writeBoolean(this.goUp);
    }

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                Entity e = Minecraft.getInstance().level.getEntity(this.seatId);
                if (e instanceof com.simibubi.create.content.contraptions.actors.seat.SeatEntity)
                    adjust(e, this.goUp);
            }
        });
        return true;
    }

    public static boolean adjust(Entity e, boolean up) {
        if (up && e.getY() + 0.1D < e.blockPosition().getY() + 0.7D) {
            e.setPos(e.getX(), e.getY() + 0.1D, e.getZ());
            return true;
        }
        if (!up) {
            e.setPos(e.getX(), e.blockPosition().getY(), e.getZ());
            return true;
        }
        return false;
    }
}