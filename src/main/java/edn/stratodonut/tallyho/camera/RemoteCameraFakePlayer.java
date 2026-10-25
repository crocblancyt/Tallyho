package edn.stratodonut.tallyho.camera;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

public class RemoteCameraFakePlayer extends FakePlayer {
   private static final Connection NETWORK_MANAGER = new Connection(PacketFlow.CLIENTBOUND);
   public static final GameProfile FAKE_PLAYER_PROFILE = new GameProfile(UUID.fromString("B0FADEE5-4411-3475-ADD0-C4EA7E30D050"), "[Conductor]");

   public RemoteCameraFakePlayer(ServerLevel level, @Nullable RemoteCameraEntity cam) {
      super(level, FAKE_PLAYER_PROFILE);
      this.connection = new RemoteCameraFakePlayer.CameraNetHandler(level.getServer(), this);
   }

   @NotNull
   public OptionalInt openMenu(MenuProvider container) {
      return OptionalInt.empty();
   }

   @NotNull
   public Component getDisplayName() {
      return Components.translatable("tallyho.camera_name");
   }

   public float getEyeHeight(@NotNull Pose pose) {
      return 0.0F;
   }

   public Vec3 position() {
      return new Vec3(this.getX(), this.getY(), this.getZ());
   }

   public float getCurrentItemAttackStrengthDelay() {
      return 0.015625F;
   }

   public boolean canEat(boolean ignoreHunger) {
      return false;
   }

   @NotNull
   public ItemStack eat(@NotNull Level world, ItemStack stack) {
      stack.shrink(1);
      return stack;
   }

   private static class CameraNetHandler extends ServerGamePacketListenerImpl {
      public CameraNetHandler(MinecraftServer server, ServerPlayer player) {
         super(server, RemoteCameraFakePlayer.NETWORK_MANAGER, player);
      }

      public void send(@NotNull Packet<?> packet) {
      }

      public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
      }
   }
}
