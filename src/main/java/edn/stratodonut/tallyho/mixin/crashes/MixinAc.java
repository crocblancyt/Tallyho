package edn.stratodonut.tallyho.mixin.crashes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.apigame.world.IPlayer;
import org.valkyrienskies.core.impl.shadow.Ac;

@Mixin(value = {Ac.class}, remap = false)
public abstract class MixinAc {
    @Unique
    private static final Set<Long> tyh$blankSet = new HashSet<>();

    @WrapOperation(method = {"a(Ljava/util/Set;Ljava/util/Set;Ljava/lang/Iterable;Ljava/lang/Iterable;)Lorg/valkyrienskies/core/impl/shadow/Ab;"}, at = {@At(value = "INVOKE", target = "Ljava/util/HashMap;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1)})
    private <V> V mixinFixPlayerCrash(HashMap<IPlayer, Set<Long>> instance, Object key, Operation<V> original) {
        V set = (V)original.call(new Object[] { instance, key });
        if (set == null)
            return (V)tyh$blankSet;
        return set;
    }
}
