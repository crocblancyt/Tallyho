package edn.stratodonut.tallyho.missile.tracker;

import java.util.Hashtable;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class RadEmissionTracker {
   private static final Hashtable<Level, Hashtable<BlockPos, RadEmissionTracker.Emitter>> emissions = new Hashtable<>();

   public static void emit(Level level, BlockPos pos, int power) {
      emissions.computeIfAbsent(level, k -> new Hashtable<>())
         .compute(pos, (k, v) -> v == null ? RadEmissionTracker.Emitter.Emit(level, pos, power) : v.update(power));
   }

   public static void tick(Level level) {
      Hashtable<BlockPos, RadEmissionTracker.Emitter> r = emissions.computeIfAbsent(level, k -> new Hashtable<>());
      r.forEach((k, v) -> v.tick());
      r.values().removeIf(v -> v.ttl <= 0);
   }

   @Nonnull
   public static Stream<RadEmissionTracker.Emitter> find(Level level, Vec3 pos, float sensitivity, Vec3 dir, float fov) {
      return emissions.computeIfAbsent(level, k -> new Hashtable<>()).values().stream().filter(e -> {
         Vec3 t = e.getPos();
         return t.distanceTo(pos) <= e.power * sensitivity && t.subtract(pos).dot(dir) > Math.cos(Math.toRadians(fov / 2.0F));
      });
   }

   public static class Emitter {
      private final Level level;
      private final BlockPos rootPos;
      private Vec3 worldPos;
      @Nullable
      private Long shipId;
      private int power;
      private RadEmissionTracker.EmitterType type = RadEmissionTracker.EmitterType.STATIC;
      private int ttl = 60;

      public static RadEmissionTracker.Emitter Emit(Level level, BlockPos pos, int power) {
         RadEmissionTracker.Emitter emitter = new RadEmissionTracker.Emitter(level, pos, power);
         emitter.worldPos = Vec3.atCenterOf(pos);
         Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
         if (s != null) {
            emitter.shipId = s.getId();
            emitter.worldPos = VectorConversionsMCKt.toMinecraft(VSGameUtilsKt.toWorldCoordinates(s, pos));
            if (s.getVelocity().length() > 35.0) {
               emitter.type = RadEmissionTracker.EmitterType.FLYING;
            } else {
               emitter.type = RadEmissionTracker.EmitterType.VEHICLE;
            }
         }

         return emitter;
      }

      private Emitter(Level level, BlockPos pos, int power) {
         this.level = level;
         this.rootPos = pos;
         this.power = power;
      }

      @Nonnull
      public RadEmissionTracker.Emitter withOffset(double x, double y, double z) {
         RadEmissionTracker.Emitter e = new RadEmissionTracker.Emitter(this.level, this.rootPos, this.power);
         e.worldPos = this.worldPos.add(x, y, z);
         e.shipId = this.shipId;
         e.type = this.type;
         e.ttl = this.ttl;
         return e;
      }

      @Nullable
      public RadEmissionTracker.Emitter update(int newPower) {
         if (newPower >= this.power) {
            this.power = newPower;
            this.ttl = 60;
            if (this.shipId != null) {
               Ship s = VSGameUtilsKt.getAllShips(this.level).getById(this.shipId);
               if (s == null) {
                  return null;
               }

               this.worldPos = VectorConversionsMCKt.toMinecraft(VSGameUtilsKt.toWorldCoordinates(s, this.rootPos));
            }
         }

         return this;
      }

      @Nonnull
      public RadEmissionTracker.Emitter tick() {
         this.ttl--;
         return this;
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof RadEmissionTracker.Emitter e2) ? false : Objects.equals(this.shipId, e2.shipId) && this.rootPos == e2.rootPos;
      }

      public int getPower() {
         return this.power;
      }

      public Vec3 getPos() {
         return this.worldPos;
      }

      public RadEmissionTracker.EmitterType getType() {
         return this.type;
      }

      @Override
      public String toString() {
         return "Emitter @ " + this.rootPos + " on " + this.shipId + " to " + this.worldPos + " power " + this.power + " is " + this.type;
      }
   }

   public enum EmitterType {
      STATIC,
      VEHICLE,
      FLYING;
   }
}
