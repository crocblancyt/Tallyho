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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class FlareDispenserBlock extends DirectionalBlock {
   public static final Random random = new Random();
   public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

   public FlareDispenserBlock(Properties p_49795_) {
      super(p_49795_);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> p_55673_) {
      super.createBlockStateDefinition(p_55673_);
      p_55673_.add(new Property[]{LIT});
      p_55673_.add(new Property[]{FACING});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction facing = context.getClickedFace();
      return (BlockState)((BlockState)this.defaultBlockState().setValue(LIT, false)).setValue(FACING, facing);
   }

   public void neighborChanged(BlockState p_55666_, Level level, BlockPos pos, Block p_55669_, BlockPos p_55670_, boolean p_55671_) {
      if (!level.isClientSide) {
         boolean flag = level.hasNeighborSignal(pos);
         if (flag != (Boolean)p_55666_.getValue(LIT)) {
            if (flag && VSGameUtilsKt.getShipManagingPos(level, pos) instanceof ServerShip serverShip) {
               Direction dir = (Direction)p_55666_.getValue(FACING);
               FlareEntity.dropFromShip(level, serverShip, pos, Vec3.atLowerCornerOf(dir.getNormal()).scale(2.0));
            }

            level.setBlock(pos, (BlockState)p_55666_.setValue(LIT, flag), 3);
         }
      }
   }
}
