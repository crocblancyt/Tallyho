package edn.stratodonut.tallyho.camera.block;

import com.simibubi.create.AllShapes;
import edn.stratodonut.tallyho.camera.entity.CannonScopeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScopeBlock extends HorizontalDirectionalBlock {

    private Integer camId;

    public ScopeBlock(BlockBehaviour.Properties properties) {
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
                    pos = pos.relative(facing);
                    blockState = serverLevel.getBlockState(pos);
                }

                CannonScopeEntity cannonScopeEntity = CannonScopeEntity.spawn(serverLevel, pos, facing.toYRot());
                cannonScopeEntity.startViewing(serverPlayer);
                this.camId = cannonScopeEntity.getId();
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FaceAttachedHorizontalDirectionalBlock.FACE, FACING);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canAttachLenient(level, pos, getConnectedDirection(state).getOpposite());
    }

    public static boolean canAttachLenient(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        return !level.getBlockState(neighborPos).getShape(level, neighborPos).isEmpty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState blockState;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockState = this.defaultBlockState()
                        .setValue(FaceAttachedHorizontalDirectionalBlock.FACE,
                                direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
                        .setValue(FACING, context.getHorizontalDirection());
            } else {
                blockState = this.defaultBlockState()
                        .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL)
                        .setValue(FACING, direction.getOpposite());
            }
            if (blockState.canSurvive(context.getLevel(), context.getClickedPos())) {
                return blockState;
            }
        }
        return null;
    }

    protected static Direction getConnectedDirection(BlockState state) {
        AttachFace face = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
        switch (face) {
            case CEILING:
                return Direction.DOWN;
            case FLOOR:
                return Direction.UP;
            default:
                return state.getValue(FACING);
        }
    }

    public Integer getCamId() {
        return this.camId;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.PLACARD.get(getConnectedDirection(state));
    }
}