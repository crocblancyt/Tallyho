package edn.stratodonut.tallyho.missile;

import com.mojang.blaze3d.platform.Window;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.primitives.AABBd;
import org.valkyrienskies.core.api.ships.LoadedShip;
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
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || instance == null)
            return null;
        LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo((Entity)localPlayer);
        if (loadedShip == null)
            return null;
        if (loadedShip.getId() != instance.shipId && loadedShip.getId() != instance.seatShipId) {
            Ship actual = VSGameUtilsKt.getAllShips(localPlayer.level()).getById(instance.shipId);
            if (actual == null || !actual.getWorldAABB().intersectsAABB(new AABBd(loadedShip.getWorldAABB())))
                return null;
            instance.seatShipId = loadedShip.getId();
        }
        return instance;
    }

    public static void init(ClientLevel level, @Nonnull Ship s) {
        instance = new ShipRadarManagement((Level)level, s);
    }

    public static void tick(ClientLevel level) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null)
            return;
        ShipRadarManagement srs = getInstance();
        if (srs == null)
            return;
        srs.trackedShips.clear();
        QueryableShipData<Ship> allShips = VSGameUtilsKt.getAllShips((Level)level);
        for (Ship s : allShips) {
            Vec3 pos = VectorConversionsMCKt.toMinecraft(s.getTransform().getPositionInWorld());
            double dist = localPlayer.position().distanceToSqr(pos);
            if (dist > 16.0D && dist < 10000.0D && s.getVelocity().length() > 40.0D)
                srs.trackedShips.add(s.getId());
        }
    }

    public static void drawScreen(Window window, GuiGraphics gui) {
        ShipRadarManagement srs = getInstance();
        if (srs == null)
            return;
        Minecraft mc = Minecraft.getInstance();
        GameRenderer renderer = mc.gameRenderer;
        LocalPlayer localPlayer = mc.player;
        Vec3 cameraPos = renderer.getMainCamera().getPosition();
        Quaternionf cameraConj = (new Quaternionf((Quaternionfc)renderer.getMainCamera().rotation())).conjugate();
        int fov = mc.options.fov().get();
        float half_height = window.getGuiScaledHeight() / 2.0F;
        float half_width = window.getGuiScaledWidth() / 2.0F;
        float global_scale_factor = half_height / (float)Math.tan(Math.toRadians((fov / 2.0F)));
        QueryableShipData<Ship> allShips = VSGameUtilsKt.getAllShips(mc.level);
        for (Iterator<Long> iterator = srs.trackedShips.iterator(); iterator.hasNext(); ) {
            long l = iterator.next();
            Ship s = allShips.getById(l);
            if (s == null)
                return;
            int dist = (int)(Math.round(localPlayer.position().distanceTo(VectorConversionsMCKt.toMinecraft(s.getTransform().getPositionInWorld())) / 100.0D) * 100L);
            Vector3f worldPos = cameraPos.subtract(VectorConversionsMCKt.toMinecraft(s.getTransform().getPositionInWorld())).toVector3f();
            worldPos.rotate((Quaternionfc)cameraConj);
            float scale_factor = global_scale_factor * 1.0F / worldPos.z;
            int x = (int)(-worldPos.x * scale_factor + half_width);
            int y = (int)(half_height - worldPos.y * scale_factor);
            if (worldPos.z() < 0.0F) {
                gui.drawString(mc.font, String.format("%d", dist), x + 10, y, 14024652);
                gui.drawString(mc.font, "[ ]", x, y, 14024652);
            }
        }
    }
}