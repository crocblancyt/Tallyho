package edn.stratodonut.tallyho.clip;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PiercingClipContext extends ClipContext {
   private final Predicate<BlockState> piercingPredicate;

   public PiercingClipContext(Vec3 p_45688_, Vec3 p_45689_, Block p_45690_, Fluid p_45691_, @Nullable Entity p_45692_, Predicate<BlockState> piercePredicate) {
      super(p_45688_, p_45689_, p_45690_, p_45691_, p_45692_);
      this.piercingPredicate = piercePredicate;
   }

   public VoxelShape getBlockShape(BlockState p_45695_, BlockGetter p_45696_, BlockPos p_45697_) {
      return this.piercingPredicate.test(p_45695_) ? Shapes.empty() : super.getBlockShape(p_45695_, p_45696_, p_45697_);
   }
}
