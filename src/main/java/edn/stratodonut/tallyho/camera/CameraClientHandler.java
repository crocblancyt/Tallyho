package edn.stratodonut.tallyho.camera;

import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class CameraClientHandler {
   public static boolean isPlayerMountedOnCamera() {
      return Minecraft.getInstance().cameraEntity instanceof CameraEntity2;
   }

   @Nullable
   public static CameraEntity2 getPlayerMountedOnCamera() {
      return Minecraft.getInstance().cameraEntity instanceof CameraEntity2 ce ? ce : null;
   }

   public static boolean isPlayerMountedOnRemote() {
      return Minecraft.getInstance().cameraEntity instanceof RemoteCameraEntity;
   }

   public static boolean isPossessed(CameraEntity2 conductor) {
      return getPlayerMountedOnCamera() == conductor;
   }
}
