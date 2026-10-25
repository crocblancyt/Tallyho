package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.camera.entity.HullMachineGunEntity;
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

public class HullMGItem extends Item {
   public HullMGItem(Properties p_41383_) {
      super(p_41383_);
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      if (level.isClientSide) {
         return InteractionResult.CONSUME;
      }

      if (level instanceof ServerLevel serverLevel && ctx.getPlayer() instanceof ServerPlayer) {
         BlockState blockState = level.getBlockState(pos);
         Direction face = ctx.getClickedFace();

         int offset;
         for (offset = 2; !blockState.isAir(); offset++) {
            pos = pos.relative(face.getOpposite());
            blockState = serverLevel.getBlockState(pos);
         }

         HullMachineGunEntity.spawn(serverLevel, ctx.getClickedPos(), face.getOpposite().toYRot(), offset);
         if (!ctx.getPlayer().isCreative()) {
            ctx.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.SUCCESS;
   }
}
