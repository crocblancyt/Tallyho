package edn.stratodonut.tallyho.mixin.client.vpb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vicmatskiv.pointblank.client.render.DefaultRenderTypeProvider;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState.CompositeStateBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DefaultRenderTypeProvider.class)
public abstract class MixinVPBRenderTypeProvider extends RenderStateShard {
   public MixinVPBRenderTypeProvider(String p_110161_, Runnable p_110162_, Runnable p_110163_) {
      super(p_110161_, p_110162_, p_110163_);
   }

   @WrapOperation(
      method = "createMuzzleFlashRenderType",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setShaderState(Lnet/minecraft/client/renderer/RenderStateShard$ShaderStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;"
      )
   )
   private static CompositeStateBuilder mixinFixTransparency(
      CompositeStateBuilder instance, ShaderStateShard p_173293_, Operation<CompositeStateBuilder> original
   ) {
      return ((CompositeStateBuilder)original.call(new Object[]{instance, p_173293_}))
         .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
         .setWriteMaskState(COLOR_WRITE)
         .setLightmapState(RenderStateShard.NO_LIGHTMAP);
   }
}
