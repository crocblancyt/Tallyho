package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class SeatAdjustPacket extends SimplePacketBase {
    int seatId;
    boolean goUp;

    public SeatAdjustPacket() {}

    public SeatAdjustPacket(FriendlyByteBuf buf) {
        this.seatId = buf.readInt();
        this.goUp = buf.readBoolean();
    }

    public SeatAdjustPacket(int seat, boolean goUp) {
        this.seatId = seat;
        this.goUp = goUp;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.seatId);
        buffer.writeBoolean(this.goUp);
    }

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Entity e = context.getSender().level().getEntity(this.seatId);
            if (e instanceof com.simibubi.create.content.contraptions.actors.seat.SeatEntity) {
                CreateTypePacketHandler.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> e), new SeatSyncPacket(this));
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