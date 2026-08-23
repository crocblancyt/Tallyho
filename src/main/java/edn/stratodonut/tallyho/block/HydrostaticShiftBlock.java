package edn.stratodonut.tallyho.block;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.AbstractEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import edn.stratodonut.tallyho.AllBlockEntities;
import edn.stratodonut.tallyho.TallyhoMod;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;

public class HydrostaticShiftBlock extends AbstractEncasedShaftBlock implements IBE<HydrostaticShiftBlockEntity> {

    public HydrostaticShiftBlock(BlockBehaviour.Properties properties) {
      super(properties);
    }

    protected void detachKinetics(Level level, BlockPos pos, boolean reconnect) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KineticBlockEntity) {
            KineticBlockEntity kbe = (KineticBlockEntity) be;
            RotationPropagator.handleRemoved(level, pos, kbe);
            TallyhoMod.warn("This thing actually does smth - HydrostaticShiftBlock:28");
            if (reconnect) {
                TallyhoMod.warn("This thing actually does smth - HydrostaticShiftBlock:31");
                level.scheduleTick(pos, this, 0, TickPriority.EXTREMELY_HIGH);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal("Remanufactured")
              .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      super.appendHoverText(stack, level, tooltip, flag);
      tooltip.add(Component.literal("Model: DS15FM14/14W-VTD527")
              .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      tooltip.add(Component.literal("MADE IN")
              .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      tooltip.add(Component.literal("The rest is scratched off and unreadable.")
              .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(true)));
    }

    public void onPlace(BlockState state, ServerLevel level, BlockPos pos, BlockState oldState, boolean isMoving) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KineticBlockEntity) {
            KineticBlockEntity kbe = (KineticBlockEntity) be;
            RotationPropagator.handleAdded(level, pos, kbe);
        }
    }

    @Override
    public Class<HydrostaticShiftBlockEntity> getBlockEntityClass() {
        return HydrostaticShiftBlockEntity.class;
    }

    @Override
    public BlockEntityType<HydrostaticShiftBlockEntity> getBlockEntityType() {
        return AllBlockEntities.HYDRO_SHIFT.get();
    }
}