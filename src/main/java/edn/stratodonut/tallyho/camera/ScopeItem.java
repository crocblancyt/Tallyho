package edn.stratodonut.tallyho.camera;

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
import net.minecraft.world.level.block.state.BlockState;

@Deprecated(forRemoval = true)
public class ScopeItem extends Item {
    public ScopeItem(Item.Properties p) {
        super(p);
    }

    @Nonnull
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Player player = ctx.getPlayer();
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                Direction side = ctx.getClickedFace();
                BlockState blockState = serverLevel.getBlockState(pos);
                while (!blockState.isAir()) {
                    pos = pos.relative(side.getOpposite());
                    blockState = serverLevel.getBlockState(pos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}