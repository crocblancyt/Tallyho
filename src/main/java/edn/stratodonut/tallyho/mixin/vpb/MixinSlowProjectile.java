package edn.stratodonut.tallyho.mixin.vpb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vicmatskiv.pointblank.entity.SlowProjectile;
import edn.stratodonut.tallyho.clip.PiercingClipContext;
import java.util.function.Predicate;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({SlowProjectile.class})
public class MixinSlowProjectile {
    @WrapOperation(method = {"getHitResultOnMoveOrViewVector"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/HitResult;")})
    private HitResult mixinHitResult(Entity entity, Predicate<Entity> canHitEntity, Operation<HitResult> original) {
        Vec3 deltaMovement = entity.getDeltaMovement();
        Level level = entity.level();
        Vec3 start = entity.position();
        Vec3 end = start.add(deltaMovement);
        BlockHitResult blockHitResult = level.clip((ClipContext)new PiercingClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity, blockState -> blockState.is(BlockTags.CLIMBABLE)));
        if (blockHitResult.getType() != HitResult.Type.MISS)
            end = blockHitResult.getLocation();
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(level, entity, start, end, entity.getBoundingBox().expandTowards(deltaMovement).inflate(1.0D), canHitEntity);
        return entityHitResult != null ? entityHitResult : blockHitResult;
    }
}