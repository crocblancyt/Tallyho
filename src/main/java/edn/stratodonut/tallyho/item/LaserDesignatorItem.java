package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.entity.LaserPointEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class LaserDesignatorItem extends SpyglassItem {
   public LaserDesignatorItem(Properties p_41383_) {
      super(p_41383_);
   }

   public static void designate(ItemStack stack, boolean active) {
      stack.getOrCreateTag().putBoolean("Active", active);
   }

   public static boolean isActiveDesignating(ItemStack stack) {
      CompoundTag nbt = stack.getOrCreateTag();
      return nbt.contains("Active") && nbt.getBoolean("Active");
   }

   public ItemStack finishUsingItem(ItemStack stack, Level p_151210_, LivingEntity p_151211_) {
      stack.getOrCreateTag().putBoolean("Active", false);
      return super.finishUsingItem(stack, p_151210_, p_151211_);
   }

   public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
      super.onUseTick(level, player, stack, count);
      CompoundTag nbt = stack.getOrCreateTag();
      if (!player.level().isClientSide && isActiveDesignating(stack)) {
         LaserPointEntity.shoot(
            player.level(), player.getEyePosition(), player.getLookAngle(), nbt.contains("Code") ? nbt.getString("Code") : player.getName().getString()
         );
         stack.setDamageValue(stack.getDamageValue() + 1);
      }
   }

   public int getMaxDamage(ItemStack stack) {
      return 5000;
   }
}
