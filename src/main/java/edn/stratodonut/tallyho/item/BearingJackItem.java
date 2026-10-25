package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.block.IJackableBearing;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BearingJackItem extends Item {
   public BearingJackItem(Properties p_41383_) {
      super(p_41383_);
   }

   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      if (level instanceof ServerLevel serverLevel && serverLevel.getBlockEntity(ctx.getClickedPos()) instanceof IJackableBearing jbe) {
         Vec3 offset = ctx.getClickLocation().subtract(Vec3.atCenterOf(pos));
         offset = MissileItem.gridify(offset, 0.0625F);
         jbe.clockworkdev$setOffset(ctx.getClickedFace().getAxis(), offset);
      }

      return InteractionResult.CONSUME;
   }

   public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
      super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
      p_41423_.add(Component.literal("Owned by Riley").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      p_41423_.add(Component.literal("If found, call me at:").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      p_41423_.add(Component.literal("(855) - 9").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withUnderlined(true)));
      p_41423_.add(Component.literal("The rest of the sticker has torn off.").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(true)));
   }
}
