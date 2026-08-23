package edn.stratodonut.tallyho;

import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.SuppressionPacket;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(modid = "tallyho")
public class ServerSuppressionTracker {
    private static final HashMap<UUID, Float> suppressionAccumulator = new HashMap<>();

    public static void suppress(Player p, float amount) {
        suppressionAccumulator.merge(p.getUUID(), amount, (k, v) -> v + amount);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide) {
            Level level = event.level;
            suppressionAccumulator.replaceAll((uuid, v) -> {
                ServerPlayer p = (ServerPlayer)level.getPlayerByUUID(uuid);
                if (p != null)
                    CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new SuppressionPacket(v));
                return 0.0F;
            });
        }
    }
}