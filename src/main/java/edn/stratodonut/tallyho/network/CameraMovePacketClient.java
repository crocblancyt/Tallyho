package edn.stratodonut.tallyho.network;

import com.google.common.primitives.Floats;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import java.util.Collections;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;

public class CameraMovePacketClient extends SimplePacketBase {
   final int id;
   final PosRot packet;

   public CameraMovePacketClient(CameraEntity2 entity, PosRot packet) {
      this.id = entity.getId();
      this.packet = packet;
   }

   public CameraMovePacketClient(FriendlyByteBuf buf) {
      this.id = buf.readVarInt();
      this.packet = PosRot.read(buf);
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.id);
      this.packet.write(buffer);
   }

   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.level.getEntity(this.id) instanceof CameraEntity2 conductor && mc.cameraEntity == conductor) {
            double d0 = this.packet.getX(conductor.getX());
            double d1 = this.packet.getY(conductor.getY());
            double d2 = this.packet.getZ(conductor.getZ());
            conductor.syncPacketPositionCodec(d0, d1, d2);
            conductor.setPos(d0, d1, d2);
            float f = this.packet.getYRot(conductor.getYRot()) * 360.0F / 256.0F;
            float f1 = this.packet.getXRot(conductor.getXRot()) * 360.0F / 256.0F;
            conductor.lerpTo(d0, d1, d2, f, f1, 3, true);
            conductor.setOnGround(this.packet.isOnGround());
         }
      });
      return true;
   }

   private static boolean containsInvalidValues(double x, double y, double z, float yaw, float pitch) {
      return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || !Floats.isFinite(pitch) || !Floats.isFinite(yaw);
   }

   private static double clampHorizontal(double value) {
      return Mth.clamp(value, -3.0E7, 3.0E7);
   }

   private static double clampVertical(double value) {
      return Mth.clamp(value, -2.0E7, 2.0E7);
   }

   private static boolean isPlayerCollidingWithAnythingNew(CameraEntity2 conductor, LevelReader world, AABB box) {
      Iterable<VoxelShape> iterable = world.getCollisions(conductor, conductor.getBoundingBox().deflate(1.0E-5F));
      VoxelShape voxelShape = Shapes.create(box.deflate(1.0E-5F));

      for (VoxelShape voxelShape2 : iterable) {
         if (!Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.AND)) {
            return true;
         }
      }

      return false;
   }

   public static void teleport(ServerPlayer player, CameraEntity2 conductor, double x, double y, double z, float yaw, float pitch) {
      teleport(player, conductor, x, y, z, yaw, pitch, Collections.emptySet(), false);
   }

   public static void teleport(
      ServerPlayer player,
      CameraEntity2 conductor,
      double x,
      double y,
      double z,
      float yaw,
      float pitch,
      Set<RelativeMovement> relativeSet,
      boolean dismountVehicle
   ) {
      if (relativeSet.contains(RelativeMovement.X)) {
         conductor.getX();
      } else {
         double d = 0.0;
      }

      if (relativeSet.contains(RelativeMovement.Y)) {
         conductor.getY();
      } else {
         double e = 0.0;
      }

      if (relativeSet.contains(RelativeMovement.Z)) {
         conductor.getZ();
      } else {
         double f = 0.0;
      }

      if (relativeSet.contains(RelativeMovement.Y_ROT)) {
         conductor.getYRot();
      } else {
         float g = 0.0F;
      }

      if (relativeSet.contains(RelativeMovement.X_ROT)) {
         conductor.getXRot();
      } else {
         float h = 0.0F;
      }

      conductor.absMoveTo(x, y, z, yaw, pitch);
      CreateTypePacketHandler.getChannel()
         .send(PacketDistributor.PLAYER.with(() -> player), new CameraMovePacketClient(conductor, new PosRot(x, y, z, yaw, pitch, conductor.onGround())));
   }
}
