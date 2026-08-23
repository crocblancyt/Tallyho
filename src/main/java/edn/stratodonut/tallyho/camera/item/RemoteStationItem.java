package edn.stratodonut.tallyho.camera.item;

import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.camera.entity.RemoteStationEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class RemoteStationItem extends Item {

    public RemoteStationItem(Item.Properties properties) {
        super(properties);
    }

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
                Vec3 offset = context.getClickLocation();

                BlockHitResult hitResult = serverLevel.clip(new ClipContext(
                    offset.add(0.0D, 1.25D, 0.0D),
                    offset,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    null
                ));

                if (hitResult.getType() == BlockHitResult.Type.BLOCK) {
                    serverPlayer.displayClientMessage(
                        Component.literal("Cannot place turret here, Position is blocked!"),
                        true
                    );
                    return InteractionResult.CONSUME;
                }

                Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);
                Vector3d lookInShip = VectorConversionsMCKt.toJOML(serverPlayer.getViewVector(1.0F));
                if (ship != null) {
                    lookInShip = ship.getWorldToShip().transformDirection(lookInShip);
                }

                Direction facing = Direction.getNearest(lookInShip.x, 0.0D, lookInShip.z);
                Vec3 offsetPos = hitResult.getLocation();

                RemoteStationEntity remoteStationEntity = RemoteStationEntity.spawn(
                    serverLevel,
                    gridify(offsetPos),
                    facing.toYRot()
                );

                if (!context.getPlayer().isCreative()) {
                    context.getItemInHand().shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static Vec3 gridify(Vec3 original) {
        return new Vec3(
            Math.round(original.x * 2.0D) / 2.0D,
            original.y,
            Math.round(original.z * 2.0D) / 2.0D
        );
    }
}