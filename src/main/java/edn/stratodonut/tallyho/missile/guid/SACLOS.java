package edn.stratodonut.tallyho.missile.guid;

import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
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

    private final SeekerProperties properties;
    @Nullable
    private SteerController controller;
    @Nullable
    private Vec3 steerOrigin;
    @Nullable
    private Vec3 steerDir;

    private static abstract class SteerController {
        public abstract boolean isAlive();

        public abstract void attemptRelink(Level level, Vec3 pos);

        public abstract Vec3 getSteerOrigin();

        public abstract Vec3 getSteerDir();

        public abstract void deserialiseNBT(@Nonnull CompoundTag nbt);

        @Nonnull
        public abstract CompoundTag serialiseNBT();
    }

    public enum ControlMedium {
        RADIO, WIRE, BEAM;

        public String toString() {
            return StringUtils.capitalize(super.toString());
        }
    }

    public static final class SeekerProperties {
        private final SACLOS.ControlMedium type;
        private final float max_G;
        private final int range;
        private final Vec3 offset;

        public SeekerProperties(SACLOS.ControlMedium type, float max_G, int range, Vec3 offset) {
            this.type = type;
            this.max_G = max_G;
            this.range = range;
            this.offset = offset;
        }

        public SACLOS.ControlMedium type() {
            return this.type;
        }

        public float max_G() {
            return this.max_G;
        }

        public int range() {
            return this.range;
        }

        public Vec3 offset() {
            return this.offset;
        }

        public void appendHoverText(List<Component> components) {
            components.add(Component.literal("Guidance: SACLOS").setStyle(Style.EMPTY
                    .withColor(ChatFormatting.AQUA)));
            components.add(Component.literal(String.format("Range: %sm", this.range)).setStyle(Style.EMPTY
                    .withColor(ChatFormatting.WHITE)));
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

        public void appendHoverText(List<Component> components) {
            this.properties.appendHoverText(components);
        }
    }

    private SACLOS(SeekerProperties properties) {
        this.properties = properties;
    }

    public void tick(MountedMissileEntity missile) {
        if (missile.level().isClientSide)
            return;
        if (!isSeeking() || missile.getTicksSinceLaunch() < 5)
            return;
        if (this.controller != null)
            if (!this.controller.isAlive()) {
                this.controller.attemptRelink(missile.level(), missile.position());
            } else {
                this.steerOrigin = this.controller.getSteerOrigin();
                this.steerDir = this.controller.getSteerDir();
            }
        if (this.steerOrigin == null || this.steerDir == null)
            return;
        Vec3 perpendicularOffset = this.steerOrigin.subtract(missile.position());
        if (perpendicularOffset.lengthSqr() > (this.properties.range * this.properties.range))
            return;
        Vec3 steerTarget = missile.position().add(perpendicularOffset.subtract(this.steerDir.scale(perpendicularOffset.dot(this.steerDir)))).add(this.steerDir.scale(10.0D)).add(this.properties.offset);
        missile.setDeltaMovement(ProportionalGuidance(steerTarget, Vec3.ZERO, missile.position(), missile.getDeltaMovement(), 3.0F, this.properties.max_G));
    }

    public boolean launch(MountedMissileEntity missile) {
        activateSeeker();
        Level level = missile.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.controller == null) {
                List<CameraEntity2> cams = serverLevel.getEntitiesOfClass(CameraEntity2.class, missile
                        .getBoundingBox().inflate(10.0D));
                for (CameraEntity2 cam : cams) {
                    if ((cam instanceof edn.stratodonut.tallyho.camera.entity.CannonScopeEntity || cam instanceof edn.stratodonut.tallyho.camera.entity.ThermalsEntity) &&
                        VSGameUtilsKt.getShipMountedTo((Entity)cam) == VSGameUtilsKt.getShipMountedTo((Entity)missile)) {
                        setCameraController(cam);
                        break;
                    }
                }
            }
        }
        return true;
    }

    public void setCameraController(final CameraEntity2 controllingEntity) {
        this.controller = new SteerController() {
            @Nonnull
            WeakReference<CameraEntity2> entity = new WeakReference<>(controllingEntity);
            final UUID entityUUID = controllingEntity.getUUID();

            public boolean isAlive() {
                Entity e = (Entity)this.entity.get();
                boolean b = (e != null);
                if (b) {
                    b = (e.isAlive() && !e.isRemoved());
                    if (!b)
                        this.entity = new WeakReference<>(null);
                }
                return b;
            }

            public void attemptRelink(Level level, Vec3 pos) {
                AABB a = new AABB(pos.add(-10.0D, -10.0D, -10.0D), pos.add(10.0D, 10.0D, 10.0D));
                List<CameraEntity2> list = level.getEntitiesOfClass(CameraEntity2.class, a);
                for (CameraEntity2 e : list) {
                    if (Objects.equals(e.getUUID(), this.entityUUID)) {
                        this.entity = new WeakReference<>(e);
                        return;
                    }
                }
            }

            public Vec3 getSteerOrigin() {
                if (this.entity.get() == null)
                    return null;
                return ((CameraEntity2)this.entity.get()).getEyePosition();
            }

            public Vec3 getSteerDir() {
                if (this.entity.get() == null)
                    return null;
                return ((CameraEntity2)this.entity.get()).getLookAngle();
            }

            public void deserialiseNBT(@Nonnull CompoundTag nbt) {}

            @Nonnull
            public CompoundTag serialiseNBT() {
                return new CompoundTag();
            }
        };
    }

    @Nonnull
    public CompoundTag serialiseNBT() {
        return super.serialiseNBT();
    }

    public void deserialiseNBT(@Nonnull CompoundTag nbt) {
        super.deserialiseNBT(nbt);
    }
}