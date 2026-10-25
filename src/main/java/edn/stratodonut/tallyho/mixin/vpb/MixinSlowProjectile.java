package edn.stratodonut.tallyho.mixin.vpb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vicmatskiv.pointblank.entity.SlowProjectile;
import edn.stratodonut.tallyho.clip.PiercingClipContext;
import java.util.function.Predicate;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SlowProjectile.class)
public class MixinSlowProjectile {
   @WrapOperation(
      method = "getHitResultOnMoveOrViewVector",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/HitResult;"
      )
   )
   private HitResult mixinHitResult(Entity entity, Predicate<Entity> canHitEntity, Operation<HitResult> original) {
      Vec3 deltaMovement = entity.getDeltaMovement();
      Level level = entity.level();
      Vec3 vec31 = entity.position();
      Vec3 vec3 = vec31.add(deltaMovement);
      HitResult hitresult = level.clip(new PiercingClipContext(vec31, vec3, Block.COLLIDER, Fluid.NONE, entity, blockState -> blockState.is(BlockTags.LEAVES)));
      if (hitresult.getType() != Type.MISS) {
         vec3 = hitresult.getLocation();
      }

      HitResult hitresult1 = ProjectileUtil.getEntityHitResult(
         level, entity, vec31, vec3, entity.getBoundingBox().expandTowards(deltaMovement).inflate(1.0), canHitEntity
      );
      if (hitresult1 != null) {
         hitresult = hitresult1;
      }

      return hitresult;
   }
}
