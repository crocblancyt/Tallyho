package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.TargetingPodEntity;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

public class TGPChangeModePacketClient extends SimplePacketBase {
   public int id;
   public int mode;
   public Vec3 stab;

   public TGPChangeModePacketClient() {
   }

   public TGPChangeModePacketClient(TargetingPodEntity tgp, TargetingPodEntity.TGP_MODE mode, @Nonnull Vec3 stab) {
      this.id = tgp.getId();
      this.mode = mode.id();
      this.stab = stab;
   }

   public TGPChangeModePacketClient(FriendlyByteBuf buf) {
      this.id = buf.readVarInt();
      this.mode = buf.readInt();
      this.stab = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeVarInt(this.id);
      buf.writeInt(this.mode);
      buf.writeDouble(this.stab.x);
      buf.writeDouble(this.stab.y);
      buf.writeDouble(this.stab.z);
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.level.getEntity(this.id) instanceof TargetingPodEntity tgp) {
            tgp.handle(this);
         }
      });
      return true;
   }
}
