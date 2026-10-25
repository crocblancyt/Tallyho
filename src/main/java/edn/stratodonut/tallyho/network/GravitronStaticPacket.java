package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;
import org.valkyrienskies.clockwork.ClockworkItems;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.CreativeGravitronItem;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.GravitronItem;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.GravitronState;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.tool.GrabTool;

public class GravitronStaticPacket extends SimplePacketBase {
   public GravitronStaticPacket() {
   }

   public GravitronStaticPacket(FriendlyByteBuf buf) {
   }

   public void write(FriendlyByteBuf buffer) {
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         Player player = context.getSender();
         if (player != null && GravitronState.leftClickItem(player, GravitronState.getState(player))) {
            ItemStack stack = player.getMainHandItem();
            boolean bl = stack.is(((CreativeGravitronItem)ClockworkItems.CREATIVE_GRAVITRON.get()).asItem());
            boolean bl2 = stack.is(((GravitronItem)ClockworkItems.GRAVITRON.get()).asItem());
            if ((bl2 || bl) && !player.getCooldowns().isOnCooldown(stack.getItem())) {
               player.getCooldowns().addCooldown(stack.getItem(), 20);
            }

            GrabTool.Companion.dropShip(player);
         }
      });
      return true;
   }
}
