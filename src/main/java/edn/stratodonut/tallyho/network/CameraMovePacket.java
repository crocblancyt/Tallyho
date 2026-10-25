package edn.stratodonut.tallyho.network;

import com.google.common.primitives.Floats;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import java.util.Collections;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;

public class CameraMovePacket extends SimplePacketBase {
   final int id;
   final PosRot packet;

   public CameraMovePacket(CameraEntity2 entity, PosRot packet) {
      this.id = entity.getId();
      this.packet = packet;
   }

   public CameraMovePacket(FriendlyByteBuf buf) {
      this.id = buf.readVarInt();
      this.packet = PosRot.read(buf);
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.id);
      this.packet.write(buffer);
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

   public boolean handle(Context context) {
      ServerPlayer sender1 = context.getSender();
      context.enqueueWork(
         () -> {
            if (sender1.level().getEntity(this.id) instanceof CameraEntity2 conductor && sender1.getCamera() == conductor) {
               if (containsInvalidValues(
                  this.packet.getX(0.0), this.packet.getY(0.0), this.packet.getZ(0.0), this.packet.getYRot(0.0F), this.packet.getXRot(0.0F)
               )) {
                  sender1.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
                  return;
               }

               if (!(conductor.level() instanceof ServerLevel serverLevel)) {
                  return;
               }

               double var38 = clampHorizontal(this.packet.getX(conductor.getX()));
               double e = clampVertical(this.packet.getY(conductor.getY()));
               double f = clampHorizontal(this.packet.getZ(conductor.getZ()));
               float g = Mth.wrapDegrees(this.packet.getYRot(conductor.getYRot()));
               float h = Mth.wrapDegrees(this.packet.getXRot(conductor.getXRot()));
               if (conductor.isPassenger()) {
                  conductor.absMoveTo(conductor.getX(), conductor.getY(), conductor.getZ(), g, h);
                  return;
               }

               double i = conductor.getX();
               double j = conductor.getY();
               double k = conductor.getZ();
               double l = conductor.getY();
               double m = var38 - conductor.firstGoodX;
               double n = e - conductor.firstGoodY;
               double o = f - conductor.firstGoodZ;
               double p = conductor.getDeltaMovement().lengthSqr();
               double q = m * m + n * n + o * o;
               conductor.receivedMovePacketCount++;
               int r = conductor.receivedMovePacketCount - conductor.knownMovePacketCount;
               if (r > 5) {
                  r = 1;
               }

               float s = 100.0F;
               if (q - p > s * r) {
                  TallyhoMod.warn("{} moved too quickly! {},{},{}", sender1.getName().getString(), m, n, o);
                  teleport(sender1, conductor, conductor.getX(), conductor.getY(), conductor.getZ(), conductor.getYRot(), conductor.getXRot());
                  return;
               }

               AABB aABB = conductor.getBoundingBox();
               m = var38 - conductor.lastGoodX;
               n = e - conductor.lastGoodY;
               o = f - conductor.lastGoodZ;
               boolean bl2 = n > 0.0;
               boolean bl22 = conductor.verticalCollisionBelow;
               conductor.move(MoverType.PLAYER, new Vec3(m, n, o));
               m = var38 - conductor.getX();
               n = e - conductor.getY();
               if (n > -0.5 || n < 0.5) {
                  n = 0.0;
               }

               o = f - conductor.getZ();
               q = m * m + n * n + o * o;
               if (q > 0.0625) {
                  return;
               }

               conductor.absMoveTo(var38, e, f, g, h);
               if (!conductor.noPhysics && isPlayerCollidingWithAnythingNew(conductor, serverLevel, aABB)) {
                  teleport(sender1, conductor, i, j, k, g, h);
                  return;
               }

               conductor.setOnGround(this.packet.isOnGround());
               if (bl2) {
                  conductor.resetFallDistance();
               }

               conductor.lastGoodX = conductor.getX();
               conductor.lastGoodY = conductor.getY();
               conductor.lastGoodZ = conductor.getZ();
            }
         }
      );
      return true;
   }
}
