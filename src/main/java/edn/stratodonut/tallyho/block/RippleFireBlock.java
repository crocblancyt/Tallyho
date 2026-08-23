package edn.stratodonut.tallyho.block;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.GuidanceComponent;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class RippleFireBlock extends Block {

    Random random = new Random();

    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public RippleFireBlock(BlockBehaviour.Properties properties) {
      super(properties);
      this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.defaultBlockState().setValue(LIT,
              context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                  Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide()) {
            boolean isPowered = level.hasNeighborSignal(pos);
            if (isPowered != state.getValue(LIT)) {
                if (isPowered) {
                    Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);
                    if (ship != null) {
                        Vec3 worldPos = VectorConversionsMCKt.toMinecraft(
                                ship.getShipToWorld().transformPosition(
                                        VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(pos))
                                )
                        );

                        List<Entity> missiles = level.getEntities((Entity) null,
                                AABB.ofSize(worldPos, 7.0D, 7.0D, 7.0D),
                                entity -> (entity instanceof MountedMissileEntity && entity.isPassenger())
                        );

                        if (missiles.isEmpty()) return;

                        Entity target = missiles.get((int)(this.random.nextFloat() * (missiles.size() - 1)));

                        if (target instanceof MountedMissileEntity) {
                            MountedMissileEntity missile = (MountedMissileEntity) target;
                            GuidanceComponent guidance = missile.getGuidance();
                            if (guidance != null && ship.getSlug() != null) {
                                guidance.setCode(ship.getSlug());
                            }
                            missile.launch();
                      }
                  }
              }
              level.setBlock(pos, state.setValue(LIT, isPowered), 3);
          }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}