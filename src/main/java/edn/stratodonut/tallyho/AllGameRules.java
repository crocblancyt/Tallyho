package edn.stratodonut.tallyho;

import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.LimitF5Packet;
import edn.stratodonut.tallyho.network.MovementAffectAimPacket;
import edn.stratodonut.tallyho.network.ProneClipGamerulePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.network.PacketDistributor;

public class AllGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> RULE_LIMITF5;
    public static final GameRules.Key<GameRules.BooleanValue> RULE_MOVE_AFFECTS_AIM;
    public static final GameRules.Key<GameRules.BooleanValue> RULE_PRONE_CLIPPING;

    static {
        RULE_LIMITF5 = GameRules.register("limitF5", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true, (server, value) -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers())
                CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new LimitF5Packet(value.get()));
        }));
        RULE_MOVE_AFFECTS_AIM = GameRules.register("movementAffectsAim", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true, (server, value) -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers())
                CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new MovementAffectAimPacket(value.get()));
        }));
        RULE_PRONE_CLIPPING = GameRules.register("proneClipIntoShips", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (server, value) -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers())
                CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new ProneClipGamerulePacket(value.get()));
        }));
    }

    public static void init() {}
}