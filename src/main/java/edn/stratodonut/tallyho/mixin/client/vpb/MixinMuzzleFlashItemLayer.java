package edn.stratodonut.tallyho.mixin.client.vpb;

import com.vicmatskiv.pointblank.client.render.layer.MuzzleFlashItemLayer;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {MuzzleFlashItemLayer.class}, remap = false)
  public class MixinMuzzleFlashItemLayer {
    @Inject(method = {"isSupportedItemDisplayContext"}, at = {@At("HEAD")}, cancellable = true)
    private void mixinSupportGroundFlash(ItemDisplayContext itemDisplayContext, CallbackInfoReturnable<Boolean> cir) {
        if (itemDisplayContext == ItemDisplayContext.GROUND)
            cir.setReturnValue(Boolean.valueOf(true));
    }
}
