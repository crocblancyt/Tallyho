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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.Key;
import org.joml.primitives.AABBd;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@OnlyIn(Dist.CLIENT)
public class ShipStoresManagement {
   private static final Random random = new Random();
   private final List<ShipStoresManagement.StoresOfType> all_stores = new ArrayList<>();
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

   @Nullable
   public static String getWSlug() {
      return instance == null ? null : instance.slug;
   }

   private ShipStoresManagement() {
   }

   public static void tick(ClientLevel level) {
      Player p = Minecraft.getInstance().player;
      if (p != null) {
         ShipStoresManagement sms = getInstance();
         if (instance != null) {
            if (sms == null) {
               while (KeyBinds.KEY_SMS_LAUNCH.consumeClick()) {
               }

               return;
            }

            if (KeyBinds.KEY_SMS_LAUNCH.consumeClick()) {
               instance.launchAndActivateNext();
            }
         }
      }
   }

   public static void init(ClientLevel level, @Nonnull Ship s) {
      instance = create(level, s);
   }

   @Nullable
   private static ShipStoresManagement create(ClientLevel level, Ship s) {
      List<MountedMissileEntity> missiles = level.getEntitiesOfClass(MountedMissileEntity.class, VectorConversionsMCKt.toMinecraft(s.getWorldAABB()));
      if (missiles.isEmpty()) {
         return null;
      }

      ShipStoresManagement sms = new ShipStoresManagement();
      missiles.sort(Comparator.comparing(MountedMissileEntity::getMissileId));
      String lastId = null;

      for (MountedMissileEntity m : missiles) {
         String curId = m.getMissileId();
         if (curId != null && !Objects.equals(curId, lastId)) {
            sms.all_stores.add(new ShipStoresManagement.StoresOfType(curId, convertName(curId)));
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
      if (sms != null) {
         int width = window.getGuiScaledWidth();
         int x = width - 100;
         int y = window.getGuiScaledHeight() - 75;
         String var10002 = sms.armed ? "ARM" : "SAFE";
         y -= 10;
         gui.drawCenteredString(mc.font, var10002, x, y, 14024652);

         for (int i = sms.all_stores.size() - 1; i >= 0; i--) {
            ShipStoresManagement.StoresOfType s = sms.all_stores.get(i);
            Font var10001 = mc.font;
            var10002 = String.format("%s %d%8s", sms.selectedType == i ? ">" : " ", s.stores().size(), s.displayName());
            y -= 10;
            gui.drawCenteredString(var10001, var10002, x, y, 14024652);
         }

         if (sms.focused) {
            y -= 10;
            gui.drawCenteredString(mc.font, "FOCUSED", x, y, 14024652);
         }
      }
   }

   public static void onKeyPressed(Key event, byte specialFuncMap) {
      ShipStoresManagement sms = getInstance();
      if (sms != null) {
         if (sms.focused) {
            if (KeyBinds.isFuncKeyDown(specialFuncMap, KeyBinds.SpecialFunctions.UP)) {
               sms.activateNext();
            }

            if (KeyBinds.isFuncKeyDown(specialFuncMap, KeyBinds.SpecialFunctions.DOWN)) {
               sms.armed = !sms.armed;
            }

            if (KeyBinds.isFuncKeyDown(specialFuncMap, KeyBinds.SpecialFunctions.LEFT)) {
               sms.prevType();
            }

            if (KeyBinds.isFuncKeyDown(specialFuncMap, KeyBinds.SpecialFunctions.RIGHT)) {
               sms.nextType();
            }
         }

         if (KeyBinds.KEY_SMS_FOCUS.isDown()) {
            sms.focused = !sms.focused;
         }
      }
   }

   public boolean launchAndActivateNext() {
      Optional<Integer> next = this.getNextMissileEntityId(true);
      if (next.isEmpty()) {
         return false;
      }

      next.ifPresent(m -> {
         launchMissile(m);
         this.activateNext();
      });
      return true;
   }

   public void activateNext() {
      this.getNextMissileEntityId(false).ifPresent(ShipStoresManagement::activateMissile);
   }

   public static String convertName(String raw) {
      StringBuilder out = new StringBuilder();
      out.append(Character.toUpperCase(raw.charAt(0)));
      int i = raw.length();
      boolean skipNonNumeric = true;

      do {
         char c = raw.charAt(--i);
         if (c >= '0' && c <= '9') {
            if (skipNonNumeric) {
               skipNonNumeric = false;
            }
         } else if (!skipNonNumeric) {
            break;
         }
      } while (i > 0);

      if (++i < raw.length() && !skipNonNumeric) {
         out.append(" ");

         while (i < raw.length()) {
            char var7 = raw.charAt(i);
            out.append(Character.toUpperCase(var7));
            if (var7 < '0' || var7 > '9') {
               break;
            }

            i++;
         }
      } else {
         for (int var6 = 1; var6 < raw.length(); var6++) {
            out.append(Character.toUpperCase(raw.charAt(var6)));
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
      if (this.armed && this.selectedType < this.all_stores.size() && this.selectedType >= 0) {
         HashSet<Integer> m = this.all_stores.get(this.selectedType).stores();
         if (m.isEmpty()) {
            return Optional.empty();
         }

         int id = m.iterator().next();
         if (remove) {
            m.remove(id);
         }

         return Optional.of(id);
      } else {
         return Optional.empty();
      }
   }

   @Nonnull
   public Optional<Integer> getNextMissileEntityId() {
      return this.getNextMissileEntityId(false);
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

   public record StoresOfType(String name, HashSet<Integer> stores, String displayName) {
      public StoresOfType(String name, String displayName) {
         this(name, new HashSet<>(), displayName);
      }
   }
}
