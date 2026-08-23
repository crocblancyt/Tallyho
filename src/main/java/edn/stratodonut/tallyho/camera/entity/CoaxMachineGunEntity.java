package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.AllEntities;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AutocannonBreechBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterialProperties;
import rbasamoyai.createbigcannons.effects.particles.plumes.AutocannonPlumeParticleData;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;
import rbasamoyai.createbigcannons.munitions.autocannon.AbstractAutocannonProjectile;
import rbasamoyai.createbigcannons.munitions.autocannon.bullet.MachineGunRoundItem;
import rbasamoyai.createbigcannons.utils.CBCUtils;

public class CoaxMachineGunEntity extends Entity implements IEntityAdditionalSpawnData {
    private float baseYaw;

    protected final MachineGunRoundItem MACHINE_GUN_ROUND = (MachineGunRoundItem) CBCItems.MACHINE_GUN_ROUND.get();

    protected final AutocannonMaterialProperties MACHINE_GUN_PROPERTIES = ((AutocannonBreechBlock) CBCBlocks.STEEL_AUTOCANNON_BREECH.get()).getAutocannonMaterial().properties();

    protected final int BARREL_LENGTH = 10;

    private final int BELT_LENGTH = 500;

    private final int RELOAD_TIME_TICKS = 160;

    private final int FIRING_COOLDOWN_TICKS = 2;

    protected int muzzleOffset = 0;

    @Nonnull
    WeakReference<PitchOrientedContraptionEntity> coaxialTo = new WeakReference<>(null);

    int belt;
    int reloadProgress;
    int firingCooldown;
    int timeout;

    public CoaxMachineGunEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.belt = 0;
        this.reloadProgress = 0;
        this.firingCooldown = 0;
        this.timeout = 0;
    }

    @Override
    protected void defineSynchedData() {

    }

    public CoaxMachineGunEntity(Level level, int muzzleOffset) {
        super((EntityType) AllEntities.COAX_MACHINE_GUN_ENTITY.get(), level);
        this.belt = 0;
        this.reloadProgress = 0;
        this.firingCooldown = 0;
        this.timeout = 0;
        this.muzzleOffset = muzzleOffset;
    }

    public static CoaxMachineGunEntity spawn(ServerLevel level, BlockPos pos, float baseYaw, int muzzleOffset) {
        CoaxMachineGunEntity result = new CoaxMachineGunEntity(level, muzzleOffset);
        result.baseYaw = baseYaw;
        result.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        level.addFreshEntity(result);
        FlexibleSeatEntity.sitDown(level, pos, result);
        result.setYRot(baseYaw);
        return result;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.level().isClientSide() && player.getItemInHand(hand).getItem() instanceof com.simibubi.create.content.equipment.wrench.WrenchItem) {
            this.discard();
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;

            if (this.belt == 0) {
                this.reloadProgress++;
                if (this.reloadProgress > 160) {
                    this.belt = 500;
                    this.reloadProgress = 0;
                }
            }

            if (this.coaxialTo.get() == null) {
                List<PitchOrientedContraptionEntity> cannons = serverLevel.getEntitiesOfClass(
                        PitchOrientedContraptionEntity.class,
                        this.getBoundingBox().inflate(2.0D)
                );
                if (cannons.isEmpty()) return;
                this.coaxialTo = new WeakReference<>(cannons.get(0));
            }

            PitchOrientedContraptionEntity cannonEntity = this.coaxialTo.get();
            if (cannonEntity.isRemoved() || !cannonEntity.isAlive()) {
                this.coaxialTo = new WeakReference<>(null);
                return;
            }

            LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(this);
            Vec3 orientation = Vec3.atLowerCornerOf(cannonEntity.getInitialOrientation().getNormal());
            Vec3 muzzleDirection = cannonEntity.applyRotation(orientation, 1.0F);

            this.lookAt(EntityAnchorArgument.Anchor.EYES, this.getEyePosition().add(muzzleDirection));

            BlockPos virtualPos = this.blockPosition();
            Vec3 lookVec = this.getViewVector(1.0F);
            Vec3 spawnPos = this.getEyePosition().add(lookVec.scale(this.muzzleOffset + 1));

            if (loadedShip != null && this.isVehicle()) {
                virtualPos = this.getVehicle().blockPosition();
            }

            if (this.firingCooldown > 0) {
                this.firingCooldown--;
            }

            if (this.firingCooldown <= 0 && this.level().hasNeighborSignal(virtualPos) && this.level().hasNeighborSignal(virtualPos)) {
                if (this.belt == 0) {
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 0.0F);
                } else {
                    Vec3 direction = spawnPos.subtract(this.getEyePosition()).normalize();
                    Vec3 particlePos = spawnPos.subtract(direction.scale(1.5D));

                    AbstractAutocannonProjectile projectile = this.MACHINE_GUN_ROUND.getAutocannonProjectile(null, serverLevel);
                    float speed = this.MACHINE_GUN_PROPERTIES.baseSpeed() + this.MACHINE_GUN_PROPERTIES.speedIncreasePerBarrel() * 10.0F;
                    float spread = 0.717F;

                    if (projectile != null) {
                        projectile.setPos(spawnPos);
                        projectile.setChargePower(10.0F);
                        projectile.setTracer(true);
                        projectile.setLifetime(this.MACHINE_GUN_PROPERTIES.projectileLifetime() * 2);
                        projectile.shoot(direction.x, direction.y, direction.z, speed, spread);
                        projectile.xRotO = projectile.getXRot();
                        projectile.yRotO = projectile.getYRot();
                        serverLevel.addFreshEntity(projectile);
                    }

                    Vec3 particleVelocity = direction.scale(1.25D);

                    for (Player player : this.level().players()) {
                        serverLevel.sendParticles((ServerPlayer) player,
                                  new AutocannonPlumeParticleData(1.0F), true,
                                  particlePos.x, particlePos.y, particlePos.z,
                                  0, particleVelocity.x, particleVelocity.y, particleVelocity.z, 1.0D);
                    }

                    CBCUtils.playBlastLikeSoundOnServer(serverLevel,
                            spawnPos.x, spawnPos.y, spawnPos.z,
                            CBCSoundEvents.FIRE_MACHINE_GUN.getMainEvent(),
                            SoundSource.BLOCKS, 10.0F, 0.75F, 3.0F);

                    this.belt--;
                    this.firingCooldown = 2;
              }
            }
        }

        if (this.getVehicle() == null) {
            this.timeout++;
        }

        if (this.timeout > 100) {
            this.discard();
        }
    }

    public float getBaseYaw() {
      return this.baseYaw;
    }

    @Override
    public boolean isAttackable() {
      return !this.isRemoved();
    }

    @Override
    protected void removePassenger(Entity passenger) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.belt = nbt.getInt("belt");
        this.reloadProgress = nbt.getInt("reload");
        this.muzzleOffset = nbt.getInt("muzzleOffset");
        this.baseYaw = nbt.getFloat("BASE_YAW");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("belt", this.belt);
        nbt.putInt("reload", this.reloadProgress);
        nbt.putInt("muzzleOffset", this.muzzleOffset);
        nbt.putFloat("BASE_YAW", getBaseYaw());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {}

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {}
}