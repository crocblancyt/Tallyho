package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.SetCameraViewPacket;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public abstract class CameraEntity2 extends Entity implements IEntityAdditionalSpawnData {

    private float baseYaw;
    private AngleLimits angleLimit = new AngleLimits(0.0F, 0.0F, 0.0F);
    private int fov;

    public static final List<Player> RECENTLY_DISMOUNTED_PLAYERS = new ArrayList<>();

    @Nonnull
    protected WeakReference<ServerPlayer> currentlyViewing = new WeakReference<>(null);

    public SectionPos oldSectionPos = null;
    public double firstGoodX;
    public double firstGoodY;
    public double firstGoodZ;
    public double lastGoodX;
    public double lastGoodY;
    public double lastGoodZ;
    public int receivedMovePacketCount;
    public int knownMovePacketCount;
    public double xLast;
    public double yLast;
    public double zLast;
    public double xRotLast;
    public double yRotLast;
    int timeout;

    public CameraEntity2(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.timeout = 0;
    }

    public static boolean hasRecentlyDismounted(Player player) {
        return RECENTLY_DISMOUNTED_PLAYERS.remove(player);
    }

    protected void setParams(float baseYaw, @Deprecated PostFXType opticType, AngleLimits angleLimit, int fov) {
        this.angleLimit = angleLimit;
        this.baseYaw = baseYaw;
        this.fov = fov;
    }

    public boolean startViewing(ServerPlayer player) {
        ServerPlayer current = this.currentlyViewing.get();
        if (current != null && current.getVehicle() == this && current.isAlive() && current != player) {
          return false;
        }

        ServerLevel serverLevel = player.serverLevel();
        if (serverLevel != this.level()) {
          return false;
        }

        this.currentlyViewing = new WeakReference<>(player);
        this.oldSectionPos = null;
        SectionPos chunkPos = SectionPos.of(this.blockPosition());
        int viewDistance = player.getServer().getPlayerList().getViewDistance();

        Entity entity = player.getVehicle();
        if (entity instanceof CameraEntity2) {
          CameraEntity2 conductor = (CameraEntity2) entity;
          conductor.stopViewing(player);
        }

        player.setCamera(this);
        CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> player),
                new SetCameraViewPacket(this));
        resetPosition();
        return true;
    }

    @Override
    protected float getEyeHeight(@NotNull Pose pose, EntityDimensions dimensions) {
        return dimensions.height;
    }

    @Override
    public double getMyRidingOffset() {
        return -0.25D;
    }

    public void stopViewing(ServerPlayer player) {
        if (!this.level().isClientSide()) {
            this.currentlyViewing.clear();
            player.setCamera(player);
            CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> player),
                    new SetCameraViewPacket(player));
            if (isShortLived()) {
                this.discard();
            }
        }
    }

    protected boolean isShortLived() {
        return true;
    }

    private void resetPosition() {
        this.firstGoodX = this.lastGoodX = this.getX();
        this.firstGoodY = this.lastGoodY = this.getY();
        this.firstGoodZ = this.lastGoodZ = this.getZ();
        this.knownMovePacketCount = this.receivedMovePacketCount;
    }

    @Override
    public void tick() {
        resetPosition();
        if (this.level().isClientSide()) {
            CameraPossessionController.tryUpdatePossession(this);
            updatePossessionInputs();
        }
        super.tick();
        if (!this.level().isClientSide()) {
            if (isShortLived() && this.currentlyViewing.get() == null) {
                this.timeout++;
            }
            if (this.getVehicle() == null) {
                this.timeout++;
            }
            if (this.timeout > 100) {
                this.discard();
            }
        }
    }

    @Override
    protected void onBelowWorld() {
        if (isPassenger())
            return;
        super.onBelowWorld();
    }

    @Override
    public void turn(double yRot, double xRot) {
        super.turn(yRot, xRot);
    }

    public void turnView(double yRot, double xRot) {
        turnView(yRot, xRot, false);
    }

    public void turnView(double yRot, double xRot, boolean raw) {
        AngleLimits limit = getRotationLimit();
        float baseYaw = getBaseYaw();
        float sensitivity = (raw || getFOV() <= 0) ? 1.0F : (getFOV() / 70.0F);
        float pitchDelta = (float) xRot * sensitivity;
        float yawDelta = (float) yRot * sensitivity;

        this.setXRot(Mth.clamp(this.getXRot() + pitchDelta, -limit.upperX(), limit.lowerX()));

        if (limit.Y() >= 180.0F) {
            this.setYRot(this.getYRot() + yawDelta);
        } else {
            this.setYRot(Mth.rotateIfNecessary(this.getYRot() + yawDelta, baseYaw, limit.Y()));
        }

        this.xRotO += pitchDelta;
        this.yRotO += yawDelta;
        this.xRotO = Mth.clamp(this.xRotO, -limit.upperX(), limit.lowerX());

        if (limit.Y() < 180.0F) {
            this.yRotO = Mth.clamp(this.yRotO, baseYaw - limit.Y(), baseYaw + limit.Y());
        }

        if (this.getVehicle() != null) {
            this.getVehicle().onPassengerTurned(this);
        }
    }

    public enum PostFXType {
        SCOPE(1),
        PERISCOPE(2),
        THERMALS_GREEN(3),
        CAMERA(4),
        THERMALS_WHITE(5),
        VISIBLE(6),
        NONE(0);

        public final int id;
        private static final PostFXType[] BY_ID;

        static {
            BY_ID = Arrays.stream(values())
                    .sorted(Comparator.comparingInt(o -> o.id))
                    .toArray(PostFXType[]::new);
        }

        PostFXType(int id) {
            this.id = id;
        }

        public static PostFXType get(int id) {
            if (0 < id && id < BY_ID.length)
                return BY_ID[id];
            return NONE;
        }
    }

    @Nonnull
    public PostFXType getOverlayType() {
        return PostFXType.NONE;
    }

    public int getFOV() {
        return this.fov;
    }

    public AngleLimits getRotationLimit() {
        return this.angleLimit;
    }

    public float getBaseYaw() {
        return this.baseYaw;
    }

    public void updatePossessionInputs() {
        if (isPossessedAndClient())
          updatePossessionInputsClient();
    }

    public boolean isFixedByPlayer() {
        return true;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return (super.isControlledByLocalInstance() || (isFixedByPlayer() && isPossessedAndClient()));
    }

    public boolean isPossessed() {
        return this.level().isClientSide() ? CameraClientHandler.isPossessed(this) : (this.currentlyViewing.get() != null);
    }

    public boolean isPossessedAndClient() {
        return (this.level().isClientSide() && isPossessed());
    }

    @OnlyIn(Dist.CLIENT)
    private void updatePossessionInputsClient() {}

    @Override
    public void setDeltaMovement(@Nonnull Vec3 motion) {}

    @Override
    protected void defineSynchedData() {}

    protected Vec2 getRotationFromDirection(Vec3 direction) {
        Vector3d relPos = VectorConversionsMCKt.toJOML(direction);
        if (VSGameUtilsKt.getShipMountedTo(this) != null) {
            double length = relPos.length();
            LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(this);
            if (loadedShip instanceof ClientShip) {
                ClientShip cs = (ClientShip) loadedShip;
                relPos = cs.getRenderTransform().getWorldToShip().transformDirection(relPos.normalize());
            } else {
                relPos = loadedShip.getWorldToShip().transformDirection(relPos.normalize());
            }
            relPos.mul(length);
        }

        double x = relPos.x;
        double y = relPos.y;
        double z = relPos.z;
        double horizontalDistance = Math.sqrt(x * x + z * z);

        float pitch = Mth.wrapDegrees((float) -(Mth.atan2(y, horizontalDistance) * 57.2957763671875D));
        float yaw = Mth.wrapDegrees((float) (Mth.atan2(z, x) * 57.2957763671875D) - 90.0F);

        return new Vec2(pitch, yaw);
    }

    protected Vec2 getLimitedRotationFromDirection(Vec3 direction) {
        Vec2 rawRotation = getRotationFromDirection(direction);
        AngleLimits limit = getRotationLimit();
        float pitch = rawRotation.x;
        float yaw = rawRotation.y;

        pitch = Mth.clamp(pitch, -limit.upperX(), limit.lowerX());

        if (limit.Y() < 180.0F) {
            yaw = Mth.rotateIfNecessary(yaw, getBaseYaw(), limit.Y());
        }

        if (Double.isNaN(pitch) || Double.isNaN(yaw))
            return new Vec2(0.0F, 0.0F);

        return new Vec2(pitch, yaw);
    }

    protected Vec2 getLimitedRotationFromTarget(Vec3 target, float partialTick) {
        return getLimitedRotationFromDirection(target.subtract(this.getEyePosition(partialTick)));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.angleLimit = new AngleLimits(
                nbt.getFloat("ANGLE_LIMIT_LOWER_X"),
                nbt.getFloat("ANGLE_LIMIT_UPPER_X"),
                nbt.getFloat("ANGLE_LIMIT_Y")
        );
        this.baseYaw = nbt.getFloat("BASE_YAW");
        this.fov = nbt.getInt("FOV");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("OPTIC_TYPE", getOverlayType().id);
        AngleLimits limit = getRotationLimit();
        nbt.putFloat("ANGLE_LIMIT_LOWER_X", limit.lowerX());
        nbt.putFloat("ANGLE_LIMIT_UPPER_X", limit.upperX());
        nbt.putFloat("ANGLE_LIMIT_Y", limit.Y());
        nbt.putFloat("BASE_YAW", getBaseYaw());
        nbt.putInt("FOV", getFOV());
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        CompoundTag nbt = new CompoundTag();
        addAdditionalSaveData(nbt);
        buffer.writeNbt(nbt);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        CompoundTag nbt = additionalData.readNbt();
        if (nbt != null)
            readAdditionalSaveData(nbt);
    }
}

