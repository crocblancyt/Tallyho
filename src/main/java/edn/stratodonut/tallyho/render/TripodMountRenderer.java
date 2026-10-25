package edn.stratodonut.tallyho.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.item.GunItem;
import edn.stratodonut.tallyho.AllItems;
import edn.stratodonut.tallyho.SpecialModels;
import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.client.RenderUtil;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TripodMountRenderer<T extends GunMountEntity> extends EntityRenderer<T> {
   ResourceLocation tex = TallyhoMod.asResource("item/gun_mount.png");
   private final ItemRenderer itemRenderer;

   public TripodMountRenderer(Context context) {
      super(context);
      this.itemRenderer = context.getItemRenderer();
   }

   public ResourceLocation getTextureLocation(GunMountEntity entity) {
      return this.tex;
   }

   public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
      if (entity.tickCount > 1) {
         ItemStack m = AllItems.TRIPOD_ITEM.asStack();
         BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(m);
         poseStack.pushPose();
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getBaseYaw()));
         poseStack.translate(0.0, 0.3, 0.0);
         Minecraft.getInstance()
            .getItemRenderer()
            .render(m, ItemDisplayContext.NONE, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, bakedModel);
         poseStack.translate(0.0, 0.0, 0.5);
         poseStack.mulPose(Axis.XP.rotationDegrees(22.5F));
         poseStack.translate(0.0, 0.0, -0.5);
         RenderUtil.renderModel(SpecialModels.TRIPOD_LEGS.getModel(), m, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
         poseStack.popPose();
         ItemStack g = entity.getMountedGun();
         if (!(g.getItem() instanceof GunItem)) {
            return;
         }

         poseStack.pushPose();
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getBaseYaw()));
         poseStack.translate(0.0, 0.5, -0.7);
         poseStack.mulPose(Axis.YP.rotationDegrees(entity.getBaseYaw() - entity.getYRot()));
         poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot()));
         BakedModel itemBakedModel = this.itemRenderer.getModel(g, entity.level(), null, 0);
         this.itemRenderer.render(g, ItemDisplayContext.GROUND, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, itemBakedModel);
         poseStack.popPose();
      }
   }
}
