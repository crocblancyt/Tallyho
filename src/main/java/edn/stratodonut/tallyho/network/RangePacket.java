package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.HeliChinTurretEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class RangePacket extends SimplePacketBase {
   int id;
   int range;

   public RangePacket() {
   }

   public RangePacket(HeliChinTurretEntity e, int range) {
      this.id = e.getId();
      this.range = range;
   }

   public RangePacket(FriendlyByteBuf buf) {
      this.id = buf.readVarInt();
      this.range = buf.readInt();
   }

   public void write(FriendlyByteBuf friendlyByteBuf) {
      friendlyByteBuf.writeVarInt(this.id);
      friendlyByteBuf.writeInt(this.range);
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         if (context.getSender() != null && context.getSender().level().getEntity(this.id) instanceof HeliChinTurretEntity h) {
            h.setRange(this.range);
         }
      });
      return true;
   }
}
