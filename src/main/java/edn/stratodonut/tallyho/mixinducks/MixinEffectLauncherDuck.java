package edn.stratodonut.tallyho.mixinducks;

import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.List;
import java.util.function.Supplier;

public interface MixinEffectLauncherDuck {
    List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> tyh$getPhaseBuilders(GunItem.FirePhase paramFirePhase);
}
