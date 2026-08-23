package edn.stratodonut.tallyho.clip;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PiercingClipContext extends ClipContext {
    private final Predicate<BlockState> piercingPredicate;

    public PiercingClipContext(Vec3 from, Vec3 to, ClipContext.Block blockMode, ClipContext.Fluid fluidMode, @Nullable Entity entity, Predicate<BlockState> piercePredicate) {
        super(from, to, blockMode, fluidMode, entity);
        this.piercingPredicate = piercePredicate;
    }

    public VoxelShape getBlockShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (this.piercingPredicate.test(state))
            return Shapes.empty();
        return super.getBlockShape(state, level, pos);
    }
}