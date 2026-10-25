package edn.stratodonut.tallyho.item;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.missile.ShipRadarManagement;
import edn.stratodonut.tallyho.missile.ShipStoresManagement;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class MPCDProgrammerItem extends Item {
   public MPCDProgrammerItem(Properties p_41383_) {
      super(p_41383_);
   }

   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      if (level instanceof ClientLevel clientLevel) {
         Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
         if (s != null && ctx.getPlayer() != null) {
            ShipStoresManagement.init(clientLevel, s);
            Player p = ctx.getPlayer();
            if (p.hasPermissions(2)) {
               ShipRadarManagement.init(clientLevel, s);
            }

            p.displayClientMessage(Components.literal("Weapon Manager Reprogrammed"), true);
            AllSoundEvents.PECULIAR_BELL_USE.play(p.level(), null, p.getX(), p.getY(), p.getZ(), 0.5F, 1.1F);
         }
      }

      return InteractionResult.CONSUME;
   }

   public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
      super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
      p_41423_.add(Components.literal(String.format("Set-up on %s", ShipStoresManagement.getWSlug())));
   }
}
