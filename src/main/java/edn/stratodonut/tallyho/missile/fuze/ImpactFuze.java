package edn.stratodonut.tallyho.missile.fuze;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.FuzeComponent;
import edn.stratodonut.tallyho.missile.IFuzeFactory;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ImpactFuze implements FuzeComponent {
   @Override
   public Optional<Vec3> tick(MountedMissileEntity missile, Level level, Vec3 pos, Vec3 del) {
      return Optional.empty();
   }

   public static class Factory implements IFuzeFactory<ImpactFuze> {
      public ImpactFuze create() {
         return new ImpactFuze();
      }

      @Override
      public void appendHoverText(List<Component> components) {
      }
   }
}
