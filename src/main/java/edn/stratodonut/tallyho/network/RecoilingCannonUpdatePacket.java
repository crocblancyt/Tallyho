package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.mixinducks.RecoilingCannonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class RecoilingCannonUpdatePacket extends SimplePacketBase {
   public int entityID;
   public double motion;
   public double target;

   public RecoilingCannonUpdatePacket() {
   }

   public RecoilingCannonUpdatePacket(int entityID, double motion, double sequenceLimit) {
      this.entityID = entityID;
      this.motion = motion;
      this.target = sequenceLimit;
   }

   public RecoilingCannonUpdatePacket(FriendlyByteBuf buffer) {
      this.entityID = buffer.readInt();
      this.motion = buffer.readFloat();
      this.target = buffer.readFloat();
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.entityID);
      buffer.writeFloat((float)this.motion);
      buffer.writeFloat((float)this.target);
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(this)));
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public static void handlePacket(RecoilingCannonUpdatePacket p) {
      if (Minecraft.getInstance().level.getEntity(p.entityID) instanceof RecoilingCannonEntity rce) {
         rce.handlePacket(p);
      }
   }
}
