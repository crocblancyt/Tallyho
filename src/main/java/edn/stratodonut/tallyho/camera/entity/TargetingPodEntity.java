package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.IClientKeyConsumer;
import edn.stratodonut.tallyho.KeyBinds;
import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.ICameraStatus;
import edn.stratodonut.tallyho.camera.MultiOpticType;
import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.TGPChangeModePacket;
import edn.stratodonut.tallyho.network.TGPChangeModePacketClient;
import edn.stratodonut.tallyho.network.TGPLazeTogglePacket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class TargetingPodEntity extends PointStabilisedCameraEntity implements ICameraStatus, IClientKeyConsumer {

    private static final EntityDataAccessor<Boolean> DATA_ID_LAZING = SynchedEntityData.defineId(TargetingPodEntity.class, EntityDataSerializers.BOOLEAN);

    public enum TGP_MODE {
        NONE(0),
        INR(1),
        LSRCH(2);

        private final int id;
        private static final TGP_MODE[] BY_ID;

        static {
            BY_ID = Arrays.stream(values())
                .sorted(Comparator.comparingInt(o -> o.id))
                .toArray(TGP_MODE[]::new);
        }

        TGP_MODE(int value) {
            this.id = value;
        }

        public static TGP_MODE get(int id) {
            if (0 < id && id < BY_ID.length) {
                return BY_ID[id];
            }
            return NONE;
        }

        public int id() {
            return this.id;
        }
    }

    protected TGP_MODE mode = TGP_MODE.NONE;
    MultiOpticType opticMode;
    int stabDelay;
    public float roll;
    public float pitch;
    private float lastRotX;
    private float lastRotY;

    public TargetingPodEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.stabDelay = 0;
        this.roll = 0.0F;
        this.pitch = 0.0F;
        this.lastRotX = 0.0F;
        this.lastRotY = 0.0F;
        this.opticMode = new MultiOpticType(new CameraEntity2.PostFXType[] { CameraEntity2.PostFXType.VISIBLE, CameraEntity2.PostFXType.THERMALS_WHITE });
    }

    public TargetingPodEntity(Level level) {
        this((EntityType<? extends Entity>) AllEntities.TARGETING_POD_ENTITY.get(), level);
    }

    public static TargetingPodEntity spawn(ServerLevel level, BlockPos pos, float baseYaw) {
        TargetingPodEntity result = new TargetingPodEntity(level);
        result.setParams(baseYaw, CameraEntity2.PostFXType.THERMALS_WHITE, new AngleLimits(89.0F, 89.0F, 140.0F), -1);
        level.addFreshEntity(result);
        FlexibleSeatEntity.sitDown(level, pos, result);
        result.setYRot(baseYaw);
        return result;
    }

    @Override
    public void tick() {
        super.tick();

        TGP_MODE mode = getTGPMode();
        if (this.level().isClientSide()) {
            return;
        }

        Vec3 stabLookVector = this.virtualStabVec.subtract(this.getEyePosition()).normalize();

        if (isDesignating()) {
            String code = getCode();
            LaserPointEntity.shoot(this.level(), this.getEyePosition(), stabLookVector, code);
        }

        if (mode != TGP_MODE.INR) {
            if (mode == TGP_MODE.LSRCH) {
                Optional<LaserPointEntity> target = LaserPointEntity.find(
                    this.level(),
                    this.getBoundingBox().expandTowards(stabLookVector.scale(200.0D)).inflate(400.0D),
                    laserPoint -> (laserPoint.getCode() != null && !laserPoint.match(getCode()) && laserPoint.getGroundTicks() > 0),
                    Comparator.comparingDouble(laserPoint -> laserPoint.position().distanceTo(this.position()))
                );
                target.ifPresent(laserPoint -> setTGP(TGP_MODE.INR, laserPoint.position()));
            } else if (mode == TGP_MODE.NONE) {
                if (this.stabDelay >= 20) {
                    LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(this);
                    if (loadedShip != null) {
                        LaserPointEntity.shootWithPlatformVelocity(
                            this.level(), this.getEyePosition(), this.getViewVector(1.0F),
                            VectorConversionsMCKt.toMinecraft(loadedShip.getVelocity()), null
                        );
                    } else {
                        LaserPointEntity.shoot(this.level(), this.getEyePosition(), this.getViewVector(1.0F), null);
                    }
                    Optional<LaserPointEntity> target = LaserPointEntity.findUncoded(
                        this.level(), this.getEyePosition(), this.getViewVector(1.0F), 800.0F, 22.5D
                    );
                    target.ifPresent(laserPoint -> setTGP(TGP_MODE.INR, laserPoint.position()));
                } else {
                    this.stabDelay++;
                }
            }
        }
    }

    public boolean isStab() {
        return true;
    }

    @Override
    public void syncStab(Vec3 v) {
        super.syncStab(v);
        if (!this.level().isClientSide()) {
            this.stabDelay = 0;
        }
    }

    @Nonnull
    @Override
    public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand) {
        if (CameraPossessionController.isPossessingConductor(player)) {
            return InteractionResult.FAIL;
        }

        if (!player.level().isClientSide() && player.getItemInHand(hand).getItem() instanceof com.simibubi.create.content.equipment.wrench.WrenchItem) {
            this.discard();
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void turnView(double yRot, double xRot) {
        super.turnView(yRot, xRot);
        if (yRot != 0.0D || xRot != 0.0D) {
            if (getTGPMode() == TGP_MODE.INR) {
                setTGP(TGP_MODE.NONE, this.getViewVector(1.0F));
            } else {
                setStabVector(this.getViewVector(1.0F));
            }
        }
    }

    @Override
    public void onKeyPressed(byte keyMap) {
        if (KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.UP)) {
            toggleLazeMode();
        } else if (KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.DOWN)) {
            TGP_MODE currentMode = getTGPMode();
            if (currentMode != TGP_MODE.LSRCH) {
                setTGP(TGP_MODE.LSRCH, this.getViewVector(1.0F));
            } else {
                setTGP(TGP_MODE.NONE, this.getViewVector(1.0F));
            }
        } else if (KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.LEFT) ||
                   KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.RIGHT)) {
            this.opticMode.nextType();

            //Minecraft.getInstance().getEntityRenderDispatcher().render(this);
        }
    }

    public void toggleLazeMode() {
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_ID_LAZING, !this.entityData.get(DATA_ID_LAZING));
        } else {
            CreateTypePacketHandler.getChannel().sendToServer(new TGPLazeTogglePacket(this));
        }
    }

    public void handle(TGPChangeModePacketClient packet) {
        if (!this.level().isClientSide()) {
            setTGP(TGP_MODE.get(packet.mode), packet.stab);
        } else if (this.mode.id() != packet.mode) {
            this.mode = TGP_MODE.get(packet.mode);
            this.virtualStabVec = packet.stab;
        }
    }

    public void handle(TGPChangeModePacket packet) {
        if (!this.level().isClientSide()) {
            setTGP(TGP_MODE.get(packet.mode), packet.stab);
        } else if (this.mode.id() != packet.mode) {
            this.mode = TGP_MODE.get(packet.mode);
            this.virtualStabVec = packet.stab;
        }
    }

    public void setTGP(TGP_MODE mode, @Nonnull Vec3 stab) {
        if (this.level().isClientSide()) {
            CreateTypePacketHandler.getChannel().sendToServer(new TGPChangeModePacket(this, mode, stab));
        } else {
            CreateTypePacketHandler.getChannel().send(
                PacketDistributor.TRACKING_ENTITY.with(() -> this),
                new TGPChangeModePacketClient(this, mode, stab)
            );
        }
        this.mode = mode;
        this.virtualStabVec = stab;
    }

    public TGP_MODE getTGPMode() {
        return this.mode;
    }

    public boolean isDesignating() {
        return this.entityData.get(DATA_ID_LAZING);
    }

    @Nonnull
    protected Vec2 getStabView(float partialTick) {
        LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(this);
        if (loadedShip != null) {
            TGP_MODE currentMode = getTGPMode();
            if (currentMode == TGP_MODE.INR) {
                return getLimitedRotationFromTarget(this.virtualStabVec, partialTick);
            }
            return getLimitedRotationFromDirection(this.virtualStabVec);
        }
        return this.getRotationVector();
    }

    public String getStatus() {
        String base;
        TGP_MODE currentMode = getTGPMode();
        switch (currentMode) {
            case NONE:
                base = "TV       ";
                break;
            case INR:
                base = "TV  INR  ";
                break;
            case LSRCH:
                base = "TV LSRCH ";
                break;
            default:
                base = "TV       ";
                break;
        }
        return base + base;
    }

    @Override
    protected void setRot(float yaw, float pitch) {
        boolean significantChange = (Mth.degreesDifferenceAbs(yaw, this.lastRotY) > 1.0F ||
                                     Mth.degreesDifferenceAbs(pitch, this.lastRotX) > 1.0F);
        super.setRot(yaw, pitch);

        if (this.level().isClientSide() && significantChange) {
            this.lastRotX = this.getXRot();
            this.lastRotY = this.getYRot();

            Vector3f lookVector = this.getViewVector(1.0F).toVector3f();
            Vector3f forwardVector = Direction.fromYRot(getBaseYaw()).step();
            Vector3f upVector = Direction.UP.step();

            LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(this);
            if (loadedShip != null) {
                upVector = loadedShip.getShipToWorld().transformDirection(upVector, new Vector3f());
                forwardVector = loadedShip.getShipToWorld().transformDirection(forwardVector, new Vector3f());
            }

            Vector3f projected = lookVector.sub(
                forwardVector.get(new Vector3f()).mul(lookVector.dot(forwardVector)),
                new Vector3f()
            ).normalize();

            this.roll = (float) Math.toDegrees(Math.asin(
                projected.cross(upVector, new Vector3f()).dot(forwardVector)
            ));
            this.pitch = (float) Math.toDegrees(lookVector.angle(forwardVector));

            if (lookVector.dot(upVector) < 0.0F) {
                this.roll *= -1.0F;
                this.pitch *= -1.0F;
            }

            if (Float.isNaN(this.roll)) this.roll = 0.0F;
            if (Float.isNaN(this.pitch)) this.pitch = 0.0F;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_LAZING, false);
    }

    @NotNull
    @Override
    public CameraEntity2.PostFXType getOverlayType() {
        return this.opticMode.getCurrentOptic();
    }

    @Override
    protected boolean isShortLived() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected float getEyeHeight(@NotNull Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.5F;
    }
}