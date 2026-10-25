package edn.stratodonut.tallyho;

import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.LimitF5Packet;
import edn.stratodonut.tallyho.network.MovementAffectAimPacket;
import edn.stratodonut.tallyho.network.ProneClipGamerulePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.GameRules.Key;
import net.minecraftforge.network.PacketDistributor;

public class AllGameRules {
   public static final Key<BooleanValue> RULE_LIMITF5 = GameRules.register("limitF5", Category.PLAYER, BooleanValue.create(true, (server, value) -> {
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
         CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new LimitF5Packet(value.get()));
      }
   }));
   public static final Key<BooleanValue> RULE_MOVE_AFFECTS_AIM = GameRules.register(
      "movementAffectsAim", Category.PLAYER, BooleanValue.create(true, (server, value) -> {
         for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new MovementAffectAimPacket(value.get()));
         }
      })
   );
   public static final Key<BooleanValue> RULE_PRONE_CLIPPING = GameRules.register(
      "proneClipIntoShips", Category.PLAYER, BooleanValue.create(false, (server, value) -> {
         for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new ProneClipGamerulePacket(value.get()));
         }
      })
   );

   public static void init() {
   }
}
