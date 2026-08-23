package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class SetCameraViewPacket extends SimplePacketBase {
    private final int id;

    public SetCameraViewPacket(Entity camera) {
        this.id = camera.getId();
    }

    public SetCameraViewPacket(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id);
    }

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level.getEntity(this.id);
            boolean isCamera = entity instanceof edn.stratodonut.tallyho.camera.entity.CameraEntity2;
            if (isCamera || entity instanceof net.minecraft.world.entity.player.Player) {
                mc.setCameraEntity(entity);
                if (isCamera)
                    if (mc.player != null) {
                        mc.player.xxa = 0.0F;
                        mc.player.zza = 0.0F;
                        mc.player.setJumping(false);
                    }
                if (entity instanceof edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity)
                    mc.levelRenderer.allChanged();
            }
        });
        return true;
    }
}