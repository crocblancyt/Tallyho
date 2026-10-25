package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.ICameraStatus;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.ILaserDesignator;
import edn.stratodonut.tallyho.item.LaserDesignatorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {
   @Unique
   private static final ResourceLocation SCOPE_LOCATION = TallyhoMod.asResource("textures/misc/scope.png");
   @Unique
   private static final ResourceLocation PERISCOPE_LOCATION = TallyhoMod.asResource("textures/misc/periscope.png");
   @Unique
   private static final ResourceLocation THERMALS_LOCATION = TallyhoMod.asResource("textures/misc/thermals.png");
   @Unique
   private static final ResourceLocation MULTISPEC_LOCATION = TallyhoMod.asResource("textures/misc/apache.png");
   @Unique
   private static final ResourceLocation LASER_CROSS_LOCATION = TallyhoMod.asResource("textures/misc/laser.png");
   @Shadow
   protected int screenWidth;
   @Shadow
   protected int screenHeight;

   @Shadow
   public abstract Font getFont();

   @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
   private void railways$cancelExperienceBar(GuiGraphics p_281906_, int p_282731_, CallbackInfo ci) {
      if (CameraClientHandler.isPlayerMountedOnCamera()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
   private void mixinRenderCrosshair(GuiGraphics p_282828_, CallbackInfo ci) {
      if (CameraClientHandler.isPlayerMountedOnCamera()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
   private void mixinRenderSpyglassOverlay(GuiGraphics p_282069_, float p_283442_, CallbackInfo ci) {
      if (CameraClientHandler.isPlayerMountedOnCamera()) {
         CameraEntity2 ce = CameraClientHandler.getPlayerMountedOnCamera();
         switch (ce.getOverlayType()) {
            case THERMALS_GREEN:
               this.tyh$renderOverlay(p_282069_, THERMALS_LOCATION, p_283442_);
               break;
            case PERISCOPE:
               this.tyh$renderOverlay(p_282069_, PERISCOPE_LOCATION, p_283442_);
               break;
            case SCOPE:
               this.tyh$renderOverlay(p_282069_, SCOPE_LOCATION, p_283442_);
               break;
            case THERMALS_WHITE:
            case VISIBLE:
               this.tyh$renderOverlay(p_282069_, MULTISPEC_LOCATION, p_283442_);
            case NONE:
               break;
            default:
               return;
         }

         if (ce instanceof ICameraStatus status) {
            this.tyh$renderStatus(p_282069_, status.getStatus());
         }

         if (ce instanceof ILaserDesignator d && d.isDesignatorActive()) {
            this.tyh$renderOverlay(p_282069_, LASER_CROSS_LOCATION, p_283442_);
         }

         ci.cancel();
      } else if (Minecraft.getInstance().player != null) {
         Player player = Minecraft.getInstance().player;
         ItemStack stack = player.getUseItem();
         if (stack.getItem() instanceof LaserDesignatorItem) {
            this.tyh$renderOverlay(p_282069_, MULTISPEC_LOCATION, p_283442_);
            if (LaserDesignatorItem.isActiveDesignating(stack)) {
               this.tyh$renderOverlay(p_282069_, LASER_CROSS_LOCATION, p_283442_);
            }

            ci.cancel();
         }
      }
   }

   @Unique
   protected void tyh$renderStatus(GuiGraphics gui, String status) {
      int width = this.screenWidth;
      int y = this.screenHeight - 75;
      gui.drawCenteredString(this.getFont(), status, width / 2, y - 10, 13426175);
   }

   @Unique
   protected void tyh$renderOverlay(GuiGraphics p_282069_, ResourceLocation roc, float p_168676_) {
      int i = Mth.floor(this.screenWidth * p_168676_);
      int j = Mth.floor(this.screenHeight * p_168676_);
      int k = (this.screenWidth - i) / 2;
      int l = (this.screenHeight - j) / 2;
      int i1 = k + i;
      int j1 = l + j;
      p_282069_.blit(roc, k, l, -90, 0.0F, 0.0F, i, j, i, j);
      p_282069_.fill(RenderType.guiOverlay(), 0, j1, this.screenWidth, this.screenHeight, -90, -16777216);
      p_282069_.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, l, -90, -16777216);
      p_282069_.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
      p_282069_.fill(RenderType.guiOverlay(), i1, l, this.screenWidth, j1, -90, -16777216);
   }
}
