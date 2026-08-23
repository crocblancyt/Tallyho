package edn.stratodonut.tallyho.integration.cc;

import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.valkyrienskies.clockwork.content.contraptions.flap.FlapBearingBlockEntity;

public class ClockworkForgePeripheralProviders {
    public static void register() {
        ForgeComputerCraftAPI.registerPeripheralProvider(new ClockworkPeripheralProvider());
    }

    public static class ClockworkPeripheralProvider implements IPeripheralProvider {
        @Nonnull
        public LazyOptional<IPeripheral> getPeripheral(@Nonnull Level level, @Nonnull BlockPos blockPos, @Nonnull Direction direction) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof FlapBearingBlockEntity) {
                FlapBearingBlockEntity flap = (FlapBearingBlockEntity)be;
                return LazyOptional.of(() -> new FlapBearingPeripheral(flap));
            }
            return LazyOptional.empty();
        }
    }
}