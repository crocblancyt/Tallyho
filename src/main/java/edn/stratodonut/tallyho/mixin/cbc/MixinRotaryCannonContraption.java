package edn.stratodonut.tallyho.mixin.cbc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption.Block;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.munitions.autocannon.AbstractAutocannonProjectile;
import riftyboi.cbcmodernwarfare.cannon_control.contraption.MountedRotarycannonContraption;

@Mixin(MountedRotarycannonContraption.class)
public class MixinRotaryCannonContraption {
   @Unique
   private Ship tyh$ship;
   @Unique
   private Vec3 tyh$velFromShip;

   @Inject(
      method = "fireShot",
      at = @At(
         value = "INVOKE",
         target = "Lrbasamoyai/createbigcannons/munitions/autocannon/AutocannonAmmoItem;getAutocannonProjectile(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Lrbasamoyai/createbigcannons/munitions/autocannon/AbstractAutocannonProjectile;"
      ),
      remap = false
   )
   private void mixinInheritShipOnFire(ServerLevel level, PitchOrientedContraptionEntity entity, CallbackInfo ci) {
      if (entity.getController() instanceof Block cpcb) {
         Ship s = VSGameUtilsKt.getShipManagingPos(level, cpcb.getControllerBlockPos());
         if (s == null) {
            return;
         }

         this.tyh$ship = s;
      }
   }

   @WrapOperation(
      method = "fireShot",
      at = @At(
         value = "INVOKE",
         target = "Lrbasamoyai/createbigcannons/munitions/autocannon/AbstractAutocannonProjectile;setPos(Lnet/minecraft/world/phys/Vec3;)V"
      )
   )
   private void mixinInheritShipPos(AbstractAutocannonProjectile instance, Vec3 spawnPos, Operation<Void> original) {
      if (this.tyh$ship != null) {
         spawnPos = VSGameUtilsKt.toWorldCoordinates(this.tyh$ship, spawnPos);
         this.tyh$velFromShip = VectorConversionsMCKt.toMinecraft(this.tyh$ship.getVelocity())
            .add(
               VectorConversionsMCKt.toMinecraft(this.tyh$ship.getOmega())
                  .cross(spawnPos.subtract(VectorConversionsMCKt.toMinecraft(this.tyh$ship.getTransform().getPositionInWorld())))
            )
            .scale(0.05);
         original.call(new Object[]{instance, spawnPos.add(this.tyh$velFromShip.scale(5.0))});
      } else {
         original.call(new Object[]{instance, spawnPos});
      }
   }

   @WrapOperation(
      method = "fireShot",
      at = @At(value = "INVOKE", target = "Lrbasamoyai/createbigcannons/munitions/autocannon/AbstractAutocannonProjectile;shoot(DDDFF)V")
   )
   private void mixinInheritShipVel(AbstractAutocannonProjectile instance, double nx, double ny, double nz, float v, float spread, Operation<Void> original) {
      if (this.tyh$velFromShip != null) {
         Vec3 newVel = VectorConversionsMCKt.toMinecraft(
               this.tyh$ship.getTransform().getShipToWorld().transformDirection(new Vector3d(nx, ny, nz).normalize().mul(v))
            )
            .add(this.tyh$velFromShip);
         original.call(new Object[]{instance, newVel.x, newVel.y, newVel.z, (float)newVel.length(), spread});
         this.tyh$ship = null;
         this.tyh$velFromShip = null;
      } else {
         original.call(new Object[]{instance, nx, ny, nz, v, spread});
      }
   }
}
