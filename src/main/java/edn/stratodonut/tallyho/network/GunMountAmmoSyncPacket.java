package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class GunMountAmmoSyncPacket extends SimplePacketBase {
   int mountId;
   int ammo;

   public GunMountAmmoSyncPacket(int mountId, int ammo) {
      this.mountId = mountId;
      this.ammo = ammo;
   }

   public GunMountAmmoSyncPacket(FriendlyByteBuf buf) {
      this.mountId = buf.readInt();
      this.ammo = buf.readInt();
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.mountId);
      buffer.writeInt(this.ammo);
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.level.getEntity(this.mountId) instanceof GunMountEntity mg) {
            mg.setAmmo(this.ammo);
         }
      });
      return true;
   }
}
