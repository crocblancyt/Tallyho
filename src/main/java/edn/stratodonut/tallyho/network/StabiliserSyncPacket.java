package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.IStabilisedTurret;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class StabiliserSyncPacket extends SimplePacketBase {
    final int id;
    final Vec3 stab;

    public StabiliserSyncPacket(CameraEntity2 entity, @Nonnull Vec3 stab) {
        this.id = entity.getId();
        this.stab = stab;
    }

    public StabiliserSyncPacket(FriendlyByteBuf buf) {
        this.id = buf.readInt();
        this.stab = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.id);
        buf.writeDouble(this.stab.x);
        buf.writeDouble(this.stab.y);
        buf.writeDouble(this.stab.z);
    }

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null) {
                Entity e = serverPlayer.level().getEntity(this.id);
                if (e instanceof IStabilisedTurret) {
                    IStabilisedTurret t = (IStabilisedTurret)e;
                    if (e instanceof CameraEntity2) {
                        CameraEntity2 ce = (CameraEntity2)e;
                        if (ce.isPossessed()) {
                            t.syncStab(this.stab);
                            CreateTypePacketHandler.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> e), new StabiliserSyncPacketClient(ce, this.stab));
                        }
                    }
                }
            }
        });
        return true;
    }
}