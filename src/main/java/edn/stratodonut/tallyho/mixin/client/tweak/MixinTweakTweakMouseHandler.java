package edn.stratodonut.tallyho.mixin.client.tweak;

import com.getitemfromblock.create_tweaked_controllers.input.MouseCursorHandler;
import edn.stratodonut.tallyho.mixinducks.TweakMouseHandlerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseCursorHandler.class)
public class MixinTweakTweakMouseHandler implements TweakMouseHandlerDuck {
   @Shadow
   private static boolean mouseLockActive;

   @Inject(method = "CancelPlayerTurn", at = @At("HEAD"), remap = false, cancellable = true)
   private static void cancelTheCancel(CallbackInfo ci) {
      ci.cancel();
   }

   @Override
   public boolean clockworkdev$isActive() {
      return mouseLockActive;
   }
}
