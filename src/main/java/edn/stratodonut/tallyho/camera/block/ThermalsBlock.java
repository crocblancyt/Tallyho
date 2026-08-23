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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ThermalsBlock extends PeriscopeArcBlock {

    public ThermalsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                   Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                Direction facing = state.getValue(FACING).getOpposite();
                BlockState blockState = serverLevel.getBlockState(pos);

                while (!blockState.isAir()) {
                    pos = pos.relative(Direction.UP);
                    blockState = serverLevel.getBlockState(pos);
                }

                ThermalsEntity thermalsEntity = ThermalsEntity.spawn(
                        serverLevel,
                        pos,
                        facing.toYRot(),
                        new AngleLimits(25.0F, 25.0F, 25.0F),
                        -1
                );
                thermalsEntity.startViewing(serverPlayer);
            }
        }

        return InteractionResult.SUCCESS;
    }
}