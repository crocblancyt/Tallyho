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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class MPCDProgrammerItem extends Item {
    public MPCDProgrammerItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (level instanceof ClientLevel) {
            ClientLevel clientLevel = (ClientLevel)level;
            Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
            if (s != null && ctx.getPlayer() != null) {
                ShipStoresManagement.init(clientLevel, s);
                Player p = ctx.getPlayer();
                if (p.isShiftKeyDown())
                    ShipRadarManagement.init(clientLevel, s);
                p.displayClientMessage((Component)Components.literal("Weapon Manager Reprogrammed"), true);
                AllSoundEvents.PECULIAR_BELL_USE.play(p.level(), null, p.getX(), p.getY(), p.getZ(), 0.5F, 1.1F);
            }
        }
        return InteractionResult.CONSUME;
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Components.literal(String.format("Set-up on %s", ShipStoresManagement.getWSlug())));
    }
}