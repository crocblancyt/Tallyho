package edn.stratodonut.tallyho.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.clockwork.content.kinetics.resistor.RedstoneResistorBlockEntity;

@Mixin(value = RedstoneResistorBlockEntity.class, remap = false)
public abstract class MixinResistorBE extends BlockEntity {
   public MixinResistorBE(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
      super(p_155228_, p_155229_, p_155230_);
   }

   @Inject(method = "getPower", at = @At("RETURN"), cancellable = true)
   private void mixinGetPower(Level worldIn, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
      int i2 = worldIn.getDirectSignalTo(pos);
      if (i2 > (Integer)cir.getReturnValue()) {
         cir.setReturnValue(i2);
      }
   }
}
