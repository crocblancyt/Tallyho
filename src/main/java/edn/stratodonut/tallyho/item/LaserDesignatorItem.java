package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.entity.LaserPointEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.level.Level;

public class LaserDesignatorItem extends SpyglassItem {
    public LaserDesignatorItem(Item.Properties properties) {
        super(properties);
    }

    public static void designate(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean("Active", active);
    }

    public static boolean isActiveDesignating(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        return (nbt != null && nbt.contains("Active") && nbt.getBoolean("Active"));
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        stack.getOrCreateTag().putBoolean("Active", false);
        return super.finishUsingItem(stack, level, entity);
    }

    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        super.onUseTick(level, player, stack, count);
        CompoundTag nbt = stack.getTag();
        if (!player.level().isClientSide &&
            isActiveDesignating(stack)) {
            LaserPointEntity.shoot(player.level(), player.getEyePosition(), player.getLookAngle(),
                (nbt != null && nbt.contains("Code")) ? nbt.getString("Code") : player.getDisplayName().getString());
            stack.setDamageValue(stack.getDamageValue() + 1);
        }
    }

    public int getMaxDamage(ItemStack stack) {
        return 5000;
    }
}