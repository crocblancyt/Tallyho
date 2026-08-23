package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.entity.TripodEntity;
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
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class TripodMountItem extends Item {
    public TripodMountItem(Item.Properties properties) {
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
            Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
            Player player = ctx.getPlayer();
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                Direction face = ctx.getClickedFace();
                Vector3d lookInShip = VectorConversionsMCKt.toJOML(serverPlayer.getLookAngle());
                if (s != null)
                    lookInShip = s.getWorldToShip().transformDirection(lookInShip);
                float rotationYawHead = (float)(-Math.atan2(lookInShip.x, lookInShip.z) * 180.0D / Math.PI);
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