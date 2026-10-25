package edn.stratodonut.tallyho.missile.motor;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.IMotorFactory;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec3;

public class RocketMotorNoLift extends RocketMotor {
   private final RocketMotorNoLift.LiftProperties properties;

   public RocketMotorNoLift(RocketMotor.MotorProperties motor, RocketMotorNoLift.LiftProperties lift, float offset) {
      super(motor, offset);
      this.properties = lift;
   }

   @Override
   public void tick(MountedMissileEntity e) {
      super.tick(e);
      if (!e.level().isClientSide) {
         e.addDeltaMovement(new Vec3(0.0, -this.properties.gravity(), 0.0));
      }
   }

   public static class Factory implements IMotorFactory<RocketMotorNoLift> {
      private final RocketMotorNoLift.LiftProperties lift;
      private final RocketMotor.MotorProperties motor;
      private float renderOffset = 0.0F;

      public Factory(float boost, int burn_time, float gravity, boolean smokeless, float dispersion) {
         this.motor = new RocketMotor.MotorProperties(boost, burn_time, smokeless, dispersion);
         this.lift = new RocketMotorNoLift.LiftProperties(gravity);
      }

      public RocketMotorNoLift.Factory offsetRender(float offset) {
         this.renderOffset = offset;
         return this;
      }

      public RocketMotorNoLift create() {
         return new RocketMotorNoLift(this.motor, this.lift, this.renderOffset);
      }

      @Override
      public void appendHoverText(List<Component> list) {
         this.motor.appendHoverText(list);
         this.lift.appendHoverText(list);
      }
   }

   public record LiftProperties(float gravity) {
      public void appendHoverText(List<Component> components) {
         components.add(Component.literal("Indirect Fire").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
      }
   }
}
