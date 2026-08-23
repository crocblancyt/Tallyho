package edn.stratodonut.tallyho.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import rbasamoyai.ritchiesprojectilelib.effects.screen_shake.RPLScreenShakeHandlerClient;
import rbasamoyai.ritchiesprojectilelib.effects.screen_shake.ScreenShakeContext;

@Mixin(value = {GameRenderer.class}, priority = 99)
public class MixinGameRenderer {
    @Shadow
    @Final
    private Camera mainCamera;

    @WrapOperation(method = {"renderLevel"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;)V")})
    private void shakeFrustumPostShip(LevelRenderer instance, PoseStack poseStack, Vec3 pos, Matrix4f projectionMatrix, Operation<Void> original) {
        float partialTicks = Minecraft.getInstance().getPartialTick();
        ScreenShakeContext screenShake = RPLScreenShakeHandlerClient.getScreenShake(partialTicks);
        float modifier = 1.0F;
        if (Minecraft.getInstance().player != null && VSGameUtilsKt.getShipMountedTo(Minecraft.getInstance().player) != null)
            modifier = 0.5F;

        Camera camera = this.mainCamera;
        camera.move((screenShake
                .getDeltaYaw() / 45.0F) * Math.sin((partialTicks * modifier)), (screenShake
                .getDeltaPitch() / 45.0F) * Math.sin((partialTicks * modifier)), (
                -screenShake.getDeltaYaw() / 45.0F) * Math.sin((partialTicks * modifier)));

        original.call(instance, poseStack, camera.getPosition(), projectionMatrix);
    }
}