    package edn.stratodonut.tallyho.render;

    import com.mojang.blaze3d.vertex.PoseStack;
    import com.mojang.math.Axis;
    import edn.stratodonut.tallyho.AllItems;
    import edn.stratodonut.tallyho.SpecialModels;
    import edn.stratodonut.tallyho.client.RenderUtil;
    import edn.stratodonut.tallyho.entity.GunMountEntity;
    import net.minecraft.client.Minecraft;
    import net.minecraft.client.renderer.MultiBufferSource;
    import net.minecraft.client.renderer.entity.EntityRenderer;
    import net.minecraft.client.renderer.entity.EntityRendererProvider;
    import net.minecraft.client.renderer.entity.ItemRenderer;
    import net.minecraft.client.renderer.texture.OverlayTexture;
    import net.minecraft.client.resources.model.BakedModel;
    import net.minecraft.resources.ResourceLocation;
    import net.minecraft.world.entity.Entity;
    import net.minecraft.world.item.ItemDisplayContext;
    import net.minecraft.world.item.ItemStack;

    public class GunMountRenderer<T extends Entity> extends EntityRenderer<T> {
        private final ItemRenderer itemRenderer;

        public GunMountRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.itemRenderer = context.getItemRenderer();
        }

        public ResourceLocation getTextureLocation(T entity) {
            return null;
        }

        public void render(GunMountEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
            if (entity.tickCount > 1) {
                ItemStack m = AllItems.GUN_MOUNT_ITEM.asStack();
                BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(m, entity.level(), null, 0);
                poseStack.pushPose();
                poseStack.translate(0.0D, 0.3D, 0.0D);
                Minecraft.getInstance().getItemRenderer().render(m, ItemDisplayContext.NONE, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, bakedModel);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
                RenderUtil.renderModel(SpecialModels.GUN_MOUNT_BRACKET.getModel(), m, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
                ItemStack g = entity.getMountedGun();
                if (!(g.getItem() instanceof com.vicmatskiv.pointblank.item.GunItem))
                    return;
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
                poseStack.translate(0.0D, 0.0D, -0.5D);
                poseStack.translate(0.0D, 0.125D, 0.0D);
                poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot()));
                BakedModel itemBakedModel = this.itemRenderer.getModel(g, entity.level(), null, 0);
                this.itemRenderer.render(g, ItemDisplayContext.GROUND, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, itemBakedModel);
                poseStack.popPose();
            }
        }
    }