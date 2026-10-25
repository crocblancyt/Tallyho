package edn.stratodonut.tallyho.entity;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
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
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
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
   protected static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
   protected static final Vec3 LEFT = new Vec3(1.0, 0.0, 0.0);
   protected float hitboxScale = 1.0F;

   public MountedMissileEntity(EntityType<?> p_19870_, Level p_19871_) {
      super(p_19870_, p_19871_);
   }

   public MountedMissileEntity(EntityType<?> p_19870_, Level p_19871_, Item item) {
      this(p_19870_, p_19871_);
      this.modelItem = new ItemStack(item);
   }

   public MountedMissileEntity(EntityType<?> p_19870_, Level p_19871_, String id, Item item) {
      super(p_19870_, p_19871_);
      this.loadDataFromID(id);
      this.modelItem = new ItemStack(item);
   }

   public InteractionResult interact(Player p_19978_, InteractionHand p_19979_) {
      if (!p_19978_.level().isClientSide() && p_19978_.getItemInHand(p_19979_).getItem() instanceof WrenchItem) {
         this.discard();
         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   public void tick() {
      super.tick();
      if (!this.isPassenger()) {
         this.updateHeading();
         if (!this.level().isClientSide()) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            BlockHitResult result = this.level()
               .clip(new PiercingClipContext(startVec, endVec, Block.COLLIDER, Fluid.NONE, this, blockState -> blockState.is(BlockTags.LEAVES)));
            if (this.ticksSinceLaunch > 5) {
               if (result.getType() != Type.MISS) {
                  endVec = result.getLocation();
                  if (this.fuze != null && this.fuze.impact(this, this.level(), endVec, this.getDeltaMovement())) {
                     this.detonate(endVec);
                  }
               } else if (this.fuze != null) {
                  this.fuze.tick(this, this.level(), endVec, this.getDeltaMovement()).ifPresent(this::detonate);
               }
            }

            if (this.ticksSinceLaunch > 10 && !(Boolean)this.entityData.get(DATA_DEPLOYED)) {
               this.entityData.set(DATA_DEPLOYED, true);
            }
         }

         Vec3 vel = this.getDeltaMovement();
         if (this.motor == null) {
            vel = vel.add(0.0, -0.05, 0.0).add(vel.normalize().scale(vel.lengthSqr() * -0.001));
            this.setDeltaMovement(vel);
         }
      }

      if (this.motor != null) {
         this.motor.tick(this);
      }

      if (this.guidance != null) {
         this.guidance.tick(this);
      }

      double nextPosX = this.getX() + this.getDeltaMovement().x();
      double nextPosY = this.getY() + this.getDeltaMovement().y();
      double nextPosZ = this.getZ() + this.getDeltaMovement().z();
      this.setPos(nextPosX, nextPosY, nextPosZ);
      if (!this.level().isClientSide()) {
         Ship launchShip = VSGameUtilsKt.getShipMountedTo(this);
         if (this.isPassenger()) {
            BlockPos virtualPos = this.blockPosition();
            if (launchShip != null) {
               virtualPos = this.getVehicle().blockPosition();
            }

            if (this.level().isLoaded(virtualPos)) {
               boolean currentlyPowered = this.level().hasNeighborSignal(virtualPos);
               if (currentlyPowered && !this.lastPowered) {
                  this.lastPowered = true;
                  if (this.guidance != null) {
                     if (this.guidance.canLaunch()) {
                        this.guidance.activateSeeker();
                        this.launch();
                     }

                     if (!this.guidance.isSeeking()) {
                        this.guidance.activateSeeker();
                     }
                  } else {
                     this.launch();
                  }
               }

               if (!currentlyPowered && this.lastPowered) {
                  this.lastPowered = false;
               }
            }
         }

         if (this.ticksSinceLaunch >= 1200 && (this.fuze == null || this.fuze.explodesOnExpiry())) {
            this.detonate(this.position());
         }

         CreateTypePacketHandler.getChannel()
            .send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new MissileVelocityPacket(this.getId(), this.getDeltaMovement()));
      }

      if (!this.isPassenger()) {
         this.ticksSinceLaunch++;
      } else {
         this.ticksSinceLaunch = 0;
      }

      if (this.level() instanceof ServerLevel slevel
         && !this.isRemoved()
         && this.isLongRange
         && !this.isPassenger()
         && (Boolean)CBCConfigs.SERVER.munitions.projectilesCanChunkload.get()) {
         ChunkPos cpos1 = new ChunkPos(this.blockPosition());
         RitchiesProjectileLib.queueForceLoad(slevel, cpos1.x, cpos1.z);
      }
   }

   public boolean launch(Vec3 dir, float boost) {
      Ship s = VSGameUtilsKt.getShipMountedTo(this);
      if (this.guidance != null) {
         this.guidance.launch(this);
      }

      this.stopRiding();
      if (s != null) {
         Utils.PosVel compensation = Utils.moveInWorld(this.position(), s, 3.0F);
         this.lookAt(Anchor.FEET, this.position().add(dir));
         this.setPos(compensation.pos());
         this.addDeltaMovement(compensation.vel());
      }

      if (this.motor != null) {
         this.motor.launch(this, dir, boost);
      } else {
         Vec3 vel = dir.scale(boost);
         this.setDeltaMovement(vel);
      }

      return true;
   }

   public boolean launch() {
      return this.launch(this.getLookAngle(), 0.0F);
   }

   @Nonnull
   public Vec3 disperseSquareErrorProbable50(@Nonnull Vec3 dir, double mils) {
      dir = dir.normalize();
      Vec3 a = UP;
      Vec3 b = LEFT;
      if (Math.abs(dir.dot(a)) > 0.707) {
         a = dir.cross(a);
      } else {
         a = dir.cross(b);
      }

      b = dir.cross(a);
      double ratio = Math.tan(mils / 2000.0);
      return dir.add(a.scale(ratio * this.random.nextGaussian())).add(b.scale(ratio * this.random.nextGaussian())).normalize();
   }

   protected void detonate(Vec3 hit) {
      if (!this.level().isClientSide) {
         if (this.warhead.detonate(this.level(), hit, this.getDeltaMovement().normalize())) {
            this.remove(RemovalReason.KILLED);
         }
      }
   }

   @Nonnull
   private Vec3 checkCollide(Vec3 p_20273_, AABB aabb) {
      List<VoxelShape> list = this.level().getEntityCollisions(this, aabb.expandTowards(p_20273_));
      return p_20273_.lengthSqr() == 0.0 ? p_20273_ : collideBoundingBox(null, p_20273_, aabb, this.level(), list);
   }

   @Nonnull
   public Vec3 collideOnlyShips(Vec3 movement, AABB aabb) {
      return movement.lengthSqr() == 0.0
         ? movement
         : EntityShipCollisionUtils.INSTANCE.adjustEntityMovementForShipCollisions(this, movement, aabb, this.level());
   }

   @Nonnull
   public Vec3 collideWithShips(Vec3 movement, AABB aabb) {
      movement = this.collideOnlyShips(movement, aabb);
      return this.checkCollide(movement, aabb);
   }

   public boolean hurt(DamageSource p_19946_, float p_19947_) {
      if (super.hurt(p_19946_, p_19947_) && this.ticksSinceLaunch > 5 && p_19947_ > 5.0F) {
         float hp = (Float)this.entityData.get(DATA_HEALTH_ID);
         this.entityData.set(DATA_HEALTH_ID, Mth.clamp(hp - p_19947_ + 5.0F, 0.0F, 20.0F));
         if (hp - p_19947_ + 5.0F <= 0.0F) {
            this.detonate(this.position());
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean isAlive() {
      return super.isAlive() && (Float)this.entityData.get(DATA_HEALTH_ID) > 0.0F;
   }

   public void updateHeading() {
      Vec3 weightedLook = this.getDeltaMovement().scale(0.33333334F).add(this.getLookAngle());
      this.lookAt(Anchor.FEET, this.position().add(weightedLook));
   }

   public boolean isDeployed() {
      return (Boolean)this.entityData.get(DATA_DEPLOYED);
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
      return !this.isRemoved();
   }

   protected float getEyeHeight(Pose p_19976_, EntityDimensions p_19977_) {
      return p_19977_.height * 0.5F;
   }

   private void loadDataFromID(String id) {
      MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(id);
      if (entry == null) {
         TallyhoMod.warn("Failed to load missile of id '{}'!", id);
      } else {
         this.missileId = id;
         if (this.guidance == null) {
            this.guidance = entry.getGuidance();
         }

         if (this.motor == null) {
            this.motor = entry.getMotor();
         }

         if (this.fuze == null) {
            this.fuze = entry.getFuze();
         }

         this.warhead = entry.warhead;
         this.hitboxScale = entry.getHitboxScale();
         this.isLongRange = entry.isLongRange();
         this.refreshDimensions();
      }
   }

   @Nonnull
   public EntityDimensions getDimensions(@Nonnull Pose p_19975_) {
      return super.getDimensions(p_19975_).scale(this.hitboxScale);
   }

   @Nonnull
   public AABB getBoundingBoxForCulling() {
      return super.getBoundingBoxForCulling().inflate(3.0);
   }

   public float getHitboxScale() {
      return this.hitboxScale;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_DEPLOYED, false);
      this.entityData.define(DATA_HEALTH_ID, 20.0F);
   }

   protected void readAdditionalSaveData(CompoundTag nbt) {
      if (nbt.contains("MissileId")) {
         this.loadDataFromID(nbt.getString("MissileId"));
      }

      if (this.guidance != null && nbt.contains("Guidance")) {
         this.guidance.deserialiseNBT(nbt.getCompound("Guidance"));
      }

      if (this.motor != null && nbt.contains("Motor")) {
         this.motor.deserialiseNBT(nbt.getCompound("Motor"));
      }

      this.lastPowered = nbt.getBoolean("LastPowered");
      this.modelItem = ItemStack.of(nbt.getCompound("ModelItem"));
   }

   protected void addAdditionalSaveData(CompoundTag nbt) {
      nbt.putString("MissileId", this.missileId);
      if (this.guidance != null) {
         nbt.put("Guidance", this.guidance.serialiseNBT());
      }

      if (this.motor != null) {
         nbt.put("Motor", this.motor.serialiseNBT());
      }

      nbt.putBoolean("LastPowered", this.lastPowered);
      nbt.put("ModelItem", this.modelItem.serializeNBT());
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public void writeSpawnData(FriendlyByteBuf buffer) {
      CompoundTag compound = new CompoundTag();
      this.addAdditionalSaveData(compound);
      buffer.writeNbt(compound);
   }

   public void readSpawnData(FriendlyByteBuf additionalData) {
      this.readAdditionalSaveData(additionalData.readNbt());
   }

   public int getTicksSinceLaunch() {
      return this.ticksSinceLaunch;
   }
}
