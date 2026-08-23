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

    public static class ShipTarget extends Target<Ship> {
        WeakReference<Ship> s;
        long id;

        public ShipTarget(Ship s) {
            this.s = new WeakReference<>(s);
            this.id = s.getId();
        }

        public Vec3 position() {
            if (!isAlive())
                return Vec3.ZERO;
            return VectorConversionsMCKt.toMinecraft(this.s.get().getTransform().getPositionInWorld());
        }

        public Vec3 velocity() {
            if (!isAlive())
                return Vec3.ZERO;
            return VectorConversionsMCKt.toMinecraft(this.s.get().getVelocity());
        }

        public AABB boundingBox() {
            if (!isAlive())
                return DEFAULT_AABB;
            return VectorConversionsMCKt.toMinecraft(this.s.get().getWorldAABB());
        }

        @Nullable
        public Ship get() {
            return this.s.get();
        }

        public boolean isAlive() {
            return (this.s.get() != null);
        }

        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof ShipTarget) {
                ShipTarget st = (ShipTarget)obj;
                return st.id == this.id;
            }
            return false;
        }
    }

    public static class EntityTarget extends Target<Entity> {
        WeakReference<Entity> e;
        int id;

        public EntityTarget(Entity e) {
            this.e = new WeakReference<>(e);
            this.id = e.getId();
        }

        public Vec3 position() {
            if (!isAlive())
                return Vec3.ZERO;
            return this.e.get().position();
        }

        public Vec3 velocity() {
            if (!isAlive())
                return Vec3.ZERO;
            return this.e.get().getDeltaMovement().scale(20.0D);
        }

        public AABB boundingBox() {
            if (!isAlive())
                return DEFAULT_AABB;
            return this.e.get().getBoundingBox();
        }

        @Nullable
        public Entity get() {
            return this.e.get();
        }

        public boolean isAlive() {
            return (this.e.get() != null);
        }

        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof EntityTarget) {
                EntityTarget et = (EntityTarget)obj;
                return et.id == this.id;
            }
            return false;
        }
    }
}