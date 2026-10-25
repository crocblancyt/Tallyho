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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ProximityFuze implements FuzeComponent {
   private static final int ARMING_TICKS = 60;
   public final float radius;
   private final int delayTicks;
   private Integer detTicker;

   protected ProximityFuze(float radius, int delayTicks) {
      this.radius = radius;
      this.delayTicks = delayTicks;
   }

   @Override
   public Optional<Vec3> tick(MountedMissileEntity missile, Level level, Vec3 pos, Vec3 del) {
      if (missile.getTicksSinceLaunch() < 60) {
         return Optional.empty();
      }

      AABB aabb = missile.getBoundingBox().inflate(this.radius, this.radius, this.radius).move(del.reverse());
      Vec3 newDel = missile.collideWithShips(del, aabb);
      if (newDel.distanceToSqr(del) > 0.1) {
         this.detTicker = this.delayTicks;
      }

      if (this.detTicker != null) {
         if (this.detTicker <= 0) {
            return Optional.of(pos);
         }

         Integer var7 = this.detTicker;
         this.detTicker = this.detTicker - 1;
      }

      return Optional.empty();
   }

   public static class Factory implements IFuzeFactory<ProximityFuze> {
      public final float radius;
      private final int delayTicks;

      public Factory(float radius, int delayTicks) {
         this.radius = radius;
         this.delayTicks = delayTicks;
      }

      public ProximityFuze create() {
         return new ProximityFuze(this.radius, this.delayTicks);
      }

      @Override
      public void appendHoverText(List<Component> components) {
         components.add(Component.literal("Proximity Fuze").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
         components.add(Component.literal(String.format("Burst Radius: %sm", this.radius)).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
      }
   }
}
