package edn.stratodonut.tallyho.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.impl.networking.NetworkChannel;

@Mixin(value = {NetworkChannel.class}, remap = false)
public class MixinNetworkChannel {
    @WrapOperation(method = {"clientReceive"}, at = {@At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;debug(Ljava/lang/String;)V")})
    private void stopSpammingTheLog(Logger instance, String s, Operation<Void> original) {}
}
