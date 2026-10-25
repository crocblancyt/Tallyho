package edn.stratodonut.tallyho.mixin.vpb;

import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.EffectLauncher;
import com.vicmatskiv.pointblank.item.GunItem.FirePhase;
import edn.stratodonut.tallyho.mixinducks.MixinEffectLauncherDuck;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EffectLauncher.class, remap = false)
public abstract class MixinEffectLauncher implements MixinEffectLauncherDuck {
   @Shadow
   private Map<FirePhase, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>>> effectBuilders;

   @Nonnull
   @Override
   public List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> tyh$getPhaseBuilders(FirePhase phase) {
      return this.effectBuilders.computeIfAbsent(phase, k -> new ArrayList<>());
   }
}
