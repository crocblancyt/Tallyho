package edn.stratodonut.tallyho.mixin.vpb;

import com.vicmatskiv.pointblank.item.AmmoItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = {AmmoItem.Builder.class}, remap = false)
public abstract class MixinAmmoBuilder {
    @Shadow
    private double initialVelocity;

    @Shadow
    private String name;

    @Shadow
    private double gravity;

    @Inject(method = {"withProjectileInitialVelocity"}, at = {@At("TAIL")})
    private void mixinGrenadeVelocity(double initialVelocity, CallbackInfoReturnable<AmmoItem.Builder> cir) {
        if (isGrenade())
            this.initialVelocity = 1.5D * initialVelocity;
    }

    @Inject(method = {"withProjectileGravity"}, at = {@At("TAIL")})
    private void mixinGrenadeGravity(double initialGravity, CallbackInfoReturnable<AmmoItem.Builder> cir) {
        if (isGrenade())
            this.gravity = initialGravity / 2.0D;
    }

    @Unique
    private boolean isGrenade() {
        if (this.name == null)
            return false;
        switch (this.name) {
            case "grenade40mm":
            case "grenade20mm":
                return true;
            default:
                return false;
        }
    }
}