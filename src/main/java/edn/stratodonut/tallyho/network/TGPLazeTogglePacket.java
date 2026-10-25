package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.TargetingPodEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class TGPLazeTogglePacket extends SimplePacketBase {
   public int id;

   public TGPLazeTogglePacket() {
   }

   public TGPLazeTogglePacket(TargetingPodEntity tgp) {
      this.id = tgp.getId();
   }

   public TGPLazeTogglePacket(FriendlyByteBuf buf) {
      this.id = buf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlyByteBuf) {
      friendlyByteBuf.writeVarInt(this.id);
   }

   public boolean handle(Context context) {
      ServerPlayer serverPlayer = context.getSender();
      if (serverPlayer != null && serverPlayer.level().getEntity(this.id) instanceof TargetingPodEntity tgp) {
         tgp.toggleLazeMode();
      }

      return true;
   }
}
