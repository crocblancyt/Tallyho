package edn.stratodonut.tallyho.integration.cc;

import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import org.valkyrienskies.clockwork.content.contraptions.flap.FlapBearingBlockEntity;

public class ClockworkForgePeripheralProviders {
   public static void register() {
      ForgeComputerCraftAPI.registerPeripheralProvider(new ClockworkForgePeripheralProviders.ClockworkPeripheralProvider());
   }

   public static class ClockworkPeripheralProvider implements IPeripheralProvider {
      @Nonnull
      public LazyOptional<IPeripheral> getPeripheral(@Nonnull Level level, @Nonnull BlockPos blockPos, @Nonnull Direction direction) {
         return level.getBlockEntity(blockPos) instanceof FlapBearingBlockEntity flap
            ? LazyOptional.of(() -> new FlapBearingPeripheral(flap))
            : LazyOptional.empty();
      }
   }
}
