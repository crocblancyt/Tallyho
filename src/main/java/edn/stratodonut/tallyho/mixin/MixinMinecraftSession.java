package edn.stratodonut.tallyho.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({YggdrasilMinecraftSessionService.class})
public abstract class MixinMinecraftSession {
    @WrapOperation(method = {"getTextures"}, at = {@At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService;getSecurePropertyValue(Lcom/mojang/authlib/properties/Property;)Ljava/lang/String;")}, remap = false)
    public String bypassSignatureCheck(YggdrasilMinecraftSessionService instance, Property property, Operation<String> original) {
        if (!property.hasSignature())
            return property.getValue();
        return original.call(instance, property);
    }
}
