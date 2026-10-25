package edn.stratodonut.tallyho.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.AllGameRules;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules.BooleanValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MixinMinecraft {
   @WrapOperation(
      method = "handleKeybinds",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V")
   )
   private void cancelChangeCameraAndForce(Options instance, CameraType p_92158_, Operation<Void> original) {
      Player p = Minecraft.getInstance().player;
      if (p != null && ((BooleanValue)p.level().getGameRules().getRule(AllGameRules.RULE_LIMITF5)).get() && !p.isCreative() && !p.isSpectator()) {
         original.call(new Object[]{instance, CameraType.FIRST_PERSON});
      } else {
         original.call(new Object[]{instance, p_92158_});
      }
   }
}
