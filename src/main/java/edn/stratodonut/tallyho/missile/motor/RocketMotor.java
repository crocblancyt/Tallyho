package edn.stratodonut.tallyho.missile.motor;

import edn.stratodonut.tallyho.AllSounds;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.IMotorFactory;
import edn.stratodonut.tallyho.missile.MotorComponent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.effects.particles.smoke.TrailSmokeParticleData;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile;

public class RocketMotor extends MotorComponent {
    private final MotorProperties properties;
    public final float renderOffset;

    public static final class MotorProperties {
        private final float boost;
        private final int burn_time;
        private final boolean smokeless;
        private final double dispersion;

        public MotorProperties(float boost, int burn_time, boolean smokeless, double dispersion) {
            this.boost = boost;
            this.burn_time = burn_time;
            this.smokeless = smokeless;
            this.dispersion = dispersion;
        }

        public float boost() {
            return this.boost;
        }

        public int burn_time() {
            return this.burn_time;
        }

        public boolean smokeless() {
            return this.smokeless;
        }

        public double dispersion() {
            return this.dispersion;
        }

        public void appendHoverText(List<Component> components) {
            if (this.burn_time > 0)
                components.add(Component.literal(String.format("Rocket Boost: %sm/s", this.boost * 20.0F * this.burn_time)).setStyle(Style.EMPTY
                        .withColor(ChatFormatting.YELLOW)));
        }
    }

    public static class Factory implements IMotorFactory<RocketMotor> {
        private final RocketMotor.MotorProperties properties;
        private float renderOffset = 0.0F;

        public Factory(float boost, int burn_time, boolean smokeless) {
            this.properties = new RocketMotor.MotorProperties(boost, burn_time, smokeless, 0.01875D);
        }

        public Factory(float boost, int burn_time, boolean smokeless, double dispersion) {
            this.properties = new RocketMotor.MotorProperties(boost, burn_time, smokeless, dispersion);
        }

        public Factory offsetRender(float offset) {
            this.renderOffset = offset;
            return this;
        }

        public RocketMotor create() {
            return new RocketMotor(this.properties, this.renderOffset);
        }

        public void appendHoverText(List<Component> list) {
            this.properties.appendHoverText(list);
        }
    }

    public RocketMotor(MotorProperties props, float offset) {
        this.properties = props;
        this.renderOffset = offset;
    }

    public void tick(MountedMissileEntity e) {
        Vec3 dir = e.getLookAngle();
        if (!e.level().isClientSide &&
            e.getTicksSinceLaunch() < this.properties.burn_time())
            e.setDeltaMovement(e.disperseSquareErrorProbable50(dir, this.properties.dispersion).scale(this.properties.boost));
        if (e.getVehicle() == null &&
            e.level().isClientSide) {
            float vertOffset = e.getHitboxScale() * 0.5F;
            if (e.getTicksSinceLaunch() < this.properties.burn_time() && !this.properties.smokeless)
                emitSmoke(dir, (Entity)e);
        }
    }

    public float getBurnTime() {
        return this.properties.burn_time;
    }

    public boolean launch(MountedMissileEntity e, Vec3 dir, float boost) {
        Vec3 vel = dir.scale((this.properties.boost() + boost));
        e.setDeltaMovement(vel);
        e.playSound(AllSounds.MISSILE_LAUNCH.get(), 3.0F, 1.0F);
        emitSmoke(vel, e);
        return true;
    }

    public void emitSmoke(Vec3 vel, Entity e) {
        AbstractBigCannonProjectile.TrailType trailType = (AbstractBigCannonProjectile.TrailType)CBCConfigs.SERVER.munitions.bigCannonTrailType.get();
        if (trailType != AbstractBigCannonProjectile.TrailType.NONE) {
            int lifetime = (trailType == AbstractBigCannonProjectile.TrailType.SHORT) ? 100 : (280 + e.level().random.nextInt(50));
            TrailSmokeParticleData trailSmokeParticleData = new TrailSmokeParticleData(lifetime);
            double particleCount = Mth.clamp(vel.length(), 10.0D, 64.0D);
            double sep = 1.0D / particleCount;
            for (int i = 0; i < particleCount; i++) {
                double partial = i * sep;
                double dx = Mth.lerp(partial, e.xo, e.getX());
                double dy = Mth.lerp(partial, e.yo, e.getY());
                double dz = Mth.lerp(partial, e.zo, e.getZ());
                double sx = e.level().random.nextDouble() * 0.004D - 0.002D;
                double sy = e.level().random.nextDouble() * 0.004D - 0.002D;
                double sz = e.level().random.nextDouble() * 0.004D - 0.002D;
                e.level().addParticle(trailSmokeParticleData, true, dx, dy, dz, sx, sy, sz);
            }
        }
    }
}