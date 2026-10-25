package edn.stratodonut.tallyho.missile.motor;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.IMotorFactory;
import edn.stratodonut.tallyho.missile.MotorComponent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec3;

public class TailRetarder extends MotorComponent {
   @Override
   public void tick(MountedMissileEntity e) {
      Vec3 vel = e.getDeltaMovement();
      double drag = e.getTicksSinceLaunch() > 10 ? 0.01 : 0.001;
      e.addDeltaMovement(vel.normalize().scale(vel.lengthSqr() * -drag).add(0.0, -0.05, 0.0));
   }

   @Override
   public boolean launch(MountedMissileEntity e, Vec3 dir, float boost) {
      return false;
   }

   public static class Factory implements IMotorFactory<TailRetarder> {
      public TailRetarder create() {
         return new TailRetarder();
      }

      @Override
      public void appendHoverText(List<Component> components) {
         components.add(Component.literal("Has Retarding Device").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
      }
   }
}
