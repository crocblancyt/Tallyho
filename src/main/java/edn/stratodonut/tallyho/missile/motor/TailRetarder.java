package edn.stratodonut.tallyho.missile.motor;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.IMotorFactory;
import edn.stratodonut.tallyho.missile.MotorComponent;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec3;

public class TailRetarder extends MotorComponent {
    public static class Factory implements IMotorFactory<TailRetarder> {
        public TailRetarder create() {
            return new TailRetarder();
        }

        public void appendHoverText(List<Component> components) {
            components.add(Component.literal("Has Retarding Device").setStyle(Style.EMPTY
                    .withColor(ChatFormatting.WHITE)));
        }
    }

    public void tick(MountedMissileEntity e) {
        Vec3 vel = e.getDeltaMovement();
        double drag = (e.getTicksSinceLaunch() > 10) ? 0.01D : 0.001D;
        e.setDeltaMovement(vel.normalize().scale(vel.length() * -drag).add(0.0D, -0.05D, 0.0D));
    }

    public boolean launch(MountedMissileEntity e, Vec3 dir, float boost) {
        return false;
    }
}