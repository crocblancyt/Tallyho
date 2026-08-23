package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.HeliChinTurretEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class RangePacket extends SimplePacketBase {
    int id;
    int range;

    public RangePacket() {}

    public RangePacket(HeliChinTurretEntity e, int range) {
        this.id = e.getId();
        this.range = range;
    }

    public RangePacket(FriendlyByteBuf buf) {
        this.id = buf.readInt();
        this.range = buf.readInt();
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.id);
        friendlyByteBuf.writeInt(this.range);
    }

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                Entity e = context.getSender().level().getEntity(this.id);
                if (e instanceof HeliChinTurretEntity) {
                    HeliChinTurretEntity h = (HeliChinTurretEntity)e;
                    h.setRange(this.range);
                }
            }
        });
        return true;
    }
}