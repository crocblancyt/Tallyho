package edn.stratodonut.tallyho.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.commands.PlaySoundCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin({PlaySoundCommand.class})
public class MixinPlaySoundCommand {
    @ModifyVariable(method = {"playSound(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Collection;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/phys/Vec3;FFF)I"}, at = @At("STORE"), ordinal = 4)
    private static double mixinPlaySound(double value, @Local(ordinal = 0) Vec3 pos, @Local ServerPlayer serverplayer) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(serverplayer.level(), VectorConversionsMCKt.toJOML(pos));
        if (ship != null) {
            pos = VectorConversionsMCKt.toMinecraft(ship.getTransform().getShipToWorld().transformPosition(VectorConversionsMCKt.toJOML(pos)));
            double d1 = pos.x - serverplayer.getX();
            double d2 = pos.y - serverplayer.getY();
            double d3 = pos.z - serverplayer.getZ();
            double d4 = d1 * d1 + d2 * d2 + d3 * d3;
            return 0.0D;
        }
        return value;
    }
}