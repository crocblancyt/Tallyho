package edn.stratodonut.tallyho.missile.guid;

import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.GuidanceComponent;
import edn.stratodonut.tallyho.missile.IGuidanceFactory;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class LaserSeeker extends GuidanceComponent {
   public final int ARMING_TICKS = 5;
   private final LaserSeeker.SeekerProperties properties;
   @Nullable
   private String code;
   @Nonnull
   protected WeakReference<LaserPointEntity> target = new WeakReference<>(null);

   private LaserSeeker(LaserSeeker.SeekerProperties properties) {
      this.properties = properties;
   }

   @Override
   public void tick(MountedMissileEntity missile) {
      if (!missile.level().isClientSide) {
         if (this.isSeeking()) {
            LaserPointEntity t = this.target.get();
            Vec3 lookDir = missile.getLookAngle();
            Vec3 v_m = missile.getDeltaMovement();
            if (missile.getTicksSinceLaunch() > 5) {
               lookDir = v_m.normalize();
            }

            int fov = this.properties.seeker_fov + (missile.isPassenger() ? 0 : this.properties.gimbal_fov);
            if (t == null) {
               Optional<LaserPointEntity> ship = tryLockLaser(missile.level(), lookDir, missile.position(), null, fov, this.properties.seeker_range, this.code);
               ship.ifPresent(value -> this.target = new WeakReference<>(value));
            } else {
               Vec3 rel_pos = t.position().subtract(missile.position());
               Vec3 v_r = v_m.reverse();
               Vec3 omega_r = rel_pos.cross(v_r).scale(1.0 / rel_pos.dot(rel_pos));
               if (rel_pos.normalize().dot(lookDir) < Math.cos(Math.toRadians(fov))
                  || omega_r.length() > this.properties.seeker_fov_rate
                  || !t.isAlive()
                  || t.isRemoved()) {
                  this.target = new WeakReference<>(null);
                  return;
               }

               if (missile.getTicksSinceLaunch() > 5) {
                  missile.setDeltaMovement(
                     GuidanceComponent.ProportionalGuidance(t.position(), Vec3.ZERO, missile.position(), v_m, 3.0F, this.properties.max_G)
                  );
               }
            }
         }
      }
   }

   public static @NonNull Optional<LaserPointEntity> tryLockLaser(
      Level level, Vec3 forward, Vec3 position, @Nullable AABB aabb, int fov, int range, String code
   ) {
      if (aabb == null) {
         aabb = new AABB(BlockPos.containing(position)).move(forward.scale(range / 2.0)).inflate(range);
      }

      Iterable<LaserPointEntity> points = level.getEntitiesOfClass(LaserPointEntity.class, aabb);
      return StreamSupport.stream(points.spliterator(), false).filter(lpe -> {
         Vec3 rel_pos = lpe.position().subtract(position);
         return lpe.getGroundTicks() > 0 && rel_pos.normalize().dot(forward) > Math.cos(org.joml.Math.toRadians(fov)) && rel_pos.lengthSqr() > 255.0;
      }).min((a, b) -> {
         boolean isCodedA = a.match(code);
         boolean isCodedB = b.match(code);
         if (isCodedA && !isCodedB) {
            return -32767;
         } else {
            return !isCodedA && isCodedB ? 32767 : Comparator.<LaserPointEntity>comparingInt(lpe -> lpe.tickCount).compare(a, b);
         }
      });
   }

   @Override
   public boolean launch(MountedMissileEntity e) {
      Ship s = VSGameUtilsKt.getShipMountedTo(e);
      if (s != null && s.getSlug() != null && this.code == null) {
         this.code = s.getSlug();
      }

      this.activateSeeker();
      return true;
   }

   @Override
   public void setCode(@Nonnull String code) {
      this.code = code;
   }

   @Override
   public void deserialiseNBT(@Nonnull CompoundTag nbt) {
      super.deserialiseNBT(nbt);
      if (nbt.contains("Code")) {
         this.code = nbt.getString("Code");
      }
   }

   @Nonnull
   @Override
   public CompoundTag serialiseNBT() {
      CompoundTag nbt = super.serialiseNBT();
      if (this.code != null) {
         nbt.putString("Code", this.code);
      }

      return nbt;
   }

   public static class Factory implements IGuidanceFactory<LaserSeeker> {
      protected final LaserSeeker.SeekerProperties properties;

      public Factory(int seeker_fov, int gimbal_fov, double seeker_fov_rate, float max_G, int seeker_range) {
         this.properties = new LaserSeeker.SeekerProperties(seeker_fov, gimbal_fov, seeker_fov_rate, max_G, seeker_range);
      }

      public LaserSeeker create() {
         return new LaserSeeker(this.properties);
      }

      @Override
      public void appendHoverText(List<Component> components) {
         this.properties.appendHoverText(components);
      }
   }

   public record SeekerProperties(int seeker_fov, int gimbal_fov, double seeker_fov_rate, float max_G, int seeker_range) {
      public void appendHoverText(List<Component> components) {
         components.add(Component.literal("Guidance: SALH").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
         components.add(
            Component.literal(String.format("Fov: %s°/%s°", this.seeker_fov, this.gimbal_fov)).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
         );
         components.add(Component.literal(String.format("Seeker Range: %sm", this.seeker_range)).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
      }
   }
}
