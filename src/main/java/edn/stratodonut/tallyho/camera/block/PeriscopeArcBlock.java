package edn.stratodonut.tallyho.camera.block;

import com.simibubi.create.AllShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class PeriscopeArcBlock extends HorizontalDirectionalBlock {
   public PeriscopeArcBlock(Properties p) {
      super(p);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder.add(new Property[]{FaceAttachedHorizontalDirectionalBlock.FACE, FACING}));
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return canAttachLenient(pLevel, pPos, getConnectedDirection(pState).getOpposite());
   }

   public static boolean canAttachLenient(LevelReader pReader, BlockPos pPos, Direction pDirection) {
      BlockPos blockpos = pPos.relative(pDirection);
      return !pReader.getBlockState(blockpos).getCollisionShape(pReader, blockpos).isEmpty();
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      for (Direction direction : pContext.getNearestLookingDirections()) {
         BlockState blockstate;
         if (direction.getAxis() == Axis.Y) {
            blockstate = (BlockState)((BlockState)this.defaultBlockState()
                  .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR))
               .setValue(FACING, pContext.getHorizontalDirection());
         } else {
            blockstate = (BlockState)((BlockState)this.defaultBlockState().setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL))
               .setValue(FACING, direction.getOpposite());
         }

         if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
            return blockstate;
         }
      }

      return null;
   }

   protected static Direction getConnectedDirection(BlockState p_53201_) {
      switch ((AttachFace)p_53201_.getValue(FaceAttachedHorizontalDirectionalBlock.FACE)) {
         case CEILING:
            return Direction.DOWN;
         case FLOOR:
            return Direction.UP;
         default:
            return (Direction)p_53201_.getValue(FACING);
      }
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return AllShapes.PLACARD.get(getConnectedDirection(pState));
   }
}
