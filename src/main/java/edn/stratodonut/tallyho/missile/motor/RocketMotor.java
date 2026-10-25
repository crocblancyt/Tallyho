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
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile.TrailType;

public class RocketMotor extends MotorComponent {
   private final RocketMotor.MotorProperties properties;
   public final float renderOffset;

   public RocketMotor(RocketMotor.MotorProperties props, float offset) {
      this.properties = props;
      this.renderOffset = offset;
   }

   @Override
   public void tick(MountedMissileEntity e) {
      Vec3 dir = e.getLookAngle();
      if (!e.level().isClientSide && e.getTicksSinceLaunch() < this.properties.burn_time()) {
         e.addDeltaMovement(e.disperseSquareErrorProbable50(dir, this.properties.dispersion).scale(this.properties.boost));
      }

      if (e.getVehicle() == null && e.level().isClientSide) {
         float vertOffset = e.getHitboxScale() * 0.5F;
         if (e.getTicksSinceLaunch() < this.properties.burn_time() && !this.properties.smokeless) {
            this.emitSmoke(dir, e);
         }
      }
   }

   public float getBurnTime() {
      return this.properties.burn_time;
   }

   @Override
   public boolean launch(MountedMissileEntity e, Vec3 dir, float boost) {
      Vec3 vel = dir.scale(this.properties.boost() + boost);
      e.setDeltaMovement(vel);
      e.playSound((SoundEvent)AllSounds.MISSILE_LAUNCH.get(), 3.0F, 1.0F);
      this.emitSmoke(vel, e);
      return true;
   }

   public void emitSmoke(Vec3 vel, Entity e) {
      TrailType trailType = (TrailType)CBCConfigs.SERVER.munitions.bigCannonTrailType.get();
      if (trailType != TrailType.NONE) {
         int lifetime = trailType == TrailType.SHORT ? 100 : 280 + e.level().random.nextInt(50);
         ParticleOptions options = new TrailSmokeParticleData(lifetime);
         double particleCount = Mth.clamp(vel.length(), 10.0, 64.0);
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

   public static class Factory implements IMotorFactory<RocketMotor> {
      private final RocketMotor.MotorProperties properties;
      private float renderOffset = 0.0F;

      public Factory(float boost, int burn_time, boolean smokeless) {
         this.properties = new RocketMotor.MotorProperties(boost, burn_time, smokeless, 0.01875);
      }

      public Factory(float boost, int burn_time, boolean smokeless, double dispersion) {
         this.properties = new RocketMotor.MotorProperties(boost, burn_time, smokeless, dispersion);
      }

      public RocketMotor.Factory offsetRender(float offset) {
         this.renderOffset = offset;
         return this;
      }

      public RocketMotor create() {
         return new RocketMotor(this.properties, this.renderOffset);
      }

      @Override
      public void appendHoverText(List<Component> list) {
         this.properties.appendHoverText(list);
      }
   }

   public record MotorProperties(float boost, int burn_time, boolean smokeless, double dispersion) {
      public void appendHoverText(List<Component> components) {
         if (this.burn_time > 0) {
            components.add(
               Component.literal(String.format("Rocket Boost: %sm/s", this.boost * 20.0F * this.burn_time))
                  .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
            );
         }
      }
   }
}
