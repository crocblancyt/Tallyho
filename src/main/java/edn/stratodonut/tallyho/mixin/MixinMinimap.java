package edn.stratodonut.tallyho.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import xaero.common.minimap.render.MinimapRenderer;

@Pseudo
@Mixin(value = {MinimapRenderer.class}, remap = false)
public abstract class MixinMinimap {
    @Shadow
    protected Minecraft mc;

    @WrapOperation(method = {"getRenderAngle"}, at = {@At(value = "INVOKE", target = "Lxaero/common/minimap/render/MinimapRenderer;getActualAngle()D")})
    private double mixinAngle(MinimapRenderer instance, Operation<Double> original) {
        Entity cam = this.mc.getCameraEntity();
        if (cam == null)
            return original.call(instance);
        LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(cam);
        if (loadedShip == null)
            return original.call(instance);
        Vector3f look = this.mc.gameRenderer.getMainCamera().getLookVector();
        return -90.0D - -Math.atan2(look.x(), look.z()) * 180.0D / Math.PI;
    }
}