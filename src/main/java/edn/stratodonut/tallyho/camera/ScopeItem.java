package edn.stratodonut.tallyho.camera;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated(forRemoval = true)
public class ScopeItem extends Item {
   public ScopeItem(Properties p) {
      super(p);
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }

      if (level instanceof ServerLevel serverLevel && ctx.getPlayer() instanceof ServerPlayer serverPlayer) {
         Direction side = ctx.getClickedFace();

         for (BlockState blockState = serverLevel.getBlockState(pos); !blockState.isAir(); blockState = serverLevel.getBlockState(pos)) {
            pos = pos.relative(side.getOpposite());
         }
      }

      return InteractionResult.SUCCESS;
   }
}
