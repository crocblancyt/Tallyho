package edn.stratodonut.tallyho.missile.guid;

import edn.stratodonut.tallyho.AllSounds;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.GuidanceComponent;
import edn.stratodonut.tallyho.missile.IGuidanceFactory;
import edn.stratodonut.tallyho.missile.tracker.RadEmissionTracker;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.MissileTonePacket;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class AntiRadGroundSeeker extends GuidanceComponent {
   private static final int ARMING_TICKS = 10;
   private final AntiRadGroundSeeker.SeekerProperties properties;
   @Nullable
   protected RadEmissionTracker.Emitter target;

   private AntiRadGroundSeeker(AntiRadGroundSeeker.SeekerProperties props) {
      this.properties = props;
   }

   @Override
   public void tick(MountedMissileEntity missile) {
      if (!missile.level().isClientSide) {
         if (this.isSeeking()) {
            Vec3 lookDir = missile.getLookAngle();
            Vec3 v_m = missile.getDeltaMovement();
            if (missile.getTicksSinceLaunch() > 10) {
               lookDir = v_m.normalize();
            }

            RadEmissionTracker.Emitter t = this.target;
            Vec3 p = missile.position();
            if (this.target == null && missile.tickCount % 10 == 0) {
               Stream<RadEmissionTracker.Emitter> targ = RadEmissionTracker.find(
                  missile.level(), missile.position(), this.properties.sensitivity, lookDir, this.properties.seeker_fov
               );
               targ.filter(e -> e.getType() != RadEmissionTracker.EmitterType.FLYING && e.getPower() > 100)
                  .max(Comparator.comparingDouble(e -> e.getPower() / e.getPos().subtract(p).lengthSqr()))
                  .ifPresent(v -> {
                     double d = v.getPos().subtract(p).length() / 100.0;
                     this.target = v.withOffset(this.random.nextDouble(-d, d), this.random.nextDouble(-d, d), this.random.nextDouble(-d, d));
                  });
            } else if (this.target != null) {
               if (missile.tickCount % 10 == 0) {
                  Stream<RadEmissionTracker.Emitter> targ = RadEmissionTracker.find(
                     missile.level(), missile.position(), this.properties.sensitivity, lookDir, this.properties.seeker_fov
                  );
                  targ.filter(e -> e.equals(this.target)).findFirst().ifPresent(v -> {
                     double d = v.getPos().subtract(p).length() / 100.0;
                     this.target = v.withOffset(this.random.nextDouble(-d, d), this.random.nextDouble(-d, d), this.random.nextDouble(-d, d));
                  });
               }

               if (missile.getTicksSinceLaunch() > 10) {
                  missile.setDeltaMovement(
                     GuidanceComponent.ProportionalGuidance(
                        this.target.getPos(), Vec3.ZERO, missile.position(), v_m, this.properties.lead_coefficient, this.properties.max_G
                     )
                  );
               }
            }

            if (missile.getVehicle() != null) {
               boolean interrupt = t != this.target;
               if (missile.tickCount % 20 != 0 && !interrupt) {
                  return;
               }

               missile.level()
                  .players()
                  .stream()
                  .filter(
                     player -> VSGameUtilsKt.getShipMountedTo(player) != null
                        && VSGameUtilsKt.getShipMountedTo(missile) != null
                        && Objects.equals(VSGameUtilsKt.getShipMountedTo(player).getSlug(), VSGameUtilsKt.getShipMountedTo(missile).getSlug())
                  )
                  .forEach(
                     player -> CreateTypePacketHandler.getChannel()
                        .send(
                           PacketDistributor.PLAYER.with(() -> (ServerPlayer)player),
                           new MissileTonePacket(
                              this.target != null
                                 ? ((SoundEvent)AllSounds.AIM9_LOCK.get()).getLocation()
                                 : ((SoundEvent)AllSounds.AIM9_SEEK.get()).getLocation(),
                              missile.getId(),
                              interrupt
                           )
                        )
                  );
            }
         }
      }
   }

   @Override
   public boolean launch(MountedMissileEntity e) {
      return true;
   }

   public static class Factory implements IGuidanceFactory<AntiRadGroundSeeker> {
      private final AntiRadGroundSeeker.SeekerProperties props;

      public Factory(int seeker_fov, int lead_coefficient, float max_G, float sensitivity) {
         this.props = new AntiRadGroundSeeker.SeekerProperties(seeker_fov, lead_coefficient, max_G, sensitivity);
      }

      public AntiRadGroundSeeker create() {
         return new AntiRadGroundSeeker(this.props);
      }

      @Override
      public void appendHoverText(List<Component> components) {
         this.props.appendHoverText(components);
      }
   }

   public record SeekerProperties(int seeker_fov, int lead_coefficient, float max_G, float sensitivity) {
      public void appendHoverText(List<Component> components) {
         components.add(Component.literal("Guidance: Anti-Radar (Ground)").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
         components.add(Component.literal(String.format("Fov: %s°", this.seeker_fov)).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
         components.add(Component.literal(String.format("Sensitivity: %sx", this.sensitivity)).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
      }
   }
}
