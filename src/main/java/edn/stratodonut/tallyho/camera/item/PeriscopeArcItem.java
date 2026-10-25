package edn.stratodonut.tallyho.camera.item;

import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.camera.entity.PeriscopeEntity;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class PeriscopeArcItem extends Item {
   public PeriscopeArcItem(Properties p) {
      super(p);
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      if (level.isClientSide) {
         return InteractionResult.CONSUME;
      }

      if (level instanceof ServerLevel serverLevel && ctx.getPlayer() instanceof ServerPlayer serverPlayer) {
         Direction side = ctx.getHorizontalDirection().getOpposite();
         Ship s = VSGameUtilsKt.getShipManagingPos(serverLevel, pos);
         if (s != null) {
            Vec3 blockToPlayer = VectorConversionsMCKt.toMinecraft(
               s.getWorldToShip()
                  .transformPosition(VectorConversionsMCKt.toJOMLD(serverPlayer.blockPosition()))
                  .sub(VectorConversionsMCKt.toJOMLD(pos))
                  .normalize()
            );
            side = Direction.getNearest(blockToPlayer.x, blockToPlayer.y, blockToPlayer.z);
         }

         Vector3f dir = side.getOpposite().step();
         dir.mul(0.375F);
         Vec3 offset = Vec3.atBottomCenterOf(pos).add(new Vec3(dir));
         BlockHitResult hit = serverLevel.clip(new ClipContext(offset.add(0.0, 1.25, 0.0), offset, Block.COLLIDER, Fluid.NONE, null));
         if (hit.isInside()) {
            serverPlayer.displayClientMessage(Components.literal("Cannot place periscope here, Position is blocked!"), true);
            return InteractionResult.CONSUME;
         }

         PeriscopeEntity.spawnArc(serverLevel, hit.getLocation(), side.toYRot());
         if (!ctx.getPlayer().isCreative()) {
            ctx.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.SUCCESS;
   }
}
