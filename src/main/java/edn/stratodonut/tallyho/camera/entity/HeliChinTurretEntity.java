package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.IClientKeyConsumer;
import edn.stratodonut.tallyho.KeyBinds;
import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.MultiOpticType;
import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.RangePacket;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AutocannonBreechBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterialProperties;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;
import rbasamoyai.createbigcannons.munitions.autocannon.AbstractAutocannonProjectile;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonRoundItem;
import rbasamoyai.createbigcannons.utils.CBCUtils;

public class HeliChinTurretEntity extends PointStabilisedCameraEntity implements ITurret, IClientKeyConsumer, ILaserDesignator {

    private static final EntityDataAccessor<Integer> DATA_ID_RANGE_SETTING =
            SynchedEntityData.defineId(HeliChinTurretEntity.class, EntityDataSerializers.INT);

    protected final AutocannonRoundItem AMMO =
                CBCItems.FLAK_AUTOCANNON_ROUND.get();

    protected static ItemStack FUZE;

    protected final AutocannonMaterialProperties MACHINE_GUN_PROPERTIES =
            (CBCBlocks.STEEL_AUTOCANNON_BREECH.get()).getAutocannonMaterial().properties();

    protected static final int BARREL_LENGTH = 7;
    private static final int BELT_LENGTH = 300;
    private static final int RELOAD_TIME_TICKS = 160;
    private static final int FIRING_COOLDOWN_TICKS = 2;

    private float designatorTicks = 0.0F;
    MultiOpticType opticMode;
    int belt;
    int reloadProgress;
    int firingCooldown;
    int timeout;

    public HeliChinTurretEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.belt = 0;
        this.reloadProgress = 0;
        this.firingCooldown = 0;
        this.timeout = 0;

        if (FUZE == null) {
            FUZE = new ItemStack(CBCItems.PROXIMITY_FUZE.get(), 1);
            FUZE.getOrCreateTag().putInt("DetonationDistance", 1);
        }

        this.opticMode = new MultiOpticType(new CameraEntity2.PostFXType[] {
                CameraEntity2.PostFXType.VISIBLE,
                CameraEntity2.PostFXType.THERMALS_WHITE
        });
    }

    public HeliChinTurretEntity(Level level) {
        this(AllEntities.HELI_CHIN_GUN_ENTITY.get(), level);
    }

    public static HeliChinTurretEntity spawn(ServerLevel level, Vec3 pos, float baseYaw) {
        HeliChinTurretEntity result = new HeliChinTurretEntity(level);
        result.setParams(baseYaw, CameraEntity2.PostFXType.VISIBLE,
                new AngleLimits(72.0F, 20.0F, 120.0F), -1);
        result.setPos(pos);
        level.addFreshEntity(result);
        FlexibleSeatEntity.sitDown(level, pos, result);
        result.setYRot(baseYaw);
        result.belt = 300;
        return result;
    }

    @Override
    public void tick() {
        super.tick();

        if (isDesignatorActive()) {
            this.designatorTicks--;
        }

        if (this.level().isClientSide()) {
            return;
        }

        if (isDesignatorActive()) {
            String code = getCode();
            Optional<LaserPointEntity> target = LaserPointEntity.find(this.level(),
                    this.getBoundingBox().inflate(400.0D), code);
            target.ifPresent(laserPoint -> {
              setRange((int) this.getEyePosition().distanceTo(laserPoint.position()));
              setStabVector(laserPoint.position());
            });
        }

        if (this.belt == 0) {
            this.reloadProgress++;
            if (this.reloadProgress > 160) {
                this.belt = 300;
                this.reloadProgress = 0;
            }
        }

        if (this.firingCooldown > 0) {
            this.firingCooldown--;
        }
    }

    @Override
    public final void handleShoot(LivingEntity shooter) {
        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        Ship ship = VSGameUtilsKt.getShipManaging((this.getVehicle() != null) ? this.getVehicle() : this);
        BlockPos virtualPos = this.blockPosition();
        Vec3 lookVector = this.getViewVector(1.0F);
        Vec3 spawnPos = this.position().add(lookVector.scale(2.0D));

        if (ship != null && this.isVehicle()) {
            virtualPos = this.getVehicle().blockPosition();
        }

        if (this.firingCooldown <= 0 && this.level().hasNeighborSignal(virtualPos)) {
            if (this.belt == 0) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 0.0F);
            } else {
                Vec3 direction = spawnPos.subtract(this.position()).normalize();
                ItemStack ammoStack = new ItemStack(CBCItems.FLAK_AUTOCANNON_ROUND.get(), 1);

                if (FUZE != null) {
                    ammoStack.getOrCreateTag().put("Fuze", FUZE.serializeNBT());
                }

                AbstractAutocannonProjectile projectile = this.AMMO.getAutocannonProjectile(ammoStack, serverLevel);
                float speed = this.MACHINE_GUN_PROPERTIES.baseSpeed() +
                        this.MACHINE_GUN_PROPERTIES.speedIncreasePerBarrel() * 7.0F;
                float spread = 1.2F;

                if (projectile != null) {
                    projectile.setPos(spawnPos);
                    projectile.setChargePower(7.0F);
                    projectile.setTracer(false);
                    projectile.setLifetime(this.MACHINE_GUN_PROPERTIES.projectileLifetime() * 3);
                    projectile.shoot(direction.x, direction.y, direction.z, speed, spread);
                    projectile.xRotO = projectile.getXRot();
                    projectile.yRotO = projectile.getYRot();
                    serverLevel.addFreshEntity(projectile);
                }

                CBCUtils.playBlastLikeSoundOnServer(serverLevel,
                        spawnPos.x, spawnPos.y, spawnPos.z,
                        CBCSoundEvents.FIRE_AUTOCANNON.getMainEvent(),
                        SoundSource.BLOCKS, 12.0F, 1.0F, 5.0F);

                this.belt--;
                this.firingCooldown = 2;
            }
        }
    }

    @Override
    public void designate() {
        this.designatorTicks = 20.0F;
        if (!this.level().isClientSide()) {
            String code = getCode();
            LaserPointEntity.shoot(this.level(), this.position(), this.getViewVector(1.0F), code);
        }
    }

    @Override
    public boolean isDesignatorActive() {
        return this.designatorTicks > 0.0F;
    }

    @Override
    public void turnView(double yRot, double xRot) {
        super.turnView(yRot, xRot);
        if (getRange() > 0 && (yRot != 0.0D || xRot != 0.0D)) {
            setStabVector(this.getEyePosition().add(this.getViewVector(1.0F).scale(getRange())));
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (CameraPossessionController.isPossessingConductor(player)) {
            return InteractionResult.FAIL;
        }

        if (!player.level().isClientSide()) {
            if (player.getItemInHand(hand).getItem() instanceof com.simibubi.create.content.equipment.wrench.WrenchItem) {
                this.discard();
                return InteractionResult.CONSUME;
            }
            startViewing((ServerPlayer) player);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isStab() {
        return getRange() > 0;
    }

    public void setRange(int range) {
        if (this.level().isClientSide()) {
            CreateTypePacketHandler.getChannel().sendToServer(new RangePacket(this, range));
        }
        this.entityData.set(DATA_ID_RANGE_SETTING, range);
    }

    public int getRange() {
        return this.entityData.get(DATA_ID_RANGE_SETTING);
    }

    @Override
    public void onKeyPressed(byte keyMap) {
        if (KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.UP)) {
            setRange(-1);
        }

        if (KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.LEFT) ||
                KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.RIGHT)) {
            this.opticMode.nextType();
            Minecraft.getInstance().gameRenderer.checkEntityPostEffect(this);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_RANGE_SETTING, -1);
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