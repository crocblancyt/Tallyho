package edn.stratodonut.tallyho.camera;

import com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.mixin.camera_possession.AccessorKeyMapping;
import edn.stratodonut.tallyho.network.CameraMovePacket;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.DismountCameraPacket;
import java.util.Arrays;
import javax.annotation.Nonnull;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class CameraPossessionController {
    @OnlyIn(Dist.CLIENT)
    private static ClientChunkCache.Storage cameraStorage;

    private static boolean wasUpPressed;
    private static boolean wasDownPressed;
    private static boolean wasLeftPressed;
    private static boolean wasRightPressed;
    private static boolean wasJumpPressed;
    private static boolean wasSprintPressed;
    private static boolean wasMounted;
    private static final boolean[] wasMouseClicked = new boolean[3];
    private static final boolean[] wasMousePressed = new boolean[3];
    private static boolean wasUsingBefore;
    private static int ticksSincePacket = 0;
    private static int positionReminder = 0;

    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(Minecraft mc, boolean start) {
        Entity cameraEntity = mc.player.getVehicle();
        if (cameraEntity instanceof CameraEntity2) {
            CameraEntity2 cam = (CameraEntity2)cameraEntity;
            wasMounted = true;
            Options options = mc.options;
            if (start) {
                if (wasUpPressed = options.keyUp.isDown())
                    options.keyUp.setDown(false);
                if (wasDownPressed = options.keyDown.isDown())
                    options.keyDown.setDown(false);
                if (wasLeftPressed = options.keyLeft.isDown())
                    options.keyLeft.setDown(false);
                if (wasRightPressed = options.keyRight.isDown())
                    options.keyRight.setDown(false);
                if (wasJumpPressed = options.keyJump.isDown())
                    options.keyJump.setDown(false);
                Arrays.fill(wasMouseClicked, false);
                while (options.keyAttack.consumeClick())
                    wasMouseClicked[0] = true;
                while (options.keyPickItem.consumeClick())
                    wasMouseClicked[2] = true;
                wasSprintPressed = options.keySprint.isDown();
                if (options.keyShift.isDown() && LinkedControllerClientHandler.MODE != LinkedControllerClientHandler.Mode.ACTIVE) {
                    dismount();
                    options.keyShift.setDown(false);
                }
            } else {
                if (wasUpPressed)
                    options.keyUp.setDown(true);
                if (wasDownPressed)
                    options.keyDown.setDown(true);
                if (wasLeftPressed)
                    options.keyLeft.setDown(true);
                if (wasRightPressed)
                    options.keyRight.setDown(true);
                if (wasJumpPressed)
                    options.keyJump.setDown(true);
                if (wasMouseClicked[2])
                    KeyMapping.click(((AccessorKeyMapping)options.keyPickItem).getKey());
                LocalPlayer player = mc.player;
                double yRotChange = (player.getYRot() - player.yRotLast);
                double xRotChange = (player.getXRot() - player.xRotLast);
                if (yRotChange != 0.0D || xRotChange != 0.0D || ++ticksSincePacket > 10) {
                    ticksSincePacket = 0;
                    player.connection.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
                    player.connection.send(new ServerboundPlayerInputPacket(0.0F, 0.0F, false, false));
                }
                double d = cam.getX() - cam.xLast;
                double e = cam.getY() - cam.yLast;
                double f = cam.getZ() - cam.zLast;
                double g = cam.getYRot() - cam.yRotLast;
                double h = cam.getXRot() - cam.xRotLast;
                positionReminder++;
                boolean bl3 = (Mth.lengthSquared(d, e, f) > Mth.square(2.0E-4D) || positionReminder >= 20);
                boolean bl5 = (g != 0.0D || h != 0.0D);
                if (bl3 || bl5)
                    CreateTypePacketHandler.getChannel().sendToServer(new CameraMovePacket(cam, new ServerboundMovePlayerPacket.PosRot(cam
                            .getX(), cam.getY(), cam.getZ(), cam
                            .getYRot(), cam.getXRot(), cam.onGround())));
            }
        } else if (wasMounted) {
            wasMounted = false;
            dismount();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void onHandleKeybinds(Minecraft mc, boolean start) {
        Entity cameraEntity = mc.cameraEntity;
        if (cameraEntity instanceof CameraEntity2) {
            CameraEntity2 cam = (CameraEntity2)cameraEntity;
            wasMounted = true;
            Options options = mc.options;
            if (start) {
                Arrays.fill(wasMouseClicked, false);
                Arrays.fill(wasMousePressed, false);
                while (options.keyAttack.consumeClick())
                    wasMouseClicked[0] = true;
                wasMousePressed[0] = options.keyAttack.isDown();
                if (options.keyAttack.isDown())
                    options.keyAttack.setDown(false);
            } else {
                if (wasMouseClicked[0])
                    KeyMapping.click(((AccessorKeyMapping)options.keyAttack).getKey());
                if (wasMousePressed[0])
                    options.keyAttack.setDown(true);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void dismount() {
        CreateTypePacketHandler.getChannel().sendToServer(new DismountCameraPacket());
        wasMounted = false;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static ClientChunkCache.Storage getCameraStorage() {
        return cameraStorage;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setCameraStorage(ClientChunkCache.Storage newStorage) {
        cameraStorage = newStorage;
    }


    @OnlyIn(Dist.CLIENT)
    public static void setRenderPosition(Entity entity) {
        if (entity instanceof edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity) {
            SectionPos cameraPos = SectionPos.of(entity);
            cameraStorage.viewCenterX = cameraPos.x();
            cameraStorage.viewCenterZ = cameraPos.z();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void tryUpdatePossession(CameraEntity2 cam) {
        if (CameraClientHandler.getPlayerMountedOnCamera() == cam)
            setRenderPosition(cam);
    }

    public static boolean isPossessingConductor(Entity entity) {
        Player player;
        if (entity instanceof Player) {
            player = (Player)entity;
        } else {
            return false;
        }
        if ((player.level()).isClientSide)
            return CameraClientHandler.isPlayerMountedOnCamera();
        return ((ServerPlayer) player).getCamera() instanceof CameraEntity2;
    }

    @Nullable
    public static CameraEntity2 getPossessingConductor(Entity entity) {
        Player player;
        if (entity instanceof Player) {
            player = (Player)entity;
        } else {
            return null;
        }
        if ((player.level()).isClientSide)
            return CameraClientHandler.getPlayerMountedOnCamera();
        Entity entity1 = player.getVehicle();
        CameraEntity2 ce = (CameraEntity2)entity1;
        return (entity1 != null) ? ce : null;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean wasUpPressed() {
        return wasUpPressed;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean wasDownPressed() {
        return wasDownPressed;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean wasLeftPressed() {
        return wasLeftPressed;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean wasRightPressed() {
        return wasRightPressed;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean wasSprintPressed() {
        return wasSprintPressed;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean wasJumpPressed() {
        return wasJumpPressed;
    }
}