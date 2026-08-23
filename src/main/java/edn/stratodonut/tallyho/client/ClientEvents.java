package edn.stratodonut.tallyho.client;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.guns.client.handler.CrosshairHandler;
import com.mrcrayfish.guns.client.render.crosshair.Crosshair;
import com.vicmatskiv.pointblank.item.GunItem;
import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import edn.stratodonut.tallyho.missile.ShipRadarManagement;
import edn.stratodonut.tallyho.missile.ShipStoresManagement;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.ServerboundGunFirePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import rbasamoyai.createbigcannons.CBCClientCommon;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber({Dist.CLIENT})
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.START) {
            onClientTickStart(mc);
        } else if (event.phase == TickEvent.Phase.END) {
            onClientTickEnd(mc);
        }
        if (mc.player == null || mc.level == null)
            return;
        LocalPlayer player = mc.player;
        if (isCrawlTransition(player)) {
            if (!CameraClientHandler.isPlayerMountedOnCamera()) {
                mc.player.handsBusy = true;
            }
            ItemStack heldItem = player.getMainHandItem();
            Item item = heldItem.getItem();
            if (item instanceof GunItem) {
                GunItem g = (GunItem)item;
                g.setTriggerOff(player, heldItem);
            }
        } else if (player.getVehicle() == null) {
            mc.player.handsBusy = false;
        }
        if (isControllingMG(mc.player)) {
            if (!CameraClientHandler.isPlayerMountedOnCamera())
                mc.player.handsBusy = true;
            if (CBCClientCommon.FIRE_CONTROLLED_CANNON.isDown()) {
                Entity entity = player.getVehicle();
                if (entity instanceof GunMountEntity) {
                    GunMountEntity mg = (GunMountEntity)entity;
                } else {
                    CreateTypePacketHandler.getChannel().sendToServer(new ServerboundGunFirePacket(0L));
                }
            }
        }
    }

    public static void onClientTickStart(Minecraft mc) {
        if (Backend.isGameActive()) {
            CameraPossessionController.onClientTick(mc, true);
            ShipStoresManagement.tick(mc.level);
            ShipRadarManagement.tick(mc.level);
        }
    }

    public static void onClientTickEnd(Minecraft mc) {
        if (Backend.isGameActive())
            CameraPossessionController.onClientTick(mc, false);
    }

    @SubscribeEvent
    public static void onGetFov(ViewportEvent.ComputeFov f) {
        CameraEntity2 ce = CameraClientHandler.getPlayerMountedOnCamera();
        if (ce != null && ce.getFOV() > 1)
            f.setFOV(ce.getFOV());
    }

    @SubscribeEvent
    public static void onRenderGuiEvent(RenderGuiEvent.Post event) {
        ShipStoresManagement.drawScreen(event.getWindow(), event.getGuiGraphics());
        ShipRadarManagement.drawScreen(event.getWindow(), event.getGuiGraphics());
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Entity entity = mc.player.getVehicle();
            if (entity instanceof GunMountEntity) {
                GunMountEntity mg = (GunMountEntity)entity;
                GunMountTracker.renderOverlay(event.getGuiGraphics(), mg.getMountedGun());
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) {
            Crosshair crosshair = CrosshairHandler.get().getCurrentCrosshair();
            if (crosshair != null && !crosshair.isDefault()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    Entity entity = mc.player.getVehicle();
                    if (entity instanceof GunMountEntity) {
                        GunMountEntity mg = (GunMountEntity)entity;
                        event.setCanceled(true);
                        if (mc.options.sensitivity().get() > 0 &&
                            mc.player.getUseItem().getItem() != Items.SPYGLASS) {
                            PoseStack stack = event.getGuiGraphics().pose();
                            stack.pushPose();
                            int scaledWidth = event.getWindow().getGuiScaledWidth();
                            int scaledHeight = event.getWindow().getGuiScaledHeight();
                            crosshair.render(mc, stack, scaledWidth, scaledHeight, event.getPartialTick());
                            stack.popPose();
                        }
                    }
                }
            }
        }
    }

    private static boolean isCrawlTransition(Player player) {
        return (!player.isInWater() && player.getSwimAmount(1.0F) > 0.01D && player.getSwimAmount(1.0F) < 0.99D);
    }

    public static boolean isControllingMG(Entity entity) {
        CameraEntity2 cameraEntity2 = CameraClientHandler.getPlayerMountedOnCamera();
        Entity vehicle = entity.getVehicle();
        return (cameraEntity2 instanceof edn.stratodonut.tallyho.camera.entity.HullMachineGunEntity || cameraEntity2 instanceof edn.stratodonut.tallyho.camera.entity.HeliChinTurretEntity || cameraEntity2 instanceof edn.stratodonut.tallyho.camera.entity.RemoteStationEntity || vehicle instanceof GunMountEntity);
    }
}