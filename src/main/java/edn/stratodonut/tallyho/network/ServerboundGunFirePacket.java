package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.ITurret;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class ServerboundGunFirePacket extends SimplePacketBase {
   long secret;

   public ServerboundGunFirePacket(long secret) {
      this.secret = secret;
   }

   public ServerboundGunFirePacket(FriendlyByteBuf buf) {
      this.secret = buf.readLong();
   }

   public void write(FriendlyByteBuf friendlyByteBuf) {
      friendlyByteBuf.writeLong(this.secret);
   }

   public boolean handle(Context context) {
      ServerPlayer serverPlayer = context.getSender();
      if (serverPlayer != null) {
         context.enqueueWork(() -> {
            if (CameraPossessionController.getPossessingConductor(serverPlayer) instanceof ITurret fm) {
               fm.handleShoot(serverPlayer);
            }

            if (serverPlayer.getVehicle() instanceof GunMountEntity fm) {
               fm.handleShoot(serverPlayer, this.secret);
            }
         });
      }

      return true;
   }
}
