package edn.stratodonut.tallyho.mixin.cw;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.clockwork.forge.ClockworkEvents;

@Mixin(value = {ClockworkEvents.class}, remap = false)
public abstract class MixinClockworkEvents {
    @Inject(method = {"playerLeftClick"}, at = {@At("HEAD")}, cancellable = true)
    private static void mixinPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event, CallbackInfo ci) {
      ci.cancel();
    }
}
