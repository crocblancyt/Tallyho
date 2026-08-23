package edn.stratodonut.tallyho.mixin.cbc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import riftyboi.cbcmodernwarfare.cannon_control.compact_mount.CompactCannonMountPoint;

@Pseudo
@Mixin(value = {CompactCannonMountPoint.class}, remap = false)
public abstract class MixinCompactCannonMountPoint {
    @Redirect(method = {"mediumcannonInsert(Lnet/minecraft/world/item/ItemStack;ZLriftyboi/cbcmodernwarfare/cannon_control/contraption/MountedMediumcannonContraption;Lrbasamoyai/createbigcannons/cannon_control/contraption/PitchOrientedContraptionEntity;)Lnet/minecraft/world/item/ItemStack;"}, at = @At(value = "INVOKE", target = "Lriftyboi/cbcmodernwarfare/cannon_control/compact_mount/CompactCannonMountPoint;getLoadingCooldown()I"))
    private static int mixinGetLoadingCooldown() {
        return (int)Math.floor(CBCConfigs.SERVER.cannons.quickfiringBreechLoadingCooldown.get() * 1.8D);
    }
}