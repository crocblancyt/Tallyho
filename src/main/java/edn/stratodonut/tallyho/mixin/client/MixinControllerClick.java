package edn.stratodonut.tallyho.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.events.InputEvents;
import edn.stratodonut.tallyho.AllConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({InputEvents.class})
public class MixinControllerClick {
    @WrapOperation(method = {"onClickInput"}, at = {@At(value = "INVOKE", target = "Lcom/simibubi/create/content/redstone/link/controller/LinkedControllerClientHandler;deactivateInLectern()V")}, remap = false)
    private static void cancelClick(Operation<Void> original) {
        if (((AllConfigs.client()).disableQuickExit.get()).booleanValue())
            return;
        original.call(new Object[0]);
    }
}
