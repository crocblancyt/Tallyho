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
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.effects.particles.smoke.ShrapnelSmokeParticleData;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile.TrailType;

public class FlareEntity extends Entity {
   public static final int DWELL_TICKS = 100;
   private float brightness = 0.0F;

   public FlareEntity(EntityType<FlareEntity> type, Level level) {
      super(type, level);
   }

   public static void dropFromShip(Level level, ServerShip s, BlockPos pos, Vec3 launchVel) {
      FlareEntity flare = (FlareEntity)AllEntities.FLARE_ENTITY.create(level);
      if (flare != null) {
         Utils.PosVel spawnPosVel = Utils.moveToWorld(pos.getCenter(), s, 5.0F);
         flare.setPos(spawnPosVel.pos());
         flare.setDeltaMovement(
            VectorConversionsMCKt.toMinecraft(s.getTransform().getShipToWorld().transformDirection(VectorConversionsMCKt.toJOML(launchVel)))
               .add(spawnPosVel.vel())
         );
         flare.brightness = (float)s.getInertiaData().getMass();
         level.addFreshEntity(flare);
         FlareTracker.popFlare(s);
      }
   }

   public void tick() {
      super.tick();
      this.setPos(this.position().add(this.getDeltaMovement()));
      Vec3 vel = this.getDeltaMovement();
      vel = new Vec3(vel.x * 0.95, vel.y * 0.95 - 0.05, vel.z * 0.95);
      this.setDeltaMovement(vel);
      this.brightness *= 0.99F;
      if (this.tickCount > 100) {
         this.remove(RemovalReason.KILLED);
      }

      this.emitSmoke(this.getDeltaMovement(), this);
   }

   public void emitSmoke(Vec3 vel, Entity e) {
      TrailType trailType = (TrailType)CBCConfigs.SERVER.munitions.bigCannonTrailType.get();
      if (trailType != TrailType.NONE) {
         if (trailType == TrailType.SHORT) {
            int lifetime = 100;
         } else {
            int lifetime = 280 + e.level().random.nextInt(50);
         }

         ParticleOptions options = new ShrapnelSmokeParticleData();
         double particleCount = Mth.clamp(vel.length() / 2.0, 10.0, 64.0);
         double sep = 1.0 / particleCount;

         for (int i = 0; i < particleCount; i++) {
            double partial = i * sep;
            double dx = Mth.lerp(partial, e.xOld, e.getX());
            double dy = Mth.lerp(partial, e.yOld, e.getY());
            double dz = Mth.lerp(partial, e.zOld, e.getZ());
            double sx = e.level().random.nextDouble() * 0.004 - 0.002;
            double sy = e.level().random.nextDouble() * 0.004 - 0.002;
            double sz = e.level().random.nextDouble() * 0.004 - 0.002;
            e.level().addAlwaysVisibleParticle(options, true, dx, dy, dz, sx, sy, sz);
         }
      }
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag p_20052_) {
   }

   protected void addAdditionalSaveData(CompoundTag p_20139_) {
   }
}
