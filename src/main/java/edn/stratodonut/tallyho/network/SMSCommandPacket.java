package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class SMSCommandPacket extends SimplePacketBase {
   public int entityID;
   private SMSCommandPacket.CommandType type;

   public SMSCommandPacket() {
   }

   public static SMSCommandPacket Launch(int entityID) {
      return new SMSCommandPacket(entityID, SMSCommandPacket.CommandType.LAUNCH);
   }

   public static SMSCommandPacket ActivateSeeker(int entityID) {
      return new SMSCommandPacket(entityID, SMSCommandPacket.CommandType.SEEKER);
   }

   private SMSCommandPacket(int entityID, SMSCommandPacket.CommandType type) {
      this.entityID = entityID;
      this.type = type;
   }

   public SMSCommandPacket(FriendlyByteBuf buf) {
      this.entityID = buf.readInt();
      this.type = (SMSCommandPacket.CommandType)buf.readEnum(SMSCommandPacket.CommandType.class);
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.entityID);
      buffer.writeEnum(this.type);
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         if (context.getSender().level().getEntity(this.entityID) instanceof MountedMissileEntity missile) {
            switch (this.type) {
               case LAUNCH:
                  missile.launch();
                  break;
               case SEEKER:
                  if (missile.getGuidance() != null) {
                     missile.getGuidance().activateSeeker();
                  }
            }
         }
      });
      return true;
   }

   public enum CommandType {
      LAUNCH(1),
      SEEKER(2);

      public final int val;

      CommandType(int v) {
         this.val = v;
      }
   }
}
