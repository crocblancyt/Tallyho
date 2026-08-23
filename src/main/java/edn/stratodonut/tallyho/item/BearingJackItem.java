package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.block.IJackableBearing;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class BearingJackItem extends Item {
    public BearingJackItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            BlockEntity blockEntity = serverLevel.getBlockEntity(ctx.getClickedPos());
            if (blockEntity instanceof IJackableBearing) {
                IJackableBearing jbe = (IJackableBearing)blockEntity;
                Vec3 offset = ctx.getClickLocation().subtract(Vec3.atCenterOf((Vec3i)pos));
                offset = MissileItem.gridify(offset, 0.0625F);
                jbe.clockworkdev$setOffset(ctx.getClickedFace().getAxis(), offset);
            }
        }
        return InteractionResult.CONSUME;
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Owned by Riley")
            .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        tooltip.add(Component.literal("If found, call me at:")
            .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        tooltip.add(Component.literal("(855) - 9")
            .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withBold(true)));
        tooltip.add(Component.literal("The rest of the sticker has torn off.")
            .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(true)));
    }
}