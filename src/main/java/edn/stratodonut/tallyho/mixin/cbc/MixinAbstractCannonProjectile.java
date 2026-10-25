package edn.stratodonut.tallyho.mixin.cbc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

@Mixin(AbstractCannonProjectile.class)
public abstract class MixinAbstractCannonProjectile extends Projectile {
   protected MixinAbstractCannonProjectile(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
      super(p_37248_, p_37249_);
   }

   @WrapOperation(
      method = "clipAndDamage",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;atCenterOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;")
   )
   private Vec3 mixinImpact(Vec3i bPos, Operation<Vec3> original) {
      return VSGameUtilsKt.isBlockInShipyard(this.level(), bPos.getX(), bPos.getY(), bPos.getZ())
         ? VSGameUtilsKt.toWorldCoordinates(this.level(), (Vec3)original.call(new Object[]{bPos}))
         : (Vec3)original.call(new Object[]{bPos});
   }
}
