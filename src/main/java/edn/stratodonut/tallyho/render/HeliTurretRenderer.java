package edn.stratodonut.tallyho.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import edn.stratodonut.tallyho.AllItems;
import edn.stratodonut.tallyho.SpecialModels;
import edn.stratodonut.tallyho.client.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class HeliTurretRenderer<T extends Entity> extends EntityRenderer<T> {
    public HeliTurretRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(Entity entity) {
        return null;
    }

    public void render(Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
        if (entity.tickCount > 1) {
            poseStack.pushPose();
            ItemStack m = AllItems.HULL_MG_ITEM.asStack();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
            poseStack.translate(0.0D, 0.5D, -1.0D);
            RenderUtil.renderModel(SpecialModels.HELI_TURRET_CAGE.getModel(), m, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
            poseStack.translate(0.0F, 0.0F, 1.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot()));
            poseStack.translate(0.0F, 0.0F, -1.0F);
            RenderUtil.renderModel(SpecialModels.HELI_TURRET_GUN.getModel(), m, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}