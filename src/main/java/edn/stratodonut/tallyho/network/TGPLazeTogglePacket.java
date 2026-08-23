package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.TargetingPodEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class TGPLazeTogglePacket extends SimplePacketBase {
    public int id;

    public TGPLazeTogglePacket() {}

    public TGPLazeTogglePacket(TargetingPodEntity tgp) {
        this.id = tgp.getId();
    }

    public TGPLazeTogglePacket(FriendlyByteBuf buf) {
        this.id = buf.readInt();
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.id);
    }

    public boolean handle(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = context.getSender();
        if (serverPlayer != null) {
            Entity e = serverPlayer.level().getEntity(this.id);
            if (e instanceof TargetingPodEntity) {
                TargetingPodEntity tgp = (TargetingPodEntity)e;
                tgp.toggleLazeMode();
            }
        }
        return true;
    }
}