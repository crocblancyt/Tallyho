package edn.stratodonut.tallyho.mixin;

import com.jesz.createdieselgenerators.Events;
import net.minecraftforge.event.level.ExplosionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Events.class)
public abstract class MixinEventsCDG {
   @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true, remap = false)
   private static void mixinOnExplosion(ExplosionEvent event, CallbackInfo ci) {
      ci.cancel();
   }
}
