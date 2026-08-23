package edn.stratodonut.tallyho.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.mixinducks.MixinGameRendererDuck;
import edn.stratodonut.tallyho.mixinducks.MixinPostChainDuck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.joml.Math;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber({Dist.CLIENT})
public class SuppressionTracker {
    private static final ResourceLocation VIG0_LOCATION = TallyhoMod.asResource("textures/misc/vig0.png");
    private static final ResourceLocation VIG1_LOCATION = TallyhoMod.asResource("textures/misc/vig1.png");
    protected static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");

    private float supAmount = 0.0F;
    private float lastSup = 0.0F;
    private int ticker = 0;
    private boolean lastShader = false;

    private static SuppressionTracker instance;

    public static SuppressionTracker get() {
        if (instance == null)
            instance = new SuppressionTracker();
        return instance;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (this.ticker % 10 == 0) {
                this.lastSup = this.supAmount;
                this.supAmount = Math.max(this.supAmount - 10.0F, 0.0F);
            }
            this.ticker++;
        }
    }

    public float getLerpSup() {
        return this.lastSup;
    }

    public void renderOverlay(GuiGraphics guigraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;
        if (getLerpSup() > 1.0F);
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;
        if (getLerpSup() > 10.0F) {
            if (!this.lastShader) {
                mc.gameRenderer.loadEffect(new ResourceLocation("shaders/post/radial_blur.json"));
                this.lastShader = true;
            }
            PostChain shaderChain = ((MixinGameRendererDuck)mc.gameRenderer).getRenderer();
            if (shaderChain != null)
                ((MixinPostChainDuck)shaderChain).tyh$setUniformFloat(0, "blurWidth",
                    Math.clamp(0.0F, 1.0F, getLerpSup() / 100.0F) * 0.2F);
        } else if (this.lastShader) {
            mc.gameRenderer.shutdownEffect();
            this.lastShader = false;
        }
    }

    public void suppress(float factor) {
        if (this.supAmount < 100.0F && Float.isFinite(factor))
            this.supAmount += Math.min(factor, 100.0F - this.supAmount);
    }

    public void overSuppress(float factor) {
        if (this.supAmount < 300.0F && Float.isFinite(factor))
            this.supAmount += Math.min(factor, 300.0F - this.supAmount);
    }

    public void renderConfusionOverlay(GuiGraphics guiGraphics, float supPercent) {
        int i = guiGraphics.guiWidth();
        int j = guiGraphics.guiHeight();
        guiGraphics.pose().pushPose();
        float f = Mth.lerp(supPercent, 2.0F, 1.0F);
        guiGraphics.pose().translate(i / 2.0F, j / 2.0F, 0.0F);
        guiGraphics.pose().scale(f, f, f);
        guiGraphics.pose().translate(-i / 2.0F, -j / 2.0F, 0.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(VIGNETTE_LOCATION, 0, 0, -90, 0.0F, 0.0F, i, j, i, j);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        guiGraphics.pose().popPose();
    }
}