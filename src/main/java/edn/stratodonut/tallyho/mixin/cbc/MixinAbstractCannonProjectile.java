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

@Mixin({AbstractCannonProjectile.class})
public abstract class MixinAbstractCannonProjectile extends Projectile {
    protected MixinAbstractCannonProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = {"clipAndDamage"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;atCenterOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;")})
    private Vec3 mixinImpact(Vec3i bPos, Operation<Vec3> original) {
        if (VSGameUtilsKt.isBlockInShipyard(level(), bPos.getX(), bPos.getY(), bPos.getZ()))
            return VSGameUtilsKt.toWorldCoordinates(level(), original.call(bPos));
        return original.call(bPos);
    }
}