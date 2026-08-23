package edn.stratodonut.tallyho.camera.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import edn.stratodonut.tallyho.AllItems;
import edn.stratodonut.tallyho.camera.entity.PeriscopeEntity;
import edn.stratodonut.tallyho.client.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PeriscopeEntityRenderer<T extends PeriscopeEntity> extends EntityRenderer<T> {
    public PeriscopeEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(PeriscopeEntity entity) {
        return null;
    }

    public void render(PeriscopeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
        if (entity.tickCount > 1) {
            ItemStack s = (entity.getRotationLimit().Y() > 120.0F) ? AllItems.PERISCOPE_360_ITEM.asStack() : AllItems.PERISCOPE_ARC_ITEM.asStack();
            poseStack.pushPose();
            if (!RenderUtil.getModel(s).usesBlockLight()) {
                poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                Minecraft.getInstance().getItemRenderer().renderStatic(s, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, renderTypeBuffer, entity.level(), 0);
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getBaseYaw()));
                poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
                poseStack.translate(0.0F, 1.0F, 0.0F);
                Minecraft.getInstance().getItemRenderer().renderStatic(s, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, poseStack, renderTypeBuffer, entity.level(), 0);
            }
            poseStack.popPose();
        }
    }
}