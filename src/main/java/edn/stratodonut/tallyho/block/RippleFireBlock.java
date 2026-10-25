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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class RippleFireBlock extends Block {
   Random random = new Random();
   public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

   public RippleFireBlock(Properties p_49795_) {
      super(p_49795_);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LIT, false));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext p_55659_) {
      return (BlockState)this.defaultBlockState().setValue(LIT, p_55659_.getLevel().hasNeighborSignal(p_55659_.getClickedPos()));
   }

   public void neighborChanged(BlockState p_55666_, Level level, BlockPos pos, Block p_55669_, BlockPos p_55670_, boolean p_55671_) {
      if (!level.isClientSide) {
         boolean flag = level.hasNeighborSignal(pos);
         if (flag != (Boolean)p_55666_.getValue(LIT)) {
            if (flag) {
               Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
               if (s != null) {
                  Vec3 worldPos = VectorConversionsMCKt.toMinecraft(s.getShipToWorld().transformPosition(VectorConversionsMCKt.toJOML(Vec3.atCenterOf(pos))));
                  List<Entity> missiles = level.getEntities(
                     (Entity)null, AABB.ofSize(worldPos, 7.0, 7.0, 7.0), ex -> ex instanceof MountedMissileEntity && ex.isPassenger()
                  );
                  if (missiles.isEmpty()) {
                     return;
                  }

                  Entity e = missiles.get((int)(this.random.nextFloat() * (missiles.size() - 1)));
                  if (e instanceof MountedMissileEntity m) {
                     GuidanceComponent g = m.getGuidance();
                     if (g != null && s.getSlug() != null) {
                        g.setCode(s.getSlug());
                     }

                     m.launch();
                  }
               }
            }

            level.setBlock(pos, (BlockState)p_55666_.setValue(LIT, flag), 3);
         }
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> p_55673_) {
      p_55673_.add(new Property[]{LIT});
   }
}
