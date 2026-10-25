package edn.stratodonut.tallyho.mixin.camera_possession;

import com.llamalad7.mixinextras.sugar.Local;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity;
import edn.stratodonut.tallyho.camera.entity.ThermalsEntity;
import edn.stratodonut.tallyho.client.SuppressionTracker;
import edn.stratodonut.tallyho.mixinducks.MixinGameRendererDuck;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements MixinGameRendererDuck {
   @Shadow
   private float renderDistance;
   @Shadow
   @Nullable
   private PostChain postEffect;

   @Shadow
   public abstract void loadEffect(ResourceLocation var1);

   @Override
   public PostChain getRenderer() {
      return this.postEffect;
   }

   @Inject(
      method = "checkEntityPostEffect",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraftforge/client/ForgeHooksClient;loadEntityShader(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/GameRenderer;)V"
      ),
      cancellable = true
   )
   private void railways$checkEntityPostEffect(Entity entity, CallbackInfo ci) {
      if (entity instanceof ThermalsEntity) {
         this.loadEffect(new ResourceLocation("shaders/post/thermals_old.json"));
         ci.cancel();
      } else if (entity instanceof RemoteCameraEntity) {
         this.loadEffect(new ResourceLocation("shaders/post/scan_pincushion.json"));
         ci.cancel();
      } else if (entity instanceof CameraEntity2 ce && ce.getOverlayType() == CameraEntity2.PostFXType.THERMALS_WHITE) {
         this.loadEffect(new ResourceLocation("shaders/post/thermals.json"));
         ci.cancel();
      }
   }

   @Inject(method = "getDepthFar", at = @At("HEAD"), cancellable = true)
   private void moreFar(CallbackInfoReturnable<Float> cir) {
      cir.setReturnValue(Math.max(this.renderDistance * 6.0F, 1500.0F));
   }

   @Inject(
      method = "render",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", shift = Shift.AFTER)
   )
   private void mixinConfusionOverlay(float p_109094_, long p_109095_, boolean p_109096_, CallbackInfo ci, @Local(ordinal = 0) GuiGraphics guigraphics) {
      SuppressionTracker.get().renderOverlay(guigraphics);
   }
}
