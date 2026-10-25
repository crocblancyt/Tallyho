package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.IStabilisedTurret;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

public class StabiliserSyncPacketClient extends SimplePacketBase {
   final int id;
   final Vec3 stab;

   public StabiliserSyncPacketClient(CameraEntity2 entity, @Nonnull Vec3 stab) {
      this.id = entity.getId();
      this.stab = stab;
   }

   public StabiliserSyncPacketClient(FriendlyByteBuf buf) {
      this.id = buf.readVarInt();
      this.stab = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeVarInt(this.id);
      buf.writeDouble(this.stab.x);
      buf.writeDouble(this.stab.y);
      buf.writeDouble(this.stab.z);
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            Entity e = mc.level.getEntity(this.id);
            if (e instanceof IStabilisedTurret t && mc.cameraEntity != e) {
               t.syncStab(this.stab);
            }
         }
      });
      return true;
   }
}
