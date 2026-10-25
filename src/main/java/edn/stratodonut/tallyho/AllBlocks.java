package edn.stratodonut.tallyho;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import edn.stratodonut.tallyho.block.FlareDispenserBlock;
import edn.stratodonut.tallyho.block.HydrostaticShiftBlock;
import edn.stratodonut.tallyho.block.RippleFireBlock;
import edn.stratodonut.tallyho.camera.block.RemoteStationBlock;
import edn.stratodonut.tallyho.camera.block.ScopeBlock;
import edn.stratodonut.tallyho.camera.block.ThermalsBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class AllBlocks {
   public static final BlockEntry<HydrostaticShiftBlock> HYDRO_SHIFT = (((TallyhoMod.REGISTRATE
               .block("hydro_shift", HydrostaticShiftBlock::new)
               .initialProperties(SharedProperties::copperMetal)
               .properties(p -> p.noOcclusion().sound(SoundType.METAL))
               .transform(BlockStressDefaults.setImpact(16.0)))
            .transform(TagGen.pickaxeOnly()))
         .item()
         .properties(p -> p.stacksTo(1))
         .build())
      .register();

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

   public static final BlockEntry<ScopeBlock> SCOPE_BLOCK = (TallyhoMod.REGISTRATE
         .block("scope_block", ScopeBlock::new)
         .initialProperties(SharedProperties::copperMetal)
         .blockstate((c, p) -> p.horizontalFaceBlock((Block)c.get(), AssetLookup.standardModel(c, p)))
         .item()
         .transform(ModelGen.customItemModel()))
      .register();

   public static final BlockEntry<ThermalsBlock> PERISCOPE_THERMALS = (TallyhoMod.REGISTRATE
         .block("thermals_block", ThermalsBlock::new)
         .initialProperties(SharedProperties::copperMetal)
         .blockstate((c, p) -> p.horizontalFaceBlock((Block)c.get(), AssetLookup.standardModel(c, p)))
         .item()
         .transform(ModelGen.customItemModel()))
      .register();

   public static final BlockEntry<RemoteStationBlock> CROWS_CONSOLE = (TallyhoMod.REGISTRATE
         .block("crows_console", RemoteStationBlock::new)
         .initialProperties(SharedProperties::copperMetal)
         .blockstate((c, p) -> p.horizontalFaceBlock((Block)c.get(), AssetLookup.standardModel(c, p)))
         .item()
         .transform(ModelGen.customItemModel()))
      .register();

   public static void register() {
   }

   static {
      TallyhoMod.REGISTRATE.setCreativeTab(AllCreativeTabs.BASE_CREATIVE_TAB);
   }
}
