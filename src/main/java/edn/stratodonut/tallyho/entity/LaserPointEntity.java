package edn.stratodonut.tallyho.entity;

import edn.stratodonut.tallyho.AllEntities;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class LaserPointEntity extends Entity implements IEntityAdditionalSpawnData {
   public static final int DWELL_TICKS = 20;
   @Nullable
   private String code = "";
   private int groundTicks;

   public LaserPointEntity(EntityType<? extends Entity> p_37248_, Level p_37249_) {
      super(p_37248_, p_37249_);
   }

   public static void shoot(Level level, Vec3 pos, Vec3 dir, @Nullable String code) {
      LaserPointEntity lpe = new LaserPointEntity((EntityType<? extends Entity>)AllEntities.LASER_POINT_ENTITY.get(), level);
      lpe.setPos(pos);
      lpe.setDeltaMovement(dir.scale(40.0));
      lpe.code = code;
      level.addFreshEntity(lpe);
   }

   public static void shootWithPlatformVelocity(Level level, Vec3 pos, Vec3 dir, Vec3 vel, @Nullable String code) {
      LaserPointEntity lpe = new LaserPointEntity((EntityType<? extends Entity>)AllEntities.LASER_POINT_ENTITY.get(), level);
      lpe.setPos(pos);
      lpe.setDeltaMovement(dir.scale(40.0).add(vel.scale(0.05)));
      lpe.code = code;
      level.addFreshEntity(lpe);
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.getGroundTicks() == 0) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            BlockHitResult result = this.level().clip(new ClipContext(startVec, endVec, Block.COLLIDER, Fluid.NONE, this));
            if (result.getType() != Type.MISS) {
               endVec = result.getLocation();
               this.incGroundTicks();
            }

            if (this.tickCount > 20) {
               this.remove(RemovalReason.KILLED);
            }

            this.setPos(endVec);
         } else {
            if (this.getGroundTicks() > 20) {
               this.remove(RemovalReason.KILLED);
            }

            this.incGroundTicks();
         }
      }
   }

   @Nonnull
   public static Optional<LaserPointEntity> find(Level level, AABB aabb, @Nonnull String code) {
      List<LaserPointEntity> results = level.getEntitiesOfClass(LaserPointEntity.class, aabb);
      return results.stream().filter(lpe -> lpe.getGroundTicks() > 0 && lpe.match(code)).min(Comparator.comparingInt(lpe -> lpe.tickCount));
   }

   @Nonnull
   public static Optional<LaserPointEntity> find(Level level, AABB aabb, Predicate<LaserPointEntity> predicate, Comparator<LaserPointEntity> comparator) {
      List<LaserPointEntity> results = level.getEntitiesOfClass(LaserPointEntity.class, aabb);
      return results.stream().filter(predicate).min(comparator);
   }

   @Nonnull
   public static Optional<LaserPointEntity> findUncoded(Level level, Vec3 pos, Vec3 dir, float range, double fov) {
      double fovD = Math.cos(Math.toRadians(fov));
      List<LaserPointEntity> results = level.getEntitiesOfClass(LaserPointEntity.class, new AABB(pos, pos.add(dir.scale(range))).inflate(16.0));
      return results.stream()
         .filter(lpe -> lpe.getGroundTicks() > 0 && lpe.getCode() == null && lpe.position().subtract(pos).dot(dir) > fovD)
         .min(Comparator.comparingInt(lpe -> lpe.tickCount));
   }

   private void incGroundTicks() {
      this.groundTicks++;
   }

   public int getGroundTicks() {
      return this.groundTicks;
   }

   public boolean match(@Nonnull String code) {
      return this.code != null && Objects.equals(this.code, code);
   }

   @Nullable
   public String getCode() {
      return this.code;
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag nbt) {
      if (nbt.contains("Code")) {
         this.code = nbt.getString("Code");
      }
   }

   protected void addAdditionalSaveData(CompoundTag nbt) {
      if (this.code != null) {
         nbt.putString("Code", this.code);
      }
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
      CompoundTag nbt = additionalData.readNbt();
      if (nbt != null) {
         this.readAdditionalSaveData(nbt);
      }
   }
}
