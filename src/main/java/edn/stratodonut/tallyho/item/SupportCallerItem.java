package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.supportfire.OffmapSupport;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SupportCallerItem extends Item {
    public SupportCallerItem(Item.Properties properties) {
        super(properties);
    }

    public int getUseDuration(ItemStack stack) {
        return 200;
    }

    @NotNull
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.SPYGLASS;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        OffmapSupport strike = OffmapSupport.readFromNBT(itemstack.getTag());
        if (strike != null) {
            InteractionResultHolder.pass(itemstack);
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
        return InteractionResultHolder.fail(itemstack);
    }

    @NotNull
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        OffmapSupport strike = OffmapSupport.readFromNBT(stack.getTag());
        if (strike != null && !level.isClientSide &&
            strike.strike(entity) && entity instanceof Player) {
            Player player = (Player)entity;
            if (!player.isCreative()) {
                stack.shrink(1);
                player.getCooldowns().addCooldown(stack.getItem(), 20);
            }
        }
        return stack;
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        OffmapSupport strike = OffmapSupport.readFromNBT(stack.getTag());
        if (strike != null)
            strike.appendHoverText(tooltip);
        tooltip.add(Component.literal("Hold down RMB for 10s").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
    }
}