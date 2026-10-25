package edn.stratodonut.tallyho.mixin;

import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlock;
import edn.stratodonut.tallyho.TallyhoMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnalogLeverBlock.class)
public abstract class MixinAnalogLever {
   @Inject(method = "use", at = @At("HEAD"), cancellable = true)
   public void stopUse(
      BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir
   ) {
      if (player.isSpectator()) {
         String output = "User: " + player.getName().getString() + " attempted to flick a lever in spectator.";
         TallyhoMod.warn(output);
         worldIn.players().forEach(p -> {
            if (p.hasPermissions(2)) {
               p.sendSystemMessage(Component.literal(output));
            }
         });
         cir.setReturnValue(InteractionResult.FAIL);
      }
   }
}
