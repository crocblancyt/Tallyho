package edn.stratodonut.tallyho.missile;

import com.mojang.blaze3d.platform.Window;
import edn.stratodonut.tallyho.KeyBinds;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.SMSCommandPacket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import org.joml.primitives.AABBd;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@OnlyIn(Dist.CLIENT)
public class ShipStoresManagement {
    private static final Random random = new Random();

    private final List<StoresOfType> all_stores = new ArrayList<>();
    private boolean focused = false;
    private boolean armed = false;
    private int selectedType = 0;
    private long shipId = -1L;
    private long seatShipId = -1L;
    private String slug;
    @Nullable
    private static ShipStoresManagement instance;

    @Nullable
    public static ShipStoresManagement getInstance() {
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

    @Nullable
    public static String getWSlug() {
        if (instance == null)
            return null;
        return instance.slug;
    }

    public static final class StoresOfType {
        private final String name;
        private final HashSet<Integer> stores;
        private final String displayName;

        public StoresOfType(String name, HashSet<Integer> stores, String displayName) {
            this.name = name;
            this.stores = stores;
            this.displayName = displayName;
        }

        public StoresOfType(String name, String displayName) {
            this(name, new HashSet<>(), displayName);
        }

        public String name() {
            return this.name;
        }

        public HashSet<Integer> stores() {
            return this.stores;
        }

        public String displayName() {
            return this.displayName;
        }
    }

    public static void tick(ClientLevel level) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null)
            return;
        ShipStoresManagement sms = getInstance();
        if (instance != null) {
            if (sms == null) {
                while (KeyBinds.KEY_SMS_LAUNCH.consumeClick());
                return;
            }
            if (KeyBinds.KEY_SMS_LAUNCH.consumeClick())
                instance.launchAndActivateNext();
        }
    }

    public static void init(ClientLevel level, @Nonnull Ship s) {
        instance = create(level, s);
    }

    @Nullable
    private static ShipStoresManagement create(ClientLevel level, Ship s) {
        List<MountedMissileEntity> missiles = level.getEntitiesOfClass(MountedMissileEntity.class, VectorConversionsMCKt.toMinecraft(s.getWorldAABB()));
        if (missiles.isEmpty())
            return null;
        ShipStoresManagement sms = new ShipStoresManagement();
        missiles.sort(Comparator.comparing(MountedMissileEntity::getMissileId));
        String lastId = null;
        for (MountedMissileEntity m : missiles) {
            String curId = m.getMissileId();
            if (curId != null && !Objects.equals(curId, lastId)) {
                sms.all_stores.add(new StoresOfType(curId, convertName(curId)));
                lastId = curId;
            }
            sms.all_stores.get(sms.all_stores.size() - 1).stores().add(m.getId());
        }
        sms.focused = false;
        sms.armed = false;
        sms.setShipId(s);
        return sms;
    }

    public static void drawScreen(Window window, GuiGraphics gui) {
        Minecraft mc = Minecraft.getInstance();
        ShipStoresManagement sms = getInstance();
        if (sms == null)
            return;
        int width = window.getGuiScaledWidth();
        int x = width - 100;
        int y = window.getGuiScaledHeight() - 75;
        y -= 10;
        gui.drawString(mc.font, sms.armed ? "ARM" : "SAFE", x, y, 14024652);
        for (int i = sms.all_stores.size() - 1; i >= 0; i--) {
            StoresOfType s = sms.all_stores.get(i);
            y -= 10;
            gui.drawString(mc.font, String.format("%s %d%8s", (sms.selectedType == i) ? ">" : " ", s.stores().size(), s.displayName()), x, y, 14024652);
        }
        if (sms.focused) {
            y -= 10;
            gui.drawString(mc.font, "FOCUSED", x, y, 14024652);
        }
    }

    public static void onKeyPressed(InputEvent.Key event, byte specialFuncMap) {
        ShipStoresManagement sms = getInstance();
        if (sms != null) {
            if (sms.focused) {
                if (KeyBinds.isFuncKeyDown(specialFuncMap, KeyBinds.SpecialFunctions.UP))
                    sms.activateNext();
                if (KeyBinds.isFuncKeyDown(specialFuncMap, KeyBinds.SpecialFunctions.DOWN))
                    sms.armed = !sms.armed;
                if (KeyBinds.isFuncKeyDown(specialFuncMap, KeyBinds.SpecialFunctions.LEFT))
                    sms.prevType();
                if (KeyBinds.isFuncKeyDown(specialFuncMap, KeyBinds.SpecialFunctions.RIGHT))
                    sms.nextType();
            }
            if (KeyBinds.KEY_SMS_FOCUS.isDown())
                sms.focused = !sms.focused;
        }
    }

    public boolean launchAndActivateNext() {
        Optional<Integer> next = getNextMissileEntityId(true);
        if (next.isEmpty())
            return false;
        next.ifPresent(m -> {
            launchMissile(m);
            activateNext();
        });
        return true;
    }

    public void activateNext() {
        getNextMissileEntityId(false).ifPresent(ShipStoresManagement::activateMissile);
    }

    public static String convertName(String raw) {
        StringBuilder out = new StringBuilder();
        out.append(Character.toUpperCase(raw.charAt(0)));
        int i = raw.length();
        boolean skipNonNumeric = true;
        do {
            char c = raw.charAt(--i);
            if (c >= '0' && c <= '9') {
                if (skipNonNumeric)
                    skipNonNumeric = false;
            } else if (!skipNonNumeric) {
                break;
            }
        } while (i > 0);
        i++;
        if (i < raw.length() && !skipNonNumeric) {
            out.append(" ");
            for (; i < raw.length(); i++) {
                char c = raw.charAt(i);
                out.append(Character.toUpperCase(c));
                if (c < '0' || c > '9')
                    break;
            }
        } else {
            for (i = 1; i < raw.length(); ) {
                out.append(Character.toUpperCase(raw.charAt(i)));
                i++;
            }
        }
        return out.toString();
    }

    public void setShipId(Ship s) {
        this.shipId = s.getId();
        this.slug = s.getSlug();
    }

    public String getSlug() {
        return this.slug;
    }

    public void prevType() {
        this.selectedType = Math.floorMod(this.selectedType - 1, this.all_stores.size());
    }

    public void nextType() {
        this.selectedType = Math.floorMod(this.selectedType + 1, this.all_stores.size());
    }

    @Nonnull
    private Optional<Integer> getNextMissileEntityId(boolean remove) {
        if (!this.armed || this.selectedType >= this.all_stores.size() || this.selectedType < 0)
            return Optional.empty();
        HashSet<Integer> m = this.all_stores.get(this.selectedType).stores();
        if (m.isEmpty())
            return Optional.empty();
        int id = m.iterator().next();
        if (remove)
            m.remove(id);
        return Optional.of(id);
    }

    @Nonnull
    public Optional<Integer> getNextMissileEntityId() {
        return getNextMissileEntityId(false);
    }

    public boolean isArmed() {
        return this.armed;
    }

    public static void activateMissile(int id) {
        CreateTypePacketHandler.getChannel().sendToServer(SMSCommandPacket.ActivateSeeker(id));
    }

    public static void launchMissile(int id) {
        CreateTypePacketHandler.getChannel().sendToServer(SMSCommandPacket.Launch(id));
    }
}