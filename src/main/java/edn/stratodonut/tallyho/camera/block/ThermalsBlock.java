package edn.stratodonut.tallyho.camera.block;

import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.entity.ThermalsEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public class ThermalsBlock extends PeriscopeArcBlock {
   public ThermalsBlock(Properties p) {
      super(p);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }

      if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
         Direction facing = ((Direction)state.getValue(FACING)).getOpposite();

         for (BlockState blockState = serverLevel.getBlockState(pos); !blockState.isAir(); blockState = serverLevel.getBlockState(pos)) {
            pos = pos.relative(Direction.UP);
         }

         ThermalsEntity c = ThermalsEntity.spawn(serverLevel, pos, facing.toYRot(), new AngleLimits(25.0F, 25.0F, 25.0F), -1);
         c.startViewing(serverPlayer);
      }

      return InteractionResult.SUCCESS;
   }
}
