package edn.stratodonut.tallyho;

import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.ILaserDesignator;
import edn.stratodonut.tallyho.item.LaserDesignatorItem;
import edn.stratodonut.tallyho.missile.ShipStoresManagement;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.DesignatePacket;
import edn.stratodonut.tallyho.network.GravitronStaticPacket;
import edn.stratodonut.tallyho.network.SeatAdjustPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.valkyrienskies.clockwork.ClockworkItems;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.CreativeGravitronItem;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.GravitronItem;

public class KeypressHandler {
    private static KeypressHandler instance;

    public static KeypressHandler get() {
        if (instance == null)
            instance = new KeypressHandler();
        return instance;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.END && mc.player != null) {
            CameraEntity2 cameraEntity2 = CameraClientHandler.getPlayerMountedOnCamera();
            ItemStack stack = mc.player.getUseItem();
            if (cameraEntity2 instanceof ILaserDesignator) {
                ILaserDesignator d = (ILaserDesignator)cameraEntity2;
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
    public void onMouse(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.getAction() == 1 &&
            event.getButton() == 0) {
            ItemStack stack = mc.player.getMainHandItem();
            boolean bl = stack.is(ClockworkItems.CREATIVE_GRAVITRON.get().asItem());
            boolean bl2 = stack.is(ClockworkItems.GRAVITRON.get().asItem());
            if ((bl2 || bl) && !mc.player.getCooldowns().isOnCooldown(stack.getItem()))
                CreateTypePacketHandler.getChannel().sendToServer(new GravitronStaticPacket());
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            LocalPlayer localPlayer = mc.player;
            if (event.getAction() == 1) {
                Entity v = localPlayer.getVehicle();
                if (v instanceof com.simibubi.create.content.contraptions.actors.seat.SeatEntity) {
                    if (KeyBinds.KEY_ADJUST_SEAT_UP.isDown()) {
                        CreateTypePacketHandler.getChannel().sendToServer(new SeatAdjustPacket(v.getId(), true));
                        mc.player.playSound(SoundEvents.WOODEN_TRAPDOOR_CLOSE, 1.0F, 1.0F);
                    }
                    if (KeyBinds.KEY_ADJUST_SEAT_DOWN.isDown()) {
                        CreateTypePacketHandler.getChannel().sendToServer(new SeatAdjustPacket(v.getId(), false));
                        mc.player.playSound(SoundEvents.WOODEN_TRAPDOOR_CLOSE, 1.0F, 1.0F);
                    }
                }
                byte map = 0;
                if (KeyBinds.KEY_FUNCTION_1.isDown())
                    map = (byte)(map | 0x1);
                if (KeyBinds.KEY_FUNCTION_2.isDown())
                    map = (byte)(map | 0x2);
                if (KeyBinds.KEY_FUNCTION_3.isDown())
                    map = (byte)(map | 0x4);
                if (KeyBinds.KEY_FUNCTION_4.isDown())
                    map = (byte)(map | 0x8);
                CameraEntity2 cameraEntity2 = CameraClientHandler.getPlayerMountedOnCamera();
                if (cameraEntity2 instanceof IClientKeyConsumer) {
                    IClientKeyConsumer consumer = (IClientKeyConsumer)cameraEntity2;
                    consumer.onKeyPressed(map);
                }
                ShipStoresManagement.onKeyPressed(event, map);
            }
        }
    }
}