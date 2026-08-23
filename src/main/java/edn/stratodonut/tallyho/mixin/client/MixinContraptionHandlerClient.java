package edn.stratodonut.tallyho.mixin.client;

import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(value = {ContraptionHandlerClient.class}, remap = false)
public abstract class MixinContraptionHandlerClient {
    @Redirect(method = {"getRayInputs(Lnet/minecraft/client/player/LocalPlayer;)Lcom/simibubi/create/foundation/utility/Couple;"}, at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/utility/RaycastHelper;getTraceTarget(Lnet/minecraft/world/entity/player/Player;DLnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 mixinGetRayInputs(Player player, double reach, Vec3 origin) {
        if (player.getVehicle() != null) {
            Ship s = VSGameUtilsKt.getShipManaging(player.getVehicle());
            if (s != null)
                return origin.add(VectorConversionsMCKt.toMinecraft(s.getTransform().getShipToWorldRotation().transform(
                        VectorConversionsMCKt.toJOML(Vec3.directionFromRotation(player.getViewXRot(player.getXRot()), player.getViewYRot(player.getYRot()))))).scale(reach));
        }
        return RaycastHelper.getTraceTarget(player, reach, origin);
    }
}