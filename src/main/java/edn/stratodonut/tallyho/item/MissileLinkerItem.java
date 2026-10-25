package edn.stratodonut.tallyho.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class MissileLinkerItem extends Item {
   public MissileLinkerItem(Properties p_41383_) {
      super(p_41383_);
   }

   public InteractionResult useOn(UseOnContext ctx) {
      return InteractionResult.CONSUME;
   }

   public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
      super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
   }
}
