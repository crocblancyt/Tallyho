package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.ILaserDesignator;
import edn.stratodonut.tallyho.item.LaserDesignatorItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class DesignatePacket extends SimplePacketBase {
   boolean active;

   public DesignatePacket() {
   }

   public DesignatePacket(boolean active) {
      this.active = active;
   }

   public DesignatePacket(FriendlyByteBuf buf) {
      this.active = buf.readBoolean();
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.active);
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         Player p = context.getSender();
         if (CameraPossessionController.getPossessingConductor(p) instanceof ILaserDesignator d) {
            d.designate();
         } else if (p != null && p.getUseItem().getItem() instanceof LaserDesignatorItem designatorItem) {
            LaserDesignatorItem.designate(p.getUseItem(), this.active);
         }
      });
      return true;
   }
}
