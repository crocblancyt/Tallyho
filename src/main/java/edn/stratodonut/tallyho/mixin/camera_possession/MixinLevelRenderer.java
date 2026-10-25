package edn.stratodonut.tallyho.mixin.camera_possession;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
   @WrapOperation(
      method = "renderLevel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
      )
   )
   private void renderEntityGlow(
      LevelRenderer instance,
      Entity entity,
      double p_109519_,
      double p_109520_,
      double p_109521_,
      float p_109522_,
      PoseStack p_109523_,
      MultiBufferSource p_109524_,
      Operation<Void> original
   ) {
      boolean shouldModify = false;
      if (CameraClientHandler.isPlayerMountedOnCamera()) {
         CameraEntity2 ce = CameraClientHandler.getPlayerMountedOnCamera();
         CameraEntity2.PostFXType type = ce.getOverlayType();
         if (type == CameraEntity2.PostFXType.THERMALS_GREEN || type == CameraEntity2.PostFXType.THERMALS_WHITE) {
            shouldModify = entity instanceof LivingEntity;
         }
      }

      if (shouldModify && RenderSystem.isOnRenderThread()) {
         RenderSystem.setShaderColor(50.0F, 50.0F, 50.0F, 1.0F);
      }

      original.call(new Object[]{instance, entity, p_109519_, p_109520_, p_109521_, p_109522_, p_109523_, p_109524_});
   }
}
