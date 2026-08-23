package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.entity.GunMountEntity;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class GunMountItem extends Item {
    public GunMountItem(Item.Properties properties) {
        super(properties);
    }

    @Nonnull
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (level.isClientSide)
            return InteractionResult.CONSUME;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Player player = ctx.getPlayer();
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                Direction face = ctx.getClickedFace();
                Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
                Vector3d lookInShip = VectorConversionsMCKt.toJOML(serverPlayer.getLookAngle());
                if (s != null)
                    lookInShip = s.getWorldToShip().transformDirection(lookInShip);
                Direction facing = Direction.getNearest(lookInShip.x, 0.0D, lookInShip.z);
                Vec3 offset = gridify(ctx.getClickLocation());
                GunMountEntity.spawn(serverLevel, offset, facing.toYRot());
                if (!ctx.getPlayer().isCreative()) {
                    ctx.getItemInHand().shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static Vec3 gridify(Vec3 original) {
        return new Vec3(Math.round(original.x * 2.0D) / 2.0D, original.y,
            Math.round(original.z * 2.0D) / 2.0D);
    }
}