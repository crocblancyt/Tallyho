package edn.stratodonut.tallyho.missile.guid;

import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.CannonScopeEntity;
import edn.stratodonut.tallyho.camera.entity.ThermalsEntity;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.GuidanceComponent;
import edn.stratodonut.tallyho.missile.IGuidanceFactory;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class SACLOS extends GuidanceComponent {
   public final int ARMING_TICKS = 5;
   private final SACLOS.SeekerProperties properties;
   @Nullable
   private SACLOS.SteerController controller;
   @Nullable
   private Vec3 steerOrigin;
   @Nullable
   private Vec3 steerDir;

   private SACLOS(SACLOS.SeekerProperties properties) {
      this.properties = properties;
   }

   @Override
   public void tick(MountedMissileEntity missile) {
      if (!missile.level().isClientSide) {
         if (this.isSeeking() && missile.getTicksSinceLaunch() >= 5) {
            if (this.controller != null) {
               if (!this.controller.isAlive()) {
                  this.controller.attemptRelink(missile.level(), missile.position());
               } else {
                  this.steerOrigin = this.controller.getSteerOrigin();
                  this.steerDir = this.controller.getSteerDir();
               }
            }

            if (this.steerOrigin != null && this.steerDir != null) {
               Vec3 perpendicularOffset = this.steerOrigin.subtract(missile.position());
               if (!(perpendicularOffset.lengthSqr() > this.properties.range * this.properties.range)) {
                  Vec3 steerTarget = missile.position()
                     .add(perpendicularOffset.subtract(this.steerDir.scale(perpendicularOffset.dot(this.steerDir))))
                     .add(this.steerDir.scale(10.0))
                     .add(this.properties.offset);
                  missile.setDeltaMovement(
                     ProportionalGuidance(steerTarget, Vec3.ZERO, missile.position(), missile.getDeltaMovement(), 3.0F, this.properties.max_G)
                  );
               }
            }
         }
      }
   }

   @Override
   public boolean launch(MountedMissileEntity missile) {
      this.activateSeeker();
      if (missile.level() instanceof ServerLevel serverLevel && this.controller == null) {
         for (CameraEntity2 cam : serverLevel.getEntitiesOfClass(CameraEntity2.class, missile.getBoundingBox().inflate(10.0))) {
            if ((cam instanceof CannonScopeEntity || cam instanceof ThermalsEntity)
               && VSGameUtilsKt.getShipMountedTo(cam) == VSGameUtilsKt.getShipMountedTo(missile)) {
               this.setCameraController(cam);
               break;
            }
         }
      }

      return true;
   }

   public void setCameraController(final CameraEntity2 controllingEntity) {
      this.controller = new SACLOS.SteerController() {
         @Nonnull
         WeakReference<CameraEntity2> entity = new WeakReference<>(controllingEntity);
         final UUID entityUUID = controllingEntity.getUUID();

         @Override
         public boolean isAlive() {
            Entity e = this.entity.get();
            boolean b = e != null;
            if (b) {
               b = e.isAlive() && !e.isRemoved();
               if (!b) {
                  this.entity = new WeakReference<>(null);
               }
            }

            return b;
         }

         @Override
         public void attemptRelink(Level level, Vec3 pos) {
            AABB a = new AABB(pos.subtract(10.0, 10.0, 10.0), pos.add(10.0, 10.0, 10.0));

            for (CameraEntity2 e : level.getEntitiesOfClass(CameraEntity2.class, a)) {
               if (Objects.equals(e.getUUID(), this.entityUUID)) {
                  this.entity = new WeakReference<>(e);
                  return;
               }
            }
         }

         @Override
         public Vec3 getSteerOrigin() {
            return this.entity.get() == null ? null : this.entity.get().getEyePosition();
         }

         @Override
         public Vec3 getSteerDir() {
            return this.entity.get() == null ? null : this.entity.get().getLookAngle();
         }

         @Override
         public void deserialiseNBT(@Nonnull CompoundTag nbt) {
         }

         @Nonnull
         @Override
         public CompoundTag serialiseNBT() {
            return null;
         }
      };
   }

   @Nonnull
   @Override
   public CompoundTag serialiseNBT() {
      return super.serialiseNBT();
   }

   @Override
   public void deserialiseNBT(@Nonnull CompoundTag nbt) {
      super.deserialiseNBT(nbt);
   }

   public enum ControlMedium {
      RADIO,
      WIRE,
      BEAM;

      @Override
      public String toString() {
         return StringUtils.capitalize(super.toString());
      }
   }

   public static class Factory implements IGuidanceFactory<SACLOS> {
      protected final SACLOS.SeekerProperties properties;

      public Factory(SACLOS.ControlMedium type, float max_G, int range, Vec3 offset) {
         this.properties = new SACLOS.SeekerProperties(type, max_G, range, offset);
      }

      public Factory(SACLOS.ControlMedium type, float max_G, int range) {
         this.properties = new SACLOS.SeekerProperties(type, max_G, range, Vec3.ZERO);
      }

      public SACLOS create() {
         return new SACLOS(this.properties);
      }

      @Override
      public void appendHoverText(List<Component> components) {
         this.properties.appendHoverText(components);
      }
   }

   public record SeekerProperties(SACLOS.ControlMedium type, float max_G, int range, Vec3 offset) {
      public void appendHoverText(List<Component> components) {
         components.add(Component.literal("Guidance: SACLOS").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
         components.add(Component.literal(String.format("Range: %sm", this.range)).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
      }
   }

   private abstract static class SteerController {
      public abstract boolean isAlive();

      public abstract void attemptRelink(Level var1, Vec3 var2);

      public abstract Vec3 getSteerOrigin();

      public abstract Vec3 getSteerDir();

      public abstract void deserialiseNBT(@Nonnull CompoundTag var1);

      @Nonnull
      public abstract CompoundTag serialiseNBT();
   }
}
