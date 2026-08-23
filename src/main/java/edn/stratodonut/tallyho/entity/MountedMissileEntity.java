package edn.stratodonut.tallyho.entity;

import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.Utils;
import edn.stratodonut.tallyho.clip.PiercingClipContext;
import edn.stratodonut.tallyho.missile.FuzeComponent;
import edn.stratodonut.tallyho.missile.GuidanceComponent;
import edn.stratodonut.tallyho.missile.IWarhead;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import edn.stratodonut.tallyho.missile.MotorComponent;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.MissileVelocityPacket;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.EntityShipCollisionUtils;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.ritchiesprojectilelib.RitchiesProjectileLib;

public class MountedMissileEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(MountedMissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_DEPLOYED = SynchedEntityData.defineId(MountedMissileEntity.class, EntityDataSerializers.BOOLEAN);

    @Nonnull
    public ItemStack modelItem = ItemStack.EMPTY;
    private String missileId;
    private IWarhead warhead;
    @Nullable
    private FuzeComponent fuze;
    @Nullable
    private GuidanceComponent guidance;
    @Nullable
    private MotorComponent motor;

    public final int NOCLIP_TICKS = 5;
    public final int DEPLOY_TICKS = 10;
    protected boolean lastPowered = false;
    protected boolean isLongRange = false;
    protected int ticksSinceLaunch = 0;

    public MountedMissileEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.hitboxScale = 1.0F;
    }

    public MountedMissileEntity(EntityType<?> type, Level level, Item item) {
        this(type, level);
        this.modelItem = new ItemStack((ItemLike)item);
    }

    public MountedMissileEntity(EntityType<?> type, Level level, String id, Item item) {
        super(type, level);
        this.hitboxScale = 1.0F;
        loadDataFromID(id);
        this.modelItem = new ItemStack((ItemLike)item);
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.level().isClientSide() && player.getItemInHand(hand).getItem() instanceof com.simibubi.create.content.equipment.wrench.WrenchItem) {
            discard();
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    public void tick() {
        super.tick();
        if (!isPassenger()) {
            updateHeading();
            if (!level().isClientSide()) {
                Vec3 startVec = position();
                Vec3 endVec = startVec.add(getDeltaMovement());
                BlockHitResult result = level().clip((ClipContext)new PiercingClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this, blockState -> blockState.is(BlockTags.CLIMBABLE)));
                if (this.ticksSinceLaunch > 5)
                    if (result.getType() != HitResult.Type.MISS) {
                        endVec = result.getLocation();
                        if (this.fuze != null && this.fuze.impact(this, level(), endVec, getDeltaMovement()))
                            detonate(endVec);
                    } else if (this.fuze != null) {
                        this.fuze.tick(this, level(), endVec, getDeltaMovement()).ifPresent(this::detonate);
                    }
                if (this.ticksSinceLaunch > 10 && !this.entityData.get(DATA_DEPLOYED))
                    this.entityData.set(DATA_DEPLOYED, true);
            }
            Vec3 vel = getDeltaMovement();
            if (this.motor == null) {
                vel = vel.add(0.0D, -0.05D, 0.0D).add(vel.normalize().scale(vel.length() * -0.001D));
                setDeltaMovement(vel);
            }
        }
        if (this.motor != null)
            this.motor.tick(this);
        if (this.guidance != null)
            this.guidance.tick(this);
        double nextPosX = getX() + getDeltaMovement().x();
        double nextPosY = getY() + getDeltaMovement().y();
        double nextPosZ = getZ() + getDeltaMovement().z();
        setPos(nextPosX, nextPosY, nextPosZ);
        if (!level().isClientSide()) {
            LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(this);
            if (isPassenger()) {
                BlockPos virtualPos = blockPosition();
                if (loadedShip != null)
                    virtualPos = getVehicle().blockPosition();
                if (level().hasChunkAt(virtualPos)) {
                    boolean currentlyPowered = level().hasNeighborSignal(virtualPos);
                    if (currentlyPowered && !this.lastPowered) {
                        this.lastPowered = true;
                        if (this.guidance != null) {
                            if (this.guidance.canLaunch()) {
                                this.guidance.activateSeeker();
                                launch();
                            }
                            if (!this.guidance.isSeeking())
                                this.guidance.activateSeeker();
                        } else {
                            launch();
                        }
                    }
                    if (!currentlyPowered && this.lastPowered)
                        this.lastPowered = false;
                }
            }
            if (this.ticksSinceLaunch >= 1200 && (this.fuze == null || this.fuze.explodesOnExpiry()))
                detonate(position());
            CreateTypePacketHandler.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new MissileVelocityPacket(getId(), getDeltaMovement()));
        }
        if (!isPassenger()) {
            this.ticksSinceLaunch++;
        } else {
            this.ticksSinceLaunch = 0;
        }
        Level level = level();
        if (level instanceof ServerLevel) {
            ServerLevel slevel = (ServerLevel)level;
            if (!isRemoved() && this.isLongRange && !isPassenger() && CBCConfigs.SERVER.munitions.projectilesCanChunkload.get()) {
                ChunkPos cpos1 = new ChunkPos(blockPosition());
                RitchiesProjectileLib.queueForceLoad(slevel, cpos1.x, cpos1.z);
            }
        }
    }

    public boolean launch(Vec3 dir, float boost) {
        LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(this);
        if (this.guidance != null)
            this.guidance.launch(this);
        stopRiding();
        if (loadedShip != null) {
            Utils.PosVel compensation = Utils.moveInWorld(position(), (Ship)loadedShip, 3.0F);
            lookAt(EntityAnchorArgument.Anchor.FEET, position().add(dir));
            setPos(compensation.pos());
            setDeltaMovement(compensation.vel());
        }
        if (this.motor != null) {
            this.motor.launch(this, dir, boost);
        } else {
            Vec3 vel = dir.scale(boost);
            setDeltaMovement(vel);
        }
        return true;
    }

    public boolean launch() {
        return launch(getLookAngle(), 0.0F);
    }

    protected static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);
    protected static final Vec3 LEFT = new Vec3(1.0D, 0.0D, 0.0D);
    protected float hitboxScale;

    @Nonnull
    public Vec3 disperseSquareErrorProbable50(@Nonnull Vec3 dir, double mils) {
        dir = dir.normalize();
        Vec3 a = UP, b = LEFT;
        if (Math.abs(dir.dot(a)) > 0.707D) {
            a = dir.cross(a);
        } else {
            a = dir.cross(b);
        }
        b = dir.cross(a);
        double ratio = Math.tan(mils / 2000.0D);
        return dir.add(a.scale(ratio * this.random.nextGaussian())).add(b.scale(ratio * this.random.nextGaussian())).normalize();
    }

    protected void detonate(Vec3 hit) {
        if (level().isClientSide)
            return;
        if (this.warhead.detonate(level(), hit, getDeltaMovement().normalize()))
            remove(Entity.RemovalReason.KILLED);
    }

    @Nonnull
    private Vec3 checkCollide(Vec3 movement, AABB aabb) {
        List<VoxelShape> list = level().getEntityCollisions(this, aabb.expandTowards(movement));
        return (movement.length() == 0.0D) ? movement : collideBoundingBox(null, movement, aabb, level(), list);
    }

    @Nonnull
    public Vec3 collideOnlyShips(Vec3 movement, AABB aabb) {
        return (movement.length() == 0.0D) ? movement : EntityShipCollisionUtils.INSTANCE.adjustEntityMovementForShipCollisions(this, movement, aabb, level());
    }

    @Nonnull
    public Vec3 collideWithShips(Vec3 movement, AABB aabb) {
        movement = collideOnlyShips(movement, aabb);
        return checkCollide(movement, aabb);
    }

    public boolean hurt(DamageSource source, float amount) {
        if (super.hurt(source, amount) && this.ticksSinceLaunch > 5 && amount > 5.0F) {
            float hp = this.entityData.get(DATA_HEALTH_ID);
            this.entityData.set(DATA_HEALTH_ID, Mth.clamp(hp - amount + 5.0F, 0.0F, 20.0F));
            if (hp - amount + 5.0F <= 0.0F)
                detonate(position());
            return true;
        }
        return false;
    }

    public boolean isAlive() {
        return (super.isAlive() && this.entityData.get(DATA_HEALTH_ID) > 0.0F);
    }

    public void updateHeading() {
        Vec3 weightedLook = getDeltaMovement().scale(0.3333333432674408D).add(getLookAngle());
        lookAt(EntityAnchorArgument.Anchor.FEET, position().add(weightedLook));
    }

    public boolean isDeployed() {
        return this.entityData.get(DATA_DEPLOYED);
    }

    @Nullable
    public GuidanceComponent getGuidance() {
        return this.guidance;
    }

    @Nullable
    public MotorComponent getMotor() {
        return this.motor;
    }

    @Nullable
    public String getMissileId() {
        return this.missileId;
    }

    public boolean isPickable() {
        return !isRemoved();
    }

    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.5F;
    }

    private void loadDataFromID(String id) {
        MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(id);
        if (entry == null) {
            TallyhoMod.warn("Failed to load missile of id '{}'!", id);
            return;
        }
        this.missileId = id;
        if (this.guidance == null)
            this.guidance = entry.getGuidance();
        if (this.motor == null)
            this.motor = entry.getMotor();
        if (this.fuze == null)
            this.fuze = entry.getFuze();
        this.warhead = entry.warhead;
        this.hitboxScale = entry.getHitboxScale();
        this.isLongRange = entry.isLongRange();
        refreshDimensions();
    }

    @Nonnull
    public EntityDimensions getDimensions(@Nonnull Pose pose) {
        return super.getDimensions(pose).scale(this.hitboxScale);
    }

    @Nonnull
    public AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().inflate(3.0D);
    }

    public float getHitboxScale() {
        return this.hitboxScale;
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_DEPLOYED, false);
        this.entityData.define(DATA_HEALTH_ID, 20.0F);
    }

    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains("MissileId"))
            loadDataFromID(nbt.getString("MissileId"));
        if (this.guidance != null && nbt.contains("Guidance"))
            this.guidance.deserialiseNBT(nbt.getCompound("Guidance"));
        if (this.motor != null && nbt.contains("Motor"))
            this.motor.deserialiseNBT(nbt.getCompound("Motor"));
        this.lastPowered = nbt.getBoolean("LastPowered");
        this.modelItem = ItemStack.of(nbt.getCompound("ModelItem"));
    }

    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putString("MissileId", this.missileId);
        if (this.guidance != null)
            nbt.put("Guidance", (Tag)this.guidance.serialiseNBT());
        if (this.motor != null)
            nbt.put("Motor", (Tag)this.motor.serialiseNBT());
        nbt.putBoolean("LastPowered", this.lastPowered);
        nbt.put("ModelItem", this.modelItem.serializeNBT());
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        CompoundTag compound = new CompoundTag();
        addAdditionalSaveData(compound);
        buffer.writeNbt(compound);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        readAdditionalSaveData(additionalData.readNbt());
    }

    public int getTicksSinceLaunch() {
        return this.ticksSinceLaunch;
    }
}