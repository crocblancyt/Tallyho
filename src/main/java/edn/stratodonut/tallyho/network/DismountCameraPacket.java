package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;

public class DismountCameraPacket extends SimplePacketBase {
   public DismountCameraPacket() {
   }

   public DismountCameraPacket(FriendlyByteBuf buf) {
   }

   public void write(FriendlyByteBuf buffer) {
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         ServerPlayer sender = context.getSender();
         if (sender.getCamera() instanceof CameraEntity2 conductor) {
            conductor.stopViewing(sender);
         } else {
            CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> sender), new SetCameraViewPacket(sender));
         }
      });
      return true;
   }
}
