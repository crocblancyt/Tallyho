package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.IStabilisedTurret;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;

public class StabiliserSyncPacket extends SimplePacketBase {
   final int id;
   final Vec3 stab;

   public StabiliserSyncPacket(CameraEntity2 entity, @Nonnull Vec3 stab) {
      this.id = entity.getId();
      this.stab = stab;
   }

   public StabiliserSyncPacket(FriendlyByteBuf buf) {
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
         ServerPlayer serverPlayer = context.getSender();
         if (serverPlayer != null) {
            Entity e = serverPlayer.level().getEntity(this.id);
            if (e instanceof IStabilisedTurret t && e instanceof CameraEntity2 ce && ce.isPossessed()) {
               t.syncStab(this.stab);
               CreateTypePacketHandler.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> e), new StabiliserSyncPacketClient(ce, this.stab));
            }
         }
      });
      return true;
   }
}
