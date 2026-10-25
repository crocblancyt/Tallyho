package edn.stratodonut.tallyho.camera.block;

import com.simibubi.create.AllShapes;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.CannonScopeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScopeBlock extends HorizontalDirectionalBlock {
   private Integer camId;

   public ScopeBlock(Properties p) {
      super(p);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }

      if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
         Direction facing = ((Direction)state.getValue(FACING)).getOpposite();

         for (BlockState blockState = serverLevel.getBlockState(pos); !blockState.isAir(); blockState = serverLevel.getBlockState(pos)) {
            pos = pos.relative(facing);
         }

         CameraEntity2 c = CannonScopeEntity.spawn(serverLevel, pos, facing.toYRot());
         c.startViewing(serverPlayer);
         this.camId = c.getId();
      }

      return InteractionResult.SUCCESS;
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

   public Integer getCamId() {
      return this.camId;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return AllShapes.PLACARD.get(getConnectedDirection(pState));
   }
}
