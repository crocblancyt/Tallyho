package edn.stratodonut.tallyho;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import edn.stratodonut.tallyho.block.HydrostaticShiftBlockEntity;
import edn.stratodonut.tallyho.block.HydrostaticShiftRenderer;

public class AllBlockEntities {

    public static final BlockEntityEntry<HydrostaticShiftBlockEntity> HYDRO_SHIFT = TallyhoMod.REGISTRATE
            .blockEntity("hydro_shift", HydrostaticShiftBlockEntity::new)
            .validBlocks(AllBlocks.HYDRO_SHIFT)
            .renderer(() -> HydrostaticShiftRenderer::new)
            .register();

    public static void register() {}
}
