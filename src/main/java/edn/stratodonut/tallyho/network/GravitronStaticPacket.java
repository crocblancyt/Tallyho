package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.valkyrienskies.clockwork.ClockworkItems;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.CreativeGravitronItem;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.GravitronItem;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.GravitronState;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.tool.GrabTool;

public class GravitronStaticPacket extends SimplePacketBase {
    public GravitronStaticPacket() {}

    public GravitronStaticPacket(FriendlyByteBuf buf) {}

    public void write(FriendlyByteBuf buffer) {}

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null && GravitronState.leftClickItem((Player)serverPlayer, GravitronState.getState((Player)serverPlayer))) {
                ItemStack stack = serverPlayer.getMainHandItem();
                boolean bl = stack.is(ClockworkItems.CREATIVE_GRAVITRON.get());
                boolean bl2 = stack.is(ClockworkItems.GRAVITRON.get());
                if ((bl2 || bl) && !serverPlayer.getCooldowns().isOnCooldown(stack.getItem()))
                    serverPlayer.getCooldowns().addCooldown(stack.getItem(), 20);
                GrabTool.Companion.dropShip((Player)serverPlayer);
            }
        });
        return true;
    }
}