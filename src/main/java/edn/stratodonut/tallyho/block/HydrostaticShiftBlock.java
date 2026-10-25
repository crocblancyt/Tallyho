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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.ticks.TickPriority;

public class HydrostaticShiftBlock extends AbstractEncasedShaftBlock implements IBE<HydrostaticShiftBlockEntity> {
   public HydrostaticShiftBlock(Properties properties) {
      super(properties);
   }

   protected void detachKinetics(Level level, BlockPos pos, boolean reconnect) {
      if (level.getBlockEntity(pos) instanceof KineticBlockEntity kbe) {
         RotationPropagator.handleRemoved(level, pos, kbe);
         TallyhoMod.warn("This thing actually does smth - HydrostaticShiftBlock:28");
         if (reconnect) {
            TallyhoMod.warn("This thing actually does smth - HydrostaticShiftBlock:31");
            level.scheduleTick(pos, this, 0, TickPriority.EXTREMELY_HIGH);
         }
      }
   }

   public void appendHoverText(ItemStack p_49816_, @Nullable BlockGetter p_49817_, List<Component> p_49818_, TooltipFlag p_49819_) {
      p_49818_.add(Component.literal("Remanufactured").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      super.appendHoverText(p_49816_, p_49817_, p_49818_, p_49819_);
      p_49818_.add(Component.literal("Model: DS15FM14/14W-VTD527").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      p_49818_.add(Component.literal("MADE IN").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      p_49818_.add(Component.literal("The rest is scratched off and unreadable.").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(true)));
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      if (level.getBlockEntity(pos) instanceof KineticBlockEntity kbe) {
         RotationPropagator.handleAdded(level, pos, kbe);
      }
   }

   public Class<HydrostaticShiftBlockEntity> getBlockEntityClass() {
      return HydrostaticShiftBlockEntity.class;
   }

   public BlockEntityType<HydrostaticShiftBlockEntity> getBlockEntityType() {
      return (BlockEntityType<HydrostaticShiftBlockEntity>)AllBlockEntities.HYDRO_SHIFT.get();
   }
}
