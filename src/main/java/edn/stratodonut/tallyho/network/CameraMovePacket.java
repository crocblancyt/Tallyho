package edn.stratodonut.tallyho.network;

import com.google.common.primitives.Floats;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import java.util.Collections;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class CameraMovePacket extends SimplePacketBase {
    final int id;
    final ServerboundMovePlayerPacket.PosRot packet;

    public CameraMovePacket(CameraEntity2 entity, ServerboundMovePlayerPacket.PosRot packet) {
        this.id = entity.getId();
        this.packet = packet;
    }

    public CameraMovePacket(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.packet = ServerboundMovePlayerPacket.PosRot.read(buf);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id);
        this.packet.write(buffer);
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
        Iterable<VoxelShape> iterable = world.getCollisions(conductor, conductor.getBoundingBox().deflate(9.999999747378752E-6D));
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

    public boolean handle(NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        context.enqueueWork(() -> {
            Entity entity = sender.level().getEntity(this.id);
            if (entity instanceof CameraEntity2) {
                CameraEntity2 conductor = (CameraEntity2)entity;
                if (sender.getVehicle() == conductor) {
                    ServerLevel serverLevel;
                    if (containsInvalidValues(this.packet.getX(0.0D), this.packet.getY(0.0D), this.packet.getZ(0.0D), this.packet.getYRot(0.0F), this.packet.getXRot(0.0F))) {
                        sender.connection.disconnect(Component.literal("multiplayer.disconnect.invalid_player_movement"));
                        return;
                    }
                    Level level = conductor.level();
                    if (level instanceof ServerLevel) {
                        serverLevel = (ServerLevel)level;
                    } else {
                        return;
                    }
                    double d = clampHorizontal(this.packet.getX(conductor.getX()));
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
                    double m = d - conductor.firstGoodX;
                    double n = e - conductor.firstGoodY;
                    double o = f - conductor.firstGoodZ;
                    double p = conductor.getDeltaMovement().length();
                    double q = m * m + n * n + o * o;
                    conductor.receivedMovePacketCount++;
                    int r = conductor.receivedMovePacketCount - conductor.knownMovePacketCount;
                    if (r > 5)
                        r = 1;
                    float s = 100.0F;
                    if (q - p > (s * r)) {
                        TallyhoMod.warn("{} moved too quickly! {},{},{}", sender.getDisplayName().getString(), m, n, o);
                        teleport(sender, conductor, conductor.getX(), conductor.getY(), conductor.getZ(), conductor.getYRot(), conductor.getXRot());
                        return;
                    }
                    AABB aABB = conductor.getBoundingBox();
                    m = d - conductor.lastGoodX;
                    n = e - conductor.lastGoodY;
                    o = f - conductor.lastGoodZ;
                    boolean bl2 = (n > 0.0D);
                    boolean bl22 = conductor.verticalCollisionBelow;
                    conductor.move(MoverType.PLAYER, new Vec3(m, n, o));
                    double t = n;
                    m = d - conductor.getX();
                    n = e - conductor.getY();
                    if (n > -0.5D || n < 0.5D)
                        n = 0.0D;
                    o = f - conductor.getZ();
                    q = m * m + n * n + o * o;
                    if (q > 0.0625D)
                        return;
                    conductor.absMoveTo(d, e, f, g, h);
                    if (!conductor.noPhysics && isPlayerCollidingWithAnythingNew(conductor, serverLevel, aABB)) {
                        teleport(sender, conductor, i, j, k, g, h);
                        return;
                    }
                    conductor.setOnGround(this.packet.isOnGround());
                    if (bl2)
                        conductor.resetFallDistance();
                    conductor.lastGoodX = conductor.getX();
                    conductor.lastGoodY = conductor.getY();
                    conductor.lastGoodZ = conductor.getZ();
                }
            }
        });
        return true;
    }
}