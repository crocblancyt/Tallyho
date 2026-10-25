package edn.stratodonut.tallyho.camera.item;

import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.RemoteStationEntity;
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
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class RemoteStationItem extends Item {
   public RemoteStationItem(Properties p_41383_) {
      super(p_41383_);
   }

   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      if (level.isClientSide) {
         return InteractionResult.CONSUME;
      }

      if (level instanceof ServerLevel serverLevel && ctx.getPlayer() instanceof ServerPlayer serverPlayer) {
         Vec3 offset = ctx.getClickLocation();
         BlockHitResult hit = serverLevel.clip(new ClipContext(offset.add(0.0, 1.25, 0.0), offset, Block.COLLIDER, Fluid.NONE, null));
         if (hit.isInside()) {
            serverPlayer.displayClientMessage(Components.literal("Cannot place turret here, Position is blocked!"), true);
            return InteractionResult.CONSUME;
         }

         Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
         Vector3d lookInShip = VectorConversionsMCKt.toJOML(serverPlayer.getLookAngle());
         if (s != null) {
            lookInShip = s.getWorldToShip().transformDirection(lookInShip);
         }

         Direction facing = Direction.getNearest(lookInShip.x, 0.0, lookInShip.z);
         Vec3 offsetPos = hit.getLocation();
         CameraEntity2 c = RemoteStationEntity.spawn(serverLevel, gridify(offsetPos), facing.toYRot());
         if (!ctx.getPlayer().isCreative()) {
            ctx.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.SUCCESS;
   }

   public static Vec3 gridify(Vec3 original) {
      return new Vec3(Math.round(original.x * 2.0) / 2.0, original.y, Math.round(original.z * 2.0) / 2.0);
   }
}
