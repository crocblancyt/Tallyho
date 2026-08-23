package edn.stratodonut.tallyho.missile.motor;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.IMotorFactory;
import edn.stratodonut.tallyho.missile.MotorComponent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec3;

public class RocketMotorNoLift extends RocketMotor {
    private final LiftProperties properties;

    public static final class LiftProperties {
        private final float gravity;

        public LiftProperties(float gravity) {
            this.gravity = gravity;
        }

        public float gravity() {
            return this.gravity;
        }

        public void appendHoverText(List<Component> components) {
            components.add(Component.literal("Indirect Fire").setStyle(Style.EMPTY
                    .withColor(ChatFormatting.YELLOW)));
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

        public Factory offsetRender(float offset) {
            this.renderOffset = offset;
            return this;
        }

        public RocketMotorNoLift create() {
            return new RocketMotorNoLift(this.motor, this.lift, this.renderOffset);
        }

        public void appendHoverText(List<Component> list) {
            this.motor.appendHoverText(list);
            this.lift.appendHoverText(list);
        }
    }

    public RocketMotorNoLift(RocketMotor.MotorProperties motor, LiftProperties lift, float offset) {
        super(motor, offset);
        this.properties = lift;
    }

    public void tick(MountedMissileEntity e) {
        super.tick(e);
        if (!e.level().isClientSide)
            e.setDeltaMovement(new Vec3(0.0D, -this.properties.gravity(), 0.0D));
    }
}