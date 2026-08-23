package edn.stratodonut.tallyho.entity;

import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.Utils;
import edn.stratodonut.tallyho.missile.tracker.FlareTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.effects.particles.smoke.ShrapnelSmokeParticleData;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile;

public class FlareEntity extends Entity {
    public static final int DWELL_TICKS = 100;

    private float brightness;

    public FlareEntity(EntityType<FlareEntity> type, Level level) {
        super(type, level);
        this.brightness = 0.0F;
    }

    public static void dropFromShip(Level level, ServerShip s, BlockPos pos, Vec3 launchVel) {
        FlareEntity flare = (FlareEntity)AllEntities.FLARE_ENTITY.create(level);
        if (flare != null) {
            Utils.PosVel spawnPosVel = Utils.moveToWorld(pos.getCenter(), (Ship)s, 5.0F);
            flare.setPos(spawnPosVel.pos());
            flare.setDeltaMovement(
                VectorConversionsMCKt.toMinecraft((Vector3dc)s.getTransform().getShipToWorld().transformDirection(VectorConversionsMCKt.toJOML(launchVel)))
                .add(spawnPosVel.vel()));
            flare.brightness = (float)s.getInertiaData().getMass();
            level.addFreshEntity(flare);
            FlareTracker.popFlare((Ship)s);
        }
    }

    public void tick() {
        super.tick();
        setPos(position().add(getDeltaMovement()));
        Vec3 vel = getDeltaMovement();
        vel = new Vec3(vel.x * 0.95D, vel.y * 0.95D - 0.05D, vel.z * 0.95D);
        setDeltaMovement(vel);
        this.brightness *= 0.99F;
        if (this.tickCount > 100)
            remove(Entity.RemovalReason.KILLED);
        emitSmoke(getDeltaMovement(), this);
    }

    public void emitSmoke(Vec3 vel, Entity e) {
        AbstractBigCannonProjectile.TrailType trailType = (AbstractBigCannonProjectile.TrailType)CBCConfigs.SERVER.munitions.bigCannonTrailType.get();
        if (trailType != AbstractBigCannonProjectile.TrailType.NONE) {
            int lifetime = (trailType == AbstractBigCannonProjectile.TrailType.SHORT) ? 100 : (280 + (e.level()).random.nextInt(50));
            ShrapnelSmokeParticleData shrapnelSmokeParticleData = new ShrapnelSmokeParticleData();
            double particleCount = Mth.clamp(vel.length() / 2.0D, 10.0D, 64.0D);
            double sep = 1.0D / particleCount;
            for (int i = 0; i < particleCount; i++) {
                double partial = i * sep;
                double dx = Mth.lerp(partial, e.xo, e.getX());
                double dy = Mth.lerp(partial, e.yo, e.getY());
                double dz = Mth.lerp(partial, e.zo, e.getZ());
                double sx = (e.level()).random.nextDouble() * 0.004D - 0.002D;
                double sy = (e.level()).random.nextDouble() * 0.004D - 0.002D;
                double sz = (e.level()).random.nextDouble() * 0.004D - 0.002D;
                e.level().addParticle((ParticleOptions)shrapnelSmokeParticleData, true, dx, dy, dz, sx, sy, sz);
            }
        }
    }

    protected void defineSynchedData() {}

    protected void readAdditionalSaveData(CompoundTag p_20052_) {}

    protected void addAdditionalSaveData(CompoundTag p_20139_) {}
}