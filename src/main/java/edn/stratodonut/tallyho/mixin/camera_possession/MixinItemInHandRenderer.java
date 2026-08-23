package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.camera.CameraClientHandler;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class MixinItemInHandRenderer {
    @Inject(method = {"renderHandsWithItems"}, at = {@At("HEAD")}, cancellable = true)
    private void railways$cancelHandRendering(CallbackInfo ci) {
      if (CameraClientHandler.isPlayerMountedOnCamera())
          ci.cancel();
    }
}
