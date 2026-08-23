package edn.stratodonut.tallyho.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.controller.PlayerRecoilController;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({PlayerRecoilController.class})
public abstract class MixinPlayerRecoilController {
    @WrapOperation(method = {"onStartFiring"}, at = {@At(value = "INVOKE", target = "Lcom/vicmatskiv/pointblank/client/GunClientState;isAiming()Z")}, remap = false)
    private boolean mixinStartFiring(GunClientState instance, Operation<Boolean> original, @Local LivingEntity player) {
        return original.call(instance) || player.isAutoSpinAttack();
    }
}