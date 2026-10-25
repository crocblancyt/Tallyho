package edn.stratodonut.tallyho;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.entity.ILaserDesignator;
import edn.stratodonut.tallyho.item.LaserDesignatorItem;
import edn.stratodonut.tallyho.missile.ShipStoresManagement;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.DesignatePacket;
import edn.stratodonut.tallyho.network.GravitronStaticPacket;
import edn.stratodonut.tallyho.network.SeatAdjustPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.client.event.InputEvent.MouseButton.Pre;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.valkyrienskies.clockwork.ClockworkItems;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.CreativeGravitronItem;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.GravitronItem;

public class KeypressHandler {
   private static KeypressHandler instance;

   public static KeypressHandler get() {
      if (instance == null) {
         instance = new KeypressHandler();
      }

      return instance;
   }

   @SubscribeEvent
   public void onClientTick(ClientTickEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (event.phase == Phase.END && mc.player != null) {
         Entity c = CameraClientHandler.getPlayerMountedOnCamera();
         ItemStack stack = mc.player.getUseItem();
         if (c instanceof ILaserDesignator d) {
            if (KeyBinds.KEY_DESIGNATE.isDown()) {
               CreateTypePacketHandler.getChannel().sendToServer(new DesignatePacket(true));
               d.designate();
            }
         } else if (stack.getItem() instanceof LaserDesignatorItem) {
            if (KeyBinds.KEY_DESIGNATE.isDown()) {
               CreateTypePacketHandler.getChannel().sendToServer(new DesignatePacket(true));
               LaserDesignatorItem.designate(stack, true);
            } else {
               CreateTypePacketHandler.getChannel().sendToServer(new DesignatePacket(false));
               LaserDesignatorItem.designate(stack, false);
            }
         }
      }
   }

   @SubscribeEvent
   public void onMouse(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && event.getAction() == 1 && event.getButton() == 0) {
         ItemStack stack = mc.player.getMainHandItem();
         boolean bl = stack.is(((CreativeGravitronItem)ClockworkItems.CREATIVE_GRAVITRON.get()).asItem());
         boolean bl2 = stack.is(((GravitronItem)ClockworkItems.GRAVITRON.get()).asItem());
         if ((bl2 || bl) && !mc.player.getCooldowns().isOnCooldown(stack.getItem())) {
            CreateTypePacketHandler.getChannel().sendToServer(new GravitronStaticPacket());
         }
      }
   }

   @SubscribeEvent
   public void onKeyPressed(Key event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         Player p = mc.player;
         if (event.getAction() == 1) {
            Entity v = p.getVehicle();
            if (v instanceof SeatEntity) {
               if (KeyBinds.KEY_ADJUST_SEAT_UP.isDown()) {
                  CreateTypePacketHandler.getChannel().sendToServer(new SeatAdjustPacket(v.getId(), true));
                  mc.player.playSound(SoundEvents.LEVER_CLICK, 1.0F, 1.0F);
               }

               if (KeyBinds.KEY_ADJUST_SEAT_DOWN.isDown()) {
                  CreateTypePacketHandler.getChannel().sendToServer(new SeatAdjustPacket(v.getId(), false));
                  mc.player.playSound(SoundEvents.LEVER_CLICK, 1.0F, 1.0F);
               }
            }

            byte map = 0;
            if (KeyBinds.KEY_FUNCTION_1.isDown()) {
               map = (byte)(map | 1);
            }

            if (KeyBinds.KEY_FUNCTION_2.isDown()) {
               map = (byte)(map | 2);
            }

            if (KeyBinds.KEY_FUNCTION_3.isDown()) {
               map = (byte)(map | 4);
            }

            if (KeyBinds.KEY_FUNCTION_4.isDown()) {
               map = (byte)(map | 8);
            }

            if (CameraClientHandler.getPlayerMountedOnCamera() instanceof IClientKeyConsumer consumer) {
               consumer.onKeyPressed(map);
            }

            ShipStoresManagement.onKeyPressed(event, map);
         }
      }
   }
}
