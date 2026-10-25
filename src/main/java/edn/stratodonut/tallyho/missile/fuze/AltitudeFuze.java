package edn.stratodonut.tallyho.missile.fuze;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.FuzeComponent;
import edn.stratodonut.tallyho.missile.IFuzeFactory;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AltitudeFuze implements FuzeComponent {
   private static final int ARMING_TICKS = 30;
   public final float alt;
   private int delayTicks;

   protected AltitudeFuze(float alt, int delayTicks) {
      this.alt = alt;
      this.delayTicks = delayTicks;
   }

   @Override
   public Optional<Vec3> tick(MountedMissileEntity missile, Level level, Vec3 pos, Vec3 del) {
      if (missile.getTicksSinceLaunch() < 30) {
         return Optional.empty();
      } else {
         return pos.y() < this.alt && this.delayTicks-- <= 0 ? Optional.of(pos) : Optional.empty();
      }
   }

   @Override
   public boolean impact(MountedMissileEntity missile, Level level, Vec3 pos, Vec3 del) {
      return false;
   }

   @Override
   public boolean explodesOnExpiry() {
      return false;
   }

   public static class Factory implements IFuzeFactory<AltitudeFuze> {
      public final float alt;
      private final int delayTicks;

      public Factory(float alt, int delayTicks) {
         this.alt = alt;
         this.delayTicks = delayTicks;
      }

      public AltitudeFuze create() {
         return new AltitudeFuze(this.alt, this.delayTicks);
      }

      @Override
      public void appendHoverText(List<Component> components) {
         components.add(Component.literal("Altitude Fuze").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
         components.add(Component.literal(String.format("Burst Alt: %sm", this.alt)).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
      }
   }
}
