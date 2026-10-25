package edn.stratodonut.tallyho.mixin.vpb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.vicmatskiv.pointblank.util.HitScan;
import edn.stratodonut.tallyho.clip.PiercingClipContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(HitScan.class)
public abstract class MixinHitScan {
   @ModifyConstant(
      method = "getNearestObjectInCrosshair(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;FDLjava/util/function/Predicate;Ljava/util/function/Predicate;Ljava/util/List;)Lnet/minecraft/world/phys/HitResult;",
      constant = @Constant(doubleValue = 0.3),
      remap = false
   )
   private static double doNotInflate(double constant) {
      return 0.1;
   }

   @WrapOperation(
      method = "getNearestObjectInCrosshair(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;FDLjava/util/function/Predicate;Ljava/util/function/Predicate;Ljava/util/List;)Lnet/minecraft/world/phys/HitResult;",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"
      )
   )
   private static BlockHitResult mixinClip(Level instance, ClipContext context, Operation<BlockHitResult> original, @Local LivingEntity player) {
      return (BlockHitResult)original.call(
         new Object[]{
            instance,
            new PiercingClipContext(context.getFrom(), context.getTo(), Block.COLLIDER, Fluid.NONE, player, blockState -> blockState.is(BlockTags.LEAVES))
         }
      );
   }
}
