package edn.stratodonut.tallyho;

import com.copycatsplus.copycats.CCBlocks;
import java.util.List;
import kotlin.NotImplementedError;
import kotlin.Triple;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.apigame.world.chunks.BlockType;
import org.valkyrienskies.core.impl.game.BlockTypeImpl;
import org.valkyrienskies.mod.common.BlockStateInfo;
import org.valkyrienskies.mod.common.BlockStateInfoProvider;
import org.valkyrienskies.physics_api.voxel.Lod1LiquidBlockState;
import org.valkyrienskies.physics_api.voxel.Lod1SolidBlockState;

public class NonSolidBlockstateProvider implements BlockStateInfoProvider {
   public int getPriority() {
      return 128;
   }

   @Nullable
   public BlockType getBlockStateType(@NotNull BlockState blockState) {
      return !com.simibubi.create.AllBlocks.REDSTONE_LINK.has(blockState) && !blockState.is(BlockTags.LEAVES) && !CCBlocks.COPYCAT_BOARD.has(blockState)
         ? null
         : BlockTypeImpl.a.a();
   }

   public static void init() {
      Registry.register(BlockStateInfo.INSTANCE.getREGISTRY(), new ResourceLocation("tallyho", "data"), new NonSolidBlockstateProvider());
   }

   @Nullable
   public Double getBlockStateMass(@NotNull BlockState blockState) {
      return null;
   }

   @NotNull
   public List<Triple<Integer, Integer, Integer>> getBlockStateData() {
      throw new NotImplementedError();
   }

   @NotNull
   public List<Lod1LiquidBlockState> getLiquidBlockStates() {
      throw new NotImplementedError();
   }

   @NotNull
   public List<Lod1SolidBlockState> getSolidBlockStates() {
      throw new NotImplementedError();
   }
}
