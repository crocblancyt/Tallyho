package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.camera.CameraPossessionController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
   @Inject(method = "handleKeybinds", at = @At("HEAD"))
   private void railways$handleStart(CallbackInfo ci) {
      CameraPossessionController.onHandleKeybinds((Minecraft) (Object) this, true);
   }

   @Inject(method = "handleKeybinds", at = @At("RETURN"))
   private void railways$handleEnd(CallbackInfo ci) {
      CameraPossessionController.onHandleKeybinds((Minecraft) (Object) this, false);
   }
}
