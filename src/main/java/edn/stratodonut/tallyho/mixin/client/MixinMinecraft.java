package edn.stratodonut.tallyho.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.AllGameRules;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Minecraft.class})
public class MixinMinecraft {
    @WrapOperation(method = {"handleKeybinds"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V")})
    private void cancelChangeCameraAndForce(Options instance, CameraType cameraType, Operation<Void> original) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && localPlayer.level().getGameRules().getRule(AllGameRules.RULE_LIMITF5).get() &&
            !localPlayer.isCreative() && !localPlayer.isSpectator()) {
            original.call(instance, CameraType.FIRST_PERSON);
        } else {
            original.call(instance, cameraType);
        }
    }
}