package edn.stratodonut.tallyho.mixin.client.vpb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vicmatskiv.pointblank.client.render.DefaultRenderTypeProvider;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({DefaultRenderTypeProvider.class})
public abstract class MixinVPBRenderTypeProvider extends RenderStateShard {
    public MixinVPBRenderTypeProvider(String name, Runnable setup, Runnable clear) {
        super(name, setup, clear);
    }

    @WrapOperation(method = {"createMuzzleFlashRenderType"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setShaderState(Lnet/minecraft/client/renderer/RenderStateShard$ShaderStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;")})
    private static RenderType.CompositeState.CompositeStateBuilder mixinFixTransparency(RenderType.CompositeState.CompositeStateBuilder instance, RenderStateShard.ShaderStateShard shaderState, Operation<RenderType.CompositeState.CompositeStateBuilder> original) {
        return original.call(instance, shaderState)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL);
    }
}