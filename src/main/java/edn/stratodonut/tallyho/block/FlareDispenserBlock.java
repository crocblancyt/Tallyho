package edn.stratodonut.tallyho.block;

import edn.stratodonut.tallyho.entity.FlareEntity;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class FlareDispenserBlock extends DirectionalBlock {
    public static final Random random = new Random();

    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public FlareDispenserBlock(BlockBehaviour.Properties properties) {
      super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction facing = context.getNearestLookingDirection();
      return this.defaultBlockState()
              .setValue(LIT, false)
              .setValue(FACING, facing);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                  Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide()) {
            boolean isPowered = level.hasNeighborSignal(pos);
            if (isPowered != state.getValue(LIT)) {
                if (isPowered) {
                    Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);
                    if (ship instanceof ServerShip) {
                        ServerShip serverShip = (ServerShip) ship;
                        Direction facing = state.getValue(FACING);
                        FlareEntity.dropFromShip(level, serverShip, pos,
                                Vec3.atLowerCornerOf(facing.getNormal()).scale(2.0D));
                    }
                }
                level.setBlock(pos, state.setValue(LIT, isPowered), 3);
            }
        }
    }
}