package edn.stratodonut.tallyho.mixin.mount;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrcrayfish.guns.client.handler.RecoilHandler;
import com.mrcrayfish.guns.common.Gun;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Deprecated
@Mixin({RecoilHandler.class})
public abstract class MixinRecoilHandler {
    @WrapOperation(method = {"onGunFire"}, at = {@At(value = "INVOKE", target = "Lcom/mrcrayfish/guns/common/Gun$General;getRecoilAngle()F")}, remap = false)
    private float mixinOnGunFire(Gun.General instance, Operation<Float> original) {
        Minecraft mc = Minecraft.getInstance();
        float raw = original.call(instance);
        if (mc.player != null) {
            if (mc.player.getVehicle() instanceof edn.stratodonut.tallyho.entity.GunMountEntity)
                raw /= 20.0F;
            if (mc.player.isAutoSpinAttack())
                raw /= 5.0F;
            if (mc.player.isCrouching())
                raw /= 2.0F;
        }
        return raw;
    }
}