package edn.stratodonut.tallyho.network;

import com.google.common.primitives.Floats;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import java.util.Collections;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class CameraMovePacketClient extends SimplePacketBase {
    final int id;
    final ServerboundMovePlayerPacket.PosRot packet;

    public CameraMovePacketClient(CameraEntity2 entity, ServerboundMovePlayerPacket.PosRot packet) {
        this.id = entity.getId();
        this.packet = packet;
    }

    public CameraMovePacketClient(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.packet = ServerboundMovePlayerPacket.PosRot.read(buf);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id);
        this.packet.write(buffer);
    }

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(this.id);
                if (entity instanceof CameraEntity2 conductor) {
                    if (mc.cameraEntity == conductor) {
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
                }
            }
        });
        return true;
    }

    private static boolean containsInvalidValues(double x, double y, double z, float yaw, float pitch) {
        return (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || !Floats.isFinite(pitch) || !Floats.isFinite(yaw));
    }

    private static double clampHorizontal(double value) {
        return Mth.clamp(value, -3.0E7D, 3.0E7D);
    }

    private static double clampVertical(double value) {
        return Mth.clamp(value, -2.0E7D, 2.0E7D);
    }

    private static boolean isPlayerCollidingWithAnythingNew(CameraEntity2 conductor, LevelReader world, AABB box) {
        Iterable<VoxelShape> iterable = world.getBlockCollisions(conductor, conductor.getBoundingBox().deflate(9.999999747378752E-6D));
        VoxelShape voxelShape = Shapes.create(box.deflate(9.999999747378752E-6D));
        for (VoxelShape voxelShape2 : iterable) {
            if (Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.AND))
                continue;
            return true;
        }
        return false;
    }

    public static void teleport(ServerPlayer player, CameraEntity2 conductor, double x, double y, double z, float yaw, float pitch) {
        teleport(player, conductor, x, y, z, yaw, pitch, Collections.emptySet(), false);
    }

    public static void teleport(ServerPlayer player, CameraEntity2 conductor, double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> relativeSet, boolean dismountVehicle) {
        double d = relativeSet.contains(RelativeMovement.X) ? conductor.getX() : 0.0D;
        double e = relativeSet.contains(RelativeMovement.Y) ? conductor.getY() : 0.0D;
        double f = relativeSet.contains(RelativeMovement.Z) ? conductor.getZ() : 0.0D;
        float g = relativeSet.contains(RelativeMovement.Y_ROT) ? conductor.getYRot() : 0.0F;
        float h = relativeSet.contains(RelativeMovement.X_ROT) ? conductor.getXRot() : 0.0F;
        conductor.absMoveTo(x, y, z, yaw, pitch);
        CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> player), new CameraMovePacketClient(conductor, new ServerboundMovePlayerPacket.PosRot(x, y, z, yaw, pitch, conductor
                .onGround())));
    }
}