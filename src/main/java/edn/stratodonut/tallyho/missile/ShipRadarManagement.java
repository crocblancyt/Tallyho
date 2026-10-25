package edn.stratodonut.tallyho.missile;

import com.mojang.blaze3d.platform.Window;
import java.util.HashSet;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.primitives.AABBd;
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@OnlyIn(Dist.CLIENT)
public class ShipRadarManagement {
   private static final int RADAR_RANGE = 10000;
   private static final Random random = new Random();
   private long shipId = -1L;
   private long seatShipId = -1L;
   private String slug;
   private HashSet<Long> trackedShips;
   @Nullable
   private static ShipRadarManagement instance;

   private ShipRadarManagement(Level level, @Nonnull Ship ship) {
      this.shipId = ship.getId();
      this.slug = ship.getSlug();
      this.trackedShips = new HashSet<>();
   }

   @Nullable
   public static ShipRadarManagement getInstance() {
      Player p = Minecraft.getInstance().player;
      if (p != null && instance != null) {
         Ship s = VSGameUtilsKt.getShipMountedTo(p);
         if (s == null) {
            return null;
         }

         if (s.getId() != instance.shipId && s.getId() != instance.seatShipId) {
            Ship actual = VSGameUtilsKt.getAllShips(p.level()).getById(instance.shipId);
            if (actual == null || !actual.getWorldAABB().intersectsAABB(new AABBd(s.getWorldAABB()))) {
               return null;
            }

            instance.seatShipId = s.getId();
         }

         return instance;
      } else {
         return null;
      }
   }

   public static void init(ClientLevel level, @Nonnull Ship s) {
      instance = new ShipRadarManagement(level, s);
   }

   public static void tick(ClientLevel level) {
      Player p = Minecraft.getInstance().player;
      if (p != null) {
         ShipRadarManagement srs = getInstance();
         if (srs != null) {
            srs.trackedShips.clear();

            for (Ship s : VSGameUtilsKt.getAllShips(level)) {
               Vec3 pos = VectorConversionsMCKt.toMinecraft(s.getTransform().getPositionInWorld());
               double dist = p.position().distanceTo(pos);
               if (dist > 16.0 && dist < 10000.0 && s.getVelocity().length() > 40.0) {
                  srs.trackedShips.add(s.getId());
               }
            }
         }
      }
   }

   public static void drawScreen(Window window, GuiGraphics gui) {
      ShipRadarManagement srs = getInstance();
      if (srs != null) {
         Minecraft mc = Minecraft.getInstance();
         GameRenderer renderer = mc.gameRenderer;
         Player p = mc.player;
         Vec3 cameraPos = renderer.getMainCamera().getPosition();
         Quaternionf cameraConj = new Quaternionf(renderer.getMainCamera().rotation()).conjugate();
         int fov = (Integer)mc.options.fov().get();
         float half_height = window.getGuiScaledHeight() / 2.0F;
         float half_width = window.getGuiScaledWidth() / 2.0F;
         float global_scale_factor = half_height / (float)Math.tan(Math.toRadians(fov / 2.0F));
         QueryableShipData<Ship> allShips = VSGameUtilsKt.getAllShips(mc.level);

         for (long l : srs.trackedShips) {
            Ship s = allShips.getById(l);
            if (s == null) {
               return;
            }

            int dist = (int)(Math.round(p.position().distanceTo(VectorConversionsMCKt.toMinecraft(s.getTransform().getPositionInWorld())) / 100.0) * 100L);
            Vector3f worldPos = cameraPos.subtract(VectorConversionsMCKt.toMinecraft(s.getTransform().getPositionInWorld())).toVector3f();
            worldPos.rotate(cameraConj);
            float scale_factor = global_scale_factor * (1.0F / worldPos.z);
            int x = (int)(-worldPos.x * scale_factor + half_width);
            int y = (int)(half_height - worldPos.y * scale_factor);
            if (worldPos.z() < 0.0F) {
               gui.drawString(mc.font, String.format("%d", dist), x + 10, y, 14024652);
               gui.drawCenteredString(mc.font, "[ ]", x, y, 14024652);
            }
         }
      }
   }
}
