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
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SupportCallerItem extends Item {
   public SupportCallerItem(Properties p_41383_) {
      super(p_41383_);
   }

   public int getUseDuration(ItemStack p_151222_) {
      return 200;
   }

   @NotNull
   public UseAnim getUseAnimation(@NotNull ItemStack p_151224_) {
      return UseAnim.SPYGLASS;
   }

   public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
      ItemStack itemstack = p_41433_.getItemInHand(p_41434_);
      OffmapSupport strike = OffmapSupport.readFromNBT(itemstack.getOrCreateTag());
      if (strike != null) {
         InteractionResultHolder.pass(itemstack);
         p_41433_.startUsingItem(p_41434_);
         return InteractionResultHolder.consume(itemstack);
      } else {
         return InteractionResultHolder.fail(itemstack);
      }
   }

   @NotNull
   public ItemStack finishUsingItem(ItemStack p_41409_, Level p_41410_, LivingEntity p_41411_) {
      OffmapSupport strike = OffmapSupport.readFromNBT(p_41409_.getOrCreateTag());
      if (strike != null && !p_41410_.isClientSide && strike.strike(p_41411_) && p_41411_ instanceof Player player && !player.isCreative()) {
         p_41409_.shrink(1);
         player.getCooldowns().addCooldown(p_41409_.getItem(), 20);
      }

      return p_41409_;
   }

   public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
      super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
      OffmapSupport strike = OffmapSupport.readFromNBT(p_41421_.getOrCreateTag());
      if (strike != null) {
         strike.appendHoverText(p_41423_);
      }

      p_41423_.add(Component.literal("Hold down RMB for 10s").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
   }
}
