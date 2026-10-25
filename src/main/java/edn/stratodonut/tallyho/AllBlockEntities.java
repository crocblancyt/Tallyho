package edn.stratodonut.tallyho;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import edn.stratodonut.tallyho.block.HydrostaticShiftBlockEntity;
import edn.stratodonut.tallyho.block.HydrostaticShiftRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class AllBlockEntities {
   public static final BlockEntityEntry<HydrostaticShiftBlockEntity> HYDRO_SHIFT = TallyhoMod.REGISTRATE
      .blockEntity("hydro_shift", HydrostaticShiftBlockEntity::new)
      .validBlocks(new NonNullSupplier[]{AllBlocks.HYDRO_SHIFT})
      .renderer((NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<?>>>) () -> HydrostaticShiftRenderer::new)
      .register();

   public static void register() {
   }
}
