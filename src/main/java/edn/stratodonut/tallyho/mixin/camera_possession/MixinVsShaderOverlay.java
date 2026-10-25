package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.compat.shader.VsShipShaderOverlays;
import org.valkyrienskies.mod.compat.shader.VsShaderChunkOverlay.ShipShaderOverlayData;

@Pseudo
@Mixin(value = VsShipShaderOverlays.class, remap = false)
public class MixinVsShaderOverlay {
   @Unique
   private static final ShipShaderOverlayData tyh$HIGHLIGHT = new ShipShaderOverlayData(new float[]{50.0F, 50.0F, 50.0F, 1.0F});

   @Inject(method = "processAllOverlays", at = @At("RETURN"), cancellable = true)
   private static void glowShips(ClientShip ship, CallbackInfoReturnable<Optional<ShipShaderOverlayData>> cir) {
      if (CameraClientHandler.isPlayerMountedOnCamera()) {
         CameraEntity2 ce = CameraClientHandler.getPlayerMountedOnCamera();
         CameraEntity2.PostFXType type = ce.getOverlayType();
         if (type == CameraEntity2.PostFXType.THERMALS_GREEN || type == CameraEntity2.PostFXType.THERMALS_WHITE) {
            cir.setReturnValue(Optional.of(tyh$HIGHLIGHT));
         }
      }
   }
}
