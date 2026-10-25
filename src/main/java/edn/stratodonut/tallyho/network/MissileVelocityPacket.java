package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class MissileVelocityPacket extends SimplePacketBase {
   int missile_id;
   Vec3 vel;

   public MissileVelocityPacket() {
   }

   public MissileVelocityPacket(FriendlyByteBuf buf) {
      this.missile_id = buf.readInt();
      float x = buf.readFloat();
      float y = buf.readFloat();
      float z = buf.readFloat();
      this.vel = new Vec3(x, y, z);
   }

   public MissileVelocityPacket(int missile_id, Vec3 vel) {
      this.missile_id = missile_id;
      this.vel = vel;
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.missile_id);
      buffer.writeFloat((float)this.vel.x);
      buffer.writeFloat((float)this.vel.y);
      buffer.writeFloat((float)this.vel.z);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         if (Minecraft.getInstance().level != null) {
            Entity e = Minecraft.getInstance().level.getEntity(this.missile_id);
            if (e instanceof MountedMissileEntity) {
               e.setDeltaMovement(this.vel);
            }
         }
      });
      return true;
   }
}
