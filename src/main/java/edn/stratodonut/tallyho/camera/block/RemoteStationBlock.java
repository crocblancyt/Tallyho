package edn.stratodonut.tallyho.camera.block;

import com.simibubi.create.AllShapes;
import edn.stratodonut.tallyho.camera.entity.RemoteStationEntity;
import java.util.List;
import java.util.function.Predicate;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class RemoteStationBlock extends HorizontalDirectionalBlock {
   public RemoteStationBlock(Properties p) {
      super(p);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (level.isClientSide) {
         return InteractionResult.CONSUME;
      }

      if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
         Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
         Vec3 worldPos = Vec3.atCenterOf(pos);
         Predicate<RemoteStationEntity> pred = rse -> !rse.isPossessed();
         List<RemoteStationEntity> c = serverLevel.getEntitiesOfClass(RemoteStationEntity.class, AABB.ofSize(worldPos, 5.0, 5.0, 5.0), pred);
         if (c.isEmpty()) {
            return InteractionResult.CONSUME;
         }

         c.get(0).startViewing(serverPlayer);
      }

      return InteractionResult.SUCCESS;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder.add(new Property[]{FaceAttachedHorizontalDirectionalBlock.FACE, FACING}));
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
