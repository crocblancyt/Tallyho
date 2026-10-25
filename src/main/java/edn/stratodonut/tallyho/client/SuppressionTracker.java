package edn.stratodonut.tallyho.client;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
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
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
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
      if (instance == null) {
         instance = new SuppressionTracker();
      }

      return instance;
   }

   private SuppressionTracker() {
   }

   @SubscribeEvent
   public void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.END) {
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
      if (mc.player != null) {
         if (this.getLerpSup() > 1.0F) {
         }
      }
   }

   @SubscribeEvent
   public void onRender(RenderTickEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         if (this.getLerpSup() > 10.0F) {
            if (!this.lastShader) {
               mc.gameRenderer.loadEffect(new ResourceLocation("shaders/post/radial_blur.json"));
               this.lastShader = true;
            }

            PostChain shaderChain = ((MixinGameRendererDuck)mc.gameRenderer).getRenderer();
            if (shaderChain != null) {
               ((MixinPostChainDuck)shaderChain).tyh$setUniformFloat(0, "blurWidth", org.joml.Math.clamp(0.0F, 1.0F, this.getLerpSup() / 100.0F) * 0.2F);
            }
         } else if (this.lastShader) {
            mc.gameRenderer.checkEntityPostEffect(mc.getCameraEntity());
            this.lastShader = false;
         }
      }
   }

   public void suppress(float factor) {
      if (this.supAmount < 100.0F && Float.isFinite(factor)) {
         this.supAmount = this.supAmount + Math.min(factor, 100.0F - this.supAmount);
      }
   }

   public void overSuppress(float factor) {
      if (this.supAmount < 300.0F && Float.isFinite(factor)) {
         this.supAmount = this.supAmount + Math.min(factor, 300.0F - this.supAmount);
      }
   }

   public void renderConfusionOverlay(GuiGraphics p_282460_, float supPercent) {
      int i = p_282460_.guiWidth();
      int j = p_282460_.guiHeight();
      p_282460_.pose().pushPose();
      float f = Mth.lerp(supPercent, 2.0F, 1.0F);
      p_282460_.pose().translate(i / 2.0F, j / 2.0F, 0.0F);
      p_282460_.pose().scale(f, f, f);
      p_282460_.pose().translate(-i / 2.0F, -j / 2.0F, 0.0F);
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.ZERO, DestFactor.ONE_MINUS_SRC_COLOR);
      p_282460_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      p_282460_.blit(VIGNETTE_LOCATION, 0, 0, -90, 0.0F, 0.0F, i, j, i, j);
      p_282460_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      p_282460_.pose().popPose();
   }
}
