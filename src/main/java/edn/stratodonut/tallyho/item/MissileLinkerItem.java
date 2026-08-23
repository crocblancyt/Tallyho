package edn.stratodonut.tallyho.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class MissileLinkerItem extends Item {
    public MissileLinkerItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResult useOn(UseOnContext ctx) {
        return InteractionResult.CONSUME;
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
    }
}