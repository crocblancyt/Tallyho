package edn.stratodonut.tallyho.missile;

import java.lang.ref.WeakReference;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public abstract class Target<T> {
   static final AABB DEFAULT_AABB = AABB.unitCubeFromLowerCorner(Vec3.ZERO);

   public abstract Vec3 position();

   public abstract Vec3 velocity();

   public abstract AABB boundingBox();

   public abstract boolean isAlive();

   @Nullable
   public abstract T get();

   public static class EntityTarget extends Target<Entity> {
      WeakReference<Entity> e;
      int id;

      public EntityTarget(Entity e) {
         this.e = new WeakReference<>(e);
         this.id = e.getId();
      }

      @Override
      public Vec3 position() {
         return !this.isAlive() ? Vec3.ZERO : this.e.get().position();
      }

      @Override
      public Vec3 velocity() {
         return !this.isAlive() ? Vec3.ZERO : this.e.get().getDeltaMovement().scale(20.0);
      }

      @Override
      public AABB boundingBox() {
         return !this.isAlive() ? DEFAULT_AABB : this.e.get().getBoundingBox();
      }

      @Nullable
      public Entity get() {
         return this.e.get();
      }

      @Override
      public boolean isAlive() {
         return this.e.get() != null;
      }

      @Override
      public boolean equals(Object obj) {
         return obj == this || obj instanceof Target.EntityTarget et && et.id == this.id;
      }
   }

   public static class ShipTarget extends Target<Ship> {
      WeakReference<Ship> s;
      long id;

      public ShipTarget(Ship s) {
         this.s = new WeakReference<>(s);
         this.id = s.getId();
      }

      @Override
      public Vec3 position() {
         return !this.isAlive() ? Vec3.ZERO : VectorConversionsMCKt.toMinecraft(this.s.get().getTransform().getPositionInWorld());
      }

      @Override
      public Vec3 velocity() {
         return !this.isAlive() ? Vec3.ZERO : VectorConversionsMCKt.toMinecraft(this.s.get().getVelocity());
      }

      @Override
      public AABB boundingBox() {
         return !this.isAlive() ? DEFAULT_AABB : VectorConversionsMCKt.toMinecraft(this.s.get().getWorldAABB());
      }

      @Nullable
      public Ship get() {
         return this.s.get();
      }

      @Override
      public boolean isAlive() {
         return this.s.get() != null;
      }

      @Override
      public boolean equals(Object obj) {
         return obj == this || obj instanceof Target.ShipTarget st && st.id == this.id;
      }
   }
}
