package edn.stratodonut.tallyho.mixin.client;

import com.getitemfromblock.create_tweaked_controllers.input.ModInputEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.AllConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(ModInputEvents.class)
public class MixinTweakedClick {
   @WrapOperation(
      method = "onClickInput",
      at = @At(
         value = "INVOKE",
         target = "Lcom/getitemfromblock/create_tweaked_controllers/controller/TweakedLinkedControllerClientHandler;deactivateInLectern()V"
      ),
      remap = false
   )
   private static void stopReset(Operation<Void> original) {
      if (!(Boolean)AllConfigs.client().disableQuickExit.get()) {
         original.call(new Object[0]);
      }
   }
}
