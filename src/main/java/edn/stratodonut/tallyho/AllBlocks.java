package edn.stratodonut.tallyho;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import edn.stratodonut.tallyho.block.FlareDispenserBlock;
import edn.stratodonut.tallyho.block.HydrostaticShiftBlock;
import edn.stratodonut.tallyho.block.RippleFireBlock;
import edn.stratodonut.tallyho.camera.block.RemoteStationBlock;
import edn.stratodonut.tallyho.camera.block.ScopeBlock;
import edn.stratodonut.tallyho.camera.block.ThermalsBlock;
import kotlin.Suppress;
import net.minecraft.world.level.block.SoundType;
import org.openjdk.nashorn.internal.ir.annotations.Ignore;

public class AllBlocks {
    public static final BlockEntry<HydrostaticShiftBlock> HYDRO_SHIFT;

    static {
        HYDRO_SHIFT = (BlockEntry<HydrostaticShiftBlock>) (TallyhoMod.REGISTRATE.block("hydro_shift", HydrostaticShiftBlock::new)
                .initialProperties(SharedProperties::copperMetal)
                .properties(p -> p
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.NETHERITE_BLOCK))
                .item()
                .properties(p -> p
                        .stacksTo(1))
                .build()
                .transform((NonNullFunction)BlockStressDefaults.setImpact(16.0D))
                .transform(TagGen.pickaxeOnly()))
                .register();
    }

    public static final BlockEntry<RippleFireBlock> RIPPLE_BLOCK = TallyhoMod.REGISTRATE
        .block("ripple_block", RippleFireBlock::new)
        .initialProperties(SharedProperties::copperMetal)
        .simpleItem()
        .register();

    public static final BlockEntry<FlareDispenserBlock> FLARE_DISPENSER = TallyhoMod.REGISTRATE
        .block("flare_dispenser", FlareDispenserBlock::new)
        .initialProperties(SharedProperties::copperMetal)
        .simpleItem()
        .register();

    public static final BlockEntry<ScopeBlock> SCOPE_BLOCK;

    public static final BlockEntry<ThermalsBlock> PERISCOPE_THERMALS;

    public static final BlockEntry<RemoteStationBlock> CROWS_CONSOLE;

    static {
        SCOPE_BLOCK = TallyhoMod.REGISTRATE.block("scope_block", ScopeBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.standardModel(c, p)))
            .item()
            .transform(ModelGen.customItemModel())
            .register();
        PERISCOPE_THERMALS = TallyhoMod.REGISTRATE.block("thermals_block", ThermalsBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.standardModel(c, p)))
            .item()
            .transform(ModelGen.customItemModel())
            .register();
        CROWS_CONSOLE = TallyhoMod.REGISTRATE.block("crows_console", RemoteStationBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.standardModel(c, p)))
            .item()
            .transform(ModelGen.customItemModel())
            .register();
    }

    public static void register() {}
}