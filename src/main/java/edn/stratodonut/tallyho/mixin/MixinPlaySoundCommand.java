package edn.stratodonut.tallyho.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.commands.PlaySoundCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(PlaySoundCommand.class)
public class MixinPlaySoundCommand {
   @ModifyVariable(
      method = "playSound(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/phys/Vec3;FFF)I",
      at = @At("STORE"),
      ordinal = 4
   )
   private static double mixinPlaySound(double value, @Local(ordinal = 0) Vec3 p_138165_, @Local ServerPlayer serverplayer) {
      Ship ship = VSGameUtilsKt.getShipManagingPos(serverplayer.level(), VectorConversionsMCKt.toJOML(p_138165_));
      if (ship != null) {
         p_138165_ = VectorConversionsMCKt.toMinecraft(ship.getTransform().getShipToWorld().transformPosition(VectorConversionsMCKt.toJOML(p_138165_)));
         double d1 = p_138165_.x - serverplayer.getX();
         double d2 = p_138165_.y - serverplayer.getY();
         double d3 = p_138165_.z - serverplayer.getZ();
         double d4 = d1 * d1 + d2 * d2 + d3 * d3;
         return 0.0;
      } else {
         return value;
      }
   }
}
