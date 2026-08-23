package edn.stratodonut.tallyho.mixin.cbc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import riftyboi.cbcmodernwarfare.cannons.medium_cannon.breech.MediumcannonBreechBlock;
import riftyboi.cbcmodernwarfare.cannons.medium_cannon.breech.MediumcannonBreechBlockEntity;

@Mixin(value = {MediumcannonBreechBlock.class}, remap = false)
public class MixinMediumCannonBlock {
    @WrapOperation(method = {"onInteractWhileAssembled"}, at = {@At(value = "INVOKE", target = "Lriftyboi/cbcmodernwarfare/cannons/medium_cannon/breech/MediumcannonBreechBlockEntity;isInputFull()Z")})
    private boolean mixinStopSweatsFromSpammingTheReload(MediumcannonBreechBlockEntity instance, Operation<Boolean> original) {
        return ((original.call(new Object[] { instance })).booleanValue() || !instance.canBeAutomaticallyLoaded());
    }
}
