package edn.stratodonut.tallyho.camera.item;

import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.camera.entity.PeriscopeEntity;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class PeriscopeArcItem extends Item {

    public PeriscopeArcItem(Item.Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        }

        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            Player player = context.getPlayer();

            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                Direction side = context.getHorizontalDirection().getOpposite();
                ServerShip serverShip = VSGameUtilsKt.getShipManagingPos(serverLevel, pos);

                if (serverShip != null) {
                    Vec3 blockToPlayer = VectorConversionsMCKt.toMinecraft(
                        serverShip.getWorldToShip()
                            .transformPosition(VectorConversionsMCKt.toJOMLD(serverPlayer.blockPosition()))
                            .sub(VectorConversionsMCKt.toJOMLD(pos))
                            .normalize()
                    );
                    side = Direction.getNearest(blockToPlayer.x, blockToPlayer.y, blockToPlayer.z);
                }

                Vector3f directionVector = side.getOpposite().step();
                directionVector.mul(0.375F);
                Vec3 offset = Vec3.atCenterOf(pos).add(new Vec3(directionVector));

                BlockHitResult hitResult = serverLevel.clip(new ClipContext(
                    offset.add(0.0D, 1.25D, 0.0D),
                    offset,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    null
                ));

                if (hitResult.getType() == BlockHitResult.Type.BLOCK) {
                    serverPlayer.displayClientMessage(
                        Component.literal("Cannot place periscope here, Position is blocked!"),
                        true
                    );
                    return InteractionResult.CONSUME;
                }

                PeriscopeEntity.spawnArc(serverLevel, hitResult.getLocation(), side.toYRot());

                if (!context.getPlayer().isCreative()) {
                    context.getItemInHand().shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.SUCCESS;
    }
}