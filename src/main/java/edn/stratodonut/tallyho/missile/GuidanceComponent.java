package edn.stratodonut.tallyho.missile;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public abstract class GuidanceComponent {
   protected Random random = new Random();
   protected boolean isSeeking = false;

   public abstract void tick(MountedMissileEntity var1);

   public void activateSeeker() {
      this.isSeeking = true;
   }

   public abstract boolean launch(MountedMissileEntity var1);

   public void setCode(String c) {
   }

   public boolean isSeeking() {
      return this.isSeeking;
   }

   public boolean canLaunch() {
      return true;
   }

   public void deserialiseNBT(@Nonnull CompoundTag nbt) {
      this.isSeeking = nbt.getBoolean("Hot");
   }

   @Nonnull
   public CompoundTag serialiseNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.putBoolean("Hot", this.isSeeking);
      return nbt;
   }

   public static Vec3 ProportionalGuidance(Vec3 targetPos, Vec3 targetVel, Vec3 missilePos, Vec3 missileVel, float coefficient, float maxG) {
      Vec3 rel_pos = targetPos.subtract(missilePos);
      Vec3 v_r = targetVel.scale(0.05).subtract(missileVel);
      Vec3 omega_r = rel_pos.cross(v_r).scale(1.0 / rel_pos.dot(rel_pos));
      Vec3 accel = missileVel.normalize().cross(omega_r).scale(-coefficient * v_r.length());
      return missileVel.add(accel.normalize().scale(Math.clamp(0.0, maxG, accel.length())));
   }

   @Nonnull
   public static Optional<Ship> tryLock(Level level, Vec3 forward, Vec3 position, @Nullable AABB aabb, int fov, int range) {
      return tryLock(level, forward, position, aabb, fov, range, (a, b) -> {
         Vector3dc pos = VectorConversionsMCKt.toJOML(position);
         double a_dist = a.getTransform().getPositionInWorld().distance(pos);
         double a_size = a.getShipAABB() == null ? 1.0 : 1.0 / a.getShipAABB().extent(new Vector3d()).length();
         double b_dist = b.getTransform().getPositionInWorld().distance(pos);
         double b_size = b.getShipAABB() == null ? 1.0 : 1.0 / b.getShipAABB().extent(new Vector3d()).length();
         return (int)(a_dist * a_size - b_dist * b_size);
      }, s -> true);
   }

   @Nonnull
   public static Optional<Ship> tryLock(
      Level level, Vec3 forward, Vec3 position, @Nullable AABB aabb, int fov, int range, Comparator<Ship> heuristic, Function<Ship, Boolean> filter
   ) {
      if (aabb == null) {
         aabb = new AABB(BlockPos.containing(position)).inflate(range);
      }

      Iterable<Ship> ships = VSGameUtilsKt.getShipsIntersecting(level, aabb);
      return StreamSupport.stream(ships.spliterator(), false).filter(ship -> {
         Vec3 rel_pos = VectorConversionsMCKt.toMinecraft(ship.getTransform().getPositionInWorld()).subtract(position);
         return rel_pos.normalize().dot(forward) > java.lang.Math.cos(Math.toRadians(fov)) && rel_pos.length() > 16.0 && filter.apply(ship);
      }).min(heuristic);
   }
}
