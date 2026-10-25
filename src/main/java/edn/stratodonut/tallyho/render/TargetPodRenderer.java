package edn.stratodonut.tallyho.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import edn.stratodonut.tallyho.AllItems;
import edn.stratodonut.tallyho.SpecialModels;
import edn.stratodonut.tallyho.camera.entity.TargetingPodEntity;
import edn.stratodonut.tallyho.client.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TargetPodRenderer<T extends TargetingPodEntity> extends EntityRenderer<T> {
   public TargetPodRenderer(Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(TargetingPodEntity entity) {
      return null;
   }

   public void render(TargetingPodEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
      if (entity.tickCount > 1) {
         poseStack.pushPose();
         ItemStack m = AllItems.HULL_MG_ITEM.asStack();
         poseStack.translate(0.0, 0.5, 0.0);
         this.yawTransform(poseStack, entity.getBaseYaw(), false);
         RenderUtil.renderModel(SpecialModels.TGP_BODY.getModel(), m, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
         poseStack.mulPose(Axis.ZP.rotationDegrees(entity.roll));
         RenderUtil.renderModel(SpecialModels.TGP_CRADLE.getModel(), m, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
         poseStack.translate(0.0F, 0.0F, -1.25F);
         poseStack.mulPose(Axis.XP.rotationDegrees(entity.pitch));
         poseStack.translate(0.0F, 0.0F, 1.25F);
         RenderUtil.renderModel(SpecialModels.TGP_CAM.getModel(), m, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
         poseStack.popPose();
      }
   }

   protected void yawTransform(PoseStack poseStack, float bYaw, boolean undo) {
      if (undo) {
         poseStack.translate(0.0F, 0.0F, -1.25F);
         poseStack.mulPose(Axis.YP.rotationDegrees(bYaw));
         poseStack.mulPose(Axis.YP.rotationDegrees(-180.0F));
      } else {
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(-bYaw));
         poseStack.translate(0.0F, 0.0F, 1.25F);
      }
   }
}
