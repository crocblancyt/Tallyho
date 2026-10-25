package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.AllSounds;
import edn.stratodonut.tallyho.client.SuppressionTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class SuppressionPacket extends SimplePacketBase {
   float factor;

   public SuppressionPacket() {
   }

   public SuppressionPacket(float factor) {
      this.factor = factor;
   }

   public SuppressionPacket(FriendlyByteBuf buf) {
      this.factor = buf.readFloat();
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeFloat(this.factor);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean handle(Context context) {
      context.enqueueWork(
         () -> {
            SuppressionTracker.get().suppress(this.factor);
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
               player.playSound(
                  (SoundEvent)AllSounds.BULLET_WHIZZ.get(), Math.min(this.factor / 5.0F, 1.0F), 0.7F + (player.getRandom().nextFloat() - 0.5F) / 2.0F
               );
            }
         }
      );
      return true;
   }
}
