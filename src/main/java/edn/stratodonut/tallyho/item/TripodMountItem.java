package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.entity.TripodEntity;
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
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class TripodMountItem extends Item {
   public TripodMountItem(Properties p_41383_) {
      super(p_41383_);
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      if (level.isClientSide) {
         return InteractionResult.CONSUME;
      }

      if (level instanceof ServerLevel serverLevel) {
         Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
         if (ctx.getPlayer() instanceof ServerPlayer player) {
            Direction face = ctx.getClickedFace();
            Vector3d lookInShip = VectorConversionsMCKt.toJOML(player.getLookAngle());
            if (s != null) {
               lookInShip = s.getWorldToShip().transformDirection(lookInShip);
            }

            float rotationYawHead = (float)(-Math.atan2(lookInShip.x, lookInShip.z) * 180.0 / (float) Math.PI);
            TripodEntity.spawn(serverLevel, ctx.getClickedPos().relative(face), rotationYawHead);
            if (!ctx.getPlayer().isCreative()) {
               ctx.getItemInHand().shrink(1);
               return InteractionResult.SUCCESS;
            }
         }
      }

      return InteractionResult.SUCCESS;
   }
}
