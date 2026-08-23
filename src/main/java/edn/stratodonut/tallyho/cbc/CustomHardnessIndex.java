package edn.stratodonut.tallyho.cbc;

import com.redcat.copiescats.block.ModBlocks;
import net.minecraft.world.level.block.Block;
import rbasamoyai.createbigcannons.block_armor_properties.BlockArmorPropertiesHandler;
import rbasamoyai.createbigcannons.block_armor_properties.mimicking_blocks.AbstractMimickingBlockArmorProperties;

public class CustomHardnessIndex {
  public static void init() {
    registerCopycatSerializer(ModBlocks.COPYCAT_TWO.get());
    registerCopycatSerializer(ModBlocks.COPYCAT_FOUR.get());
    registerCopycatSerializer(ModBlocks.COPYCAT_SIX.get());
    registerCopycatSerializer(ModBlocks.COPYCAT_SLAB.get());
    registerCopycatSerializer(ModBlocks.COPYCAT_TEN.get());
    registerCopycatSerializer(ModBlocks.COPYCAT_TWELVE.get());
    registerCopycatSerializer(ModBlocks.COPYCAT_FOURTEEN.get());
    registerCopycatSerializer(ModBlocks.COPYCAT_SIXTEEN.get());
  }
  
  private static void registerCopycatSerializer(Block block) {
    BlockArmorPropertiesHandler.registerCustomSerializer(block, AbstractMimickingBlockArmorProperties.createMimicrySerializer(rbasamoyai.createbigcannons.block_armor_properties.mimicking_blocks.CopycatBlockArmorProperties::new));
  }
}
