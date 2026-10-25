package edn.stratodonut.tallyho.mixin.vpb;

import com.vicmatskiv.pointblank.item.AmmoItem.Builder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = Builder.class, remap = false)
public abstract class MixinAmmoBuilder {
   @Shadow
   private double initialVelocity;
   @Shadow
   private String name;
   @Shadow
   private double gravity;

   @Inject(method = "withProjectileInitialVelocity", at = @At("TAIL"))
   private void mixinGrenadeVelocity(double initialVelocity, CallbackInfoReturnable<Builder> cir) {
      if (this.tyh$isGrenade()) {
         this.initialVelocity = 1.5 * initialVelocity;
      }
   }

   @Inject(method = "withProjectileGravity", at = @At("TAIL"))
   private void mixinGrenadeGravity(double initialGravity, CallbackInfoReturnable<Builder> cir) {
      if (this.tyh$isGrenade()) {
         this.gravity = initialGravity / 2.0;
      }
   }

   @Unique
   private boolean tyh$isGrenade() {
      if (this.name == null) {
         return false;
      }

      return switch (this.name) {
         case "grenade40mm", "grenade20mm" -> true;
         default -> false;
      };
   }
}
