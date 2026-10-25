package edn.stratodonut.tallyho.cbc;

import com.redcat.copiescats.block.ModBlocks;
import net.minecraft.world.level.block.Block;
import rbasamoyai.createbigcannons.block_armor_properties.BlockArmorPropertiesHandler;
import rbasamoyai.createbigcannons.block_armor_properties.mimicking_blocks.AbstractMimickingBlockArmorProperties;
import rbasamoyai.createbigcannons.block_armor_properties.mimicking_blocks.CopycatBlockArmorProperties;

public class CustomHardnessIndex {
   public static void init() {
      registerCopycatSerializer((Block)ModBlocks.COPYCAT_TWO.get());
      registerCopycatSerializer((Block)ModBlocks.COPYCAT_FOUR.get());
      registerCopycatSerializer((Block)ModBlocks.COPYCAT_SIX.get());
      registerCopycatSerializer((Block)ModBlocks.COPYCAT_SLAB.get());
      registerCopycatSerializer((Block)ModBlocks.COPYCAT_TEN.get());
      registerCopycatSerializer((Block)ModBlocks.COPYCAT_TWELVE.get());
      registerCopycatSerializer((Block)ModBlocks.COPYCAT_FOURTEEN.get());
      registerCopycatSerializer((Block)ModBlocks.COPYCAT_SIXTEEN.get());
   }

   private static void registerCopycatSerializer(Block block) {
      BlockArmorPropertiesHandler.registerCustomSerializer(
         block, AbstractMimickingBlockArmorProperties.createMimicrySerializer(CopycatBlockArmorProperties::new)
      );
   }
}
