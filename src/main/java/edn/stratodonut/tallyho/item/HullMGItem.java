package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.camera.entity.HullMachineGunEntity;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class HullMGItem extends Item {
    public HullMGItem(Item.Properties properties) {
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
            if (ctx.getPlayer() instanceof net.minecraft.server.level.ServerPlayer) {
                BlockState blockState = level.getBlockState(pos);
                Direction face = ctx.getClickedFace();
                int offset = 2;
                while (!blockState.isAir()) {
                    pos = pos.relative(face.getOpposite());
                    blockState = serverLevel.getBlockState(pos);
                    offset++;
                }
                HullMachineGunEntity.spawn(serverLevel, ctx.getClickedPos(), face.getOpposite().toYRot(), offset);
                if (!ctx.getPlayer().isCreative()) {
                    ctx.getItemInHand().shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}