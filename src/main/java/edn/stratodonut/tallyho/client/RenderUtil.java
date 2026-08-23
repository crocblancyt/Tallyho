package edn.stratodonut.tallyho.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class RenderUtil {
    private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
    private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");

    public static BakedModel getModel(ItemStack item) {
        return Minecraft.getInstance().getItemRenderer().getModel(item, null, null, 0);
    }

    public static void renderModel(BakedModel model, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderModel(model, ItemDisplayContext.NONE, stack, poseStack, buffer, light, overlay);
    }

    public static void renderModel(BakedModel model, ItemDisplayContext display, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderModel(model, display, null, stack, ItemStack.EMPTY, poseStack, buffer, light, overlay);
    }

    public static void renderModel(BakedModel model, ItemDisplayContext display, @Nullable Runnable transform, ItemStack stack, ItemStack parent, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            boolean flag = (display == ItemDisplayContext.GUI || display == ItemDisplayContext.GROUND || display == ItemDisplayContext.FIXED);
            if (flag)
                if (stack.is(Items.TRIDENT)) {
                    model = Minecraft.getInstance().getModelManager().getModel(TRIDENT_MODEL);
                } else if (stack.is(Items.SPYGLASS)) {
                    model = Minecraft.getInstance().getModelManager().getModel(SPYGLASS_MODEL);
                }
            model = model.applyTransform(display, poseStack, false);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            if (model.usesBlockLight() || (stack.getItem() == Items.TRIDENT && !flag)) {
                IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, display, poseStack, buffer, light, overlay);
            } else {
                VertexConsumer builder;
                boolean entity = true;
                if (display != ItemDisplayContext.GUI && !display.firstPerson() && stack.getItem() instanceof BlockItem) {
                    Block block = ((BlockItem)stack.getItem()).getBlock();
                    entity = (!(block instanceof net.minecraft.world.level.block.HalfTransparentBlock) && !(block instanceof net.minecraft.world.level.block.StainedGlassPaneBlock));
                }
                RenderType renderType = getRenderType(stack, entity);
                if (stack.getItem() == Items.FILLED_MAP && stack.hasFoil()) {
                    poseStack.pushPose();
                    PoseStack.Pose entry = poseStack.last();
                    if (display == ItemDisplayContext.GUI) {
                        MatrixUtil.mulComponentWise(entry.pose(), 0.5F);
                    } else if (display.firstPerson()) {
                        MatrixUtil.mulComponentWise(entry.pose(), 0.75F);
                    }
                    if (entity) {
                        builder = ItemRenderer.getFoilBufferDirect(buffer, renderType, false, true);
                    } else {
                        builder = ItemRenderer.getFoilBuffer(buffer, renderType, true, true);
                    }
                    poseStack.popPose();
                } else if (entity) {
                    builder = ItemRenderer.getFoilBuffer(buffer, renderType, true, (stack.hasFoil() || parent.hasFoil()));
                } else {
                    builder = ItemRenderer.getFoilBuffer(buffer, renderType, true, (stack.hasFoil() || parent.hasFoil()));
                }
                renderModel(model, stack, parent, transform, poseStack, builder, light, overlay);
            }
            poseStack.popPose();
        }
    }

    public static void renderModel(BakedModel model, ItemStack stack, ItemStack parent, @Nullable Runnable transform, PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        if (transform != null)
            transform.run();
        RandomSource random = RandomSource.create();
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderQuads(poseStack, buffer, model.getQuads(null, direction, random), stack, parent, light, overlay);
        }
        random.setSeed(42L);
        renderQuads(poseStack, buffer, model.getQuads(null, null, random), stack, parent, light, overlay);
    }

    private static void renderQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack stack, ItemStack parent, int light, int overlay) {
        PoseStack.Pose entry = poseStack.last();
        for (BakedQuad quad : quads) {
            int color = -1;
            if (quad.isTinted())
                color = getItemStackColor(stack, parent, quad.getTintIndex());
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            buffer.putBulkData(entry, quad, red, green, blue, light, overlay);
        }
    }

    public static int getItemStackColor(ItemStack stack, ItemStack parent, int tintIndex) {
        int color = Minecraft.getInstance().getItemColors().getColor(stack, tintIndex);
        return (color == -1 && !parent.isEmpty()) ? getItemStackColor(parent, ItemStack.EMPTY, tintIndex) : color;
    }

    public static RenderType getRenderType(ItemStack stack, boolean entity) {
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            Block block = ((BlockItem)item).getBlock();
            return ItemBlockRenderTypes.getRenderType(block.defaultBlockState(), !entity);
        }
        return RenderType.text(InventoryMenu.BLOCK_ATLAS);
    }
}