package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.missile.ShipStoresManagement;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent.Context;

public class MissileTonePacket extends SimplePacketBase {
   private static ResourceLocation lastSound = null;
   ResourceLocation sound;
   int entityId;
   boolean interrupt;

   public MissileTonePacket() {
   }

   public MissileTonePacket(FriendlyByteBuf buf) {
      this.sound = buf.readResourceLocation();
      this.entityId = buf.readInt();
      this.interrupt = buf.readBoolean();
   }

   public MissileTonePacket(ResourceLocation sound, Integer entityId, boolean interrupt) {
      this.sound = sound;
      this.entityId = entityId;
      this.interrupt = interrupt;
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeResourceLocation(this.sound);
      buffer.writeInt(this.entityId);
      buffer.writeBoolean(this.interrupt);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean handle(Context context) {
      context.enqueueWork(
         () -> {
            if (Minecraft.getInstance().player != null) {
               if (lastSound != null && this.interrupt) {
                  Minecraft.getInstance().getSoundManager().stop(lastSound, SoundSource.BLOCKS);
               }

               lastSound = this.sound;
               Player p = Minecraft.getInstance().player;
               if (p.isPassenger()
                  && Minecraft.getInstance().level != null
                  && (ShipStoresManagement.getInstance() == null || ShipStoresManagement.getInstance().getNextMissileEntityId().orElse(-1) == this.entityId)) {
                  BlockPos pos = p.getVehicle().blockPosition();
                  Minecraft.getInstance().level.playLocalSound(pos, SoundEvent.createVariableRangeEvent(this.sound), SoundSource.BLOCKS, 1.0F, 1.0F, false);
               }
            }
         }
      );
      return true;
   }
}
