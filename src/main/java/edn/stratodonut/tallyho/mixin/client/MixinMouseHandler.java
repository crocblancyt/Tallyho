package edn.stratodonut.tallyho.mixin.client;

import com.getitemfromblock.create_tweaked_controllers.input.MouseCursorHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.mixinducks.TweakMouseHandlerDuck;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {
   @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
   private void cancelTurn(LocalPlayer instance, double y, double x, Operation<Void> original) {
      if (!((TweakMouseHandlerDuck)(new MouseCursorHandler())).clockworkdev$isActive()) {
         original.call(new Object[]{instance, y, x});
      }
   }
}
