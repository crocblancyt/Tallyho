package edn.stratodonut.tallyho.mixin.cgm;

import com.mrcrayfish.guns.util.GunEnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = {GunEnchantmentHelper.class}, remap = false)
public abstract class MixinGunEnchantmentHelper {
    @Inject(method = {"getAimDownSightSpeed"}, at = {@At("RETURN")}, cancellable = true)
    private static void mixinGetAimDownSightSpeed(ItemStack weapon, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(Double.valueOf((cir.getReturnValue()).doubleValue() * 0.8D));
    }
}
