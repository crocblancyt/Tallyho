package edn.stratodonut.tallyho.camera;

import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class CameraClientHandler {
  public static boolean isPlayerMountedOnCamera() {
    return (Minecraft.getInstance()).cameraEntity instanceof CameraEntity2;
  }

  @Nullable
  public static CameraEntity2 getPlayerMountedOnCamera() {
    Entity entity = (Minecraft.getInstance()).cameraEntity;
    if (entity instanceof CameraEntity2 ce) {
      return ce;
    }
    return null;
  }

  public static boolean isPlayerMountedOnRemote() {
    return (Minecraft.getInstance()).cameraEntity instanceof edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity;
  }

  public static boolean isPossessed(CameraEntity2 conductor) {
    return (getPlayerMountedOnCamera() == conductor);
  }
}