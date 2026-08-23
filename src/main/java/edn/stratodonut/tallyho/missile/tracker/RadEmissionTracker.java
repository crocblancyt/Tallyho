package edn.stratodonut.tallyho.missile.tracker;

import java.util.Hashtable;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class RadEmissionTracker {
    private static final Hashtable<Level, Hashtable<BlockPos, Emitter>> emissions = new Hashtable<>();

    public enum EmitterType {
        STATIC, VEHICLE, FLYING;
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

        public static Emitter Emit(Level level, BlockPos pos, int power) {
            Emitter emitter = new Emitter(level, pos, power);
            emitter.worldPos = Vec3.atCenterOf((Vec3i)pos);
            Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
            if (s != null) {
                emitter.shipId = s.getId();
                emitter.worldPos = VectorConversionsMCKt.toMinecraft((Vector3dc)VSGameUtilsKt.toWorldCoordinates(s, pos));
                if (s.getVelocity().length() > 35.0D) {
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
        public Emitter withOffset(double x, double y, double z) {
            Emitter e = new Emitter(this.level, this.rootPos, this.power);
            e.worldPos = this.worldPos.add(x, y, z);
            e.shipId = this.shipId;
            e.type = this.type;
            e.ttl = this.ttl;
            return e;
        }

        @Nullable
        public Emitter update(int newPower) {
            if (newPower >= this.power) {
                this.power = newPower;
                this.ttl = 60;
                if (this.shipId != null) {
                    Ship s = VSGameUtilsKt.getAllShips(this.level).getById(this.shipId);
                    if (s != null) {
                        this.worldPos = VectorConversionsMCKt.toMinecraft((Vector3dc)VSGameUtilsKt.toWorldCoordinates(s, this.rootPos));
                    } else {
                        return null;
                    }
                }
            }
            return this;
        }

        @Nonnull
        public Emitter tick() {
            this.ttl--;
            return this;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Emitter) {
                Emitter e2 = (Emitter)obj;
                return (Objects.equals(this.shipId, e2.shipId) && this.rootPos == e2.rootPos);
            }
            return false;
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

        public String toString() {
            return "Emitter @ " + this.rootPos + " on " + this.shipId + " to " + this.worldPos + " power " + this.power + " is " + this.type;
        }
    }

    public static void emit(Level level, BlockPos pos, int power) {
        emissions.computeIfAbsent(level, k -> new Hashtable<>())
            .compute(pos, (k, v) -> (v == null) ? Emitter.Emit(level, pos, power) : v.update(power));
    }

    public static void tick(Level level) {
        Hashtable<BlockPos, Emitter> r = emissions.computeIfAbsent(level, k -> new Hashtable<>());
        r.forEach((k, v) -> v.tick());
        r.values().removeIf(v -> (v.ttl <= 0));
    }

    @Nonnull
    public static Stream<Emitter> find(Level level, Vec3 pos, float sensitivity, Vec3 dir, float fov) {
        return emissions.computeIfAbsent(level, k -> new Hashtable<>()).values().stream()
            .filter(e -> {
                Vec3 t = e.getPos();
                return (t.distanceToSqr(pos) <= (e.power * sensitivity) && t.subtract(pos).normalize().dot(dir) > Math.cos(Math.toRadians((fov / 2.0F))));
            });
    }
}