package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class DismountCameraPacket extends SimplePacketBase {
    public DismountCameraPacket() {}

    public DismountCameraPacket(FriendlyByteBuf buf) {}

    public void write(FriendlyByteBuf buffer) {}

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            Entity vehicle = sender.getVehicle();
            if (vehicle instanceof CameraEntity2) {
                CameraEntity2 conductor = (CameraEntity2)vehicle;
                conductor.stopViewing(sender);
            } else {
                CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> sender), new SetCameraViewPacket((Entity)sender));
            }
        });
        return true;
    }
}