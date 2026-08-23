package edn.stratodonut.tallyho.mixin.cbc;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import riftyboi.cbcmodernwarfare.cannon_control.contraption.MountedMediumcannonContraption;
import riftyboi.cbcmodernwarfare.cannons.medium_cannon.breech.MediumcannonBreechBlockEntity;
import riftyboi.cbcmodernwarfare.index.CBCModernWarfareItem;

@Mixin(value = {CannonMountPoint.class}, remap = false)
public abstract class MixinCannonMountPoint {
    @Inject(method = {"getInsertedResultAndDoSomething(Lnet/minecraft/world/item/ItemStack;ZLrbasamoyai/createbigcannons/cannon_control/contraption/AbstractMountedCannonContraption;Lrbasamoyai/createbigcannons/cannon_control/contraption/PitchOrientedContraptionEntity;)Lnet/minecraft/world/item/ItemStack;"}, at = {@At("RETURN")}, cancellable = true)
    public void mixinGetInsertedResultAndDoSomething(ItemStack stack, boolean simulate, AbstractMountedCannonContraption cannon, PitchOrientedContraptionEntity poce, CallbackInfoReturnable<ItemStack> cir) {
        if (cannon instanceof MountedMediumcannonContraption) {
            MountedMediumcannonContraption medcannon = (MountedMediumcannonContraption)cannon;
            cir.setReturnValue(tyh$medcannonInsert(stack, simulate, medcannon, poce));
        }
    }

    @Unique
    private static ItemStack tyh$medcannonInsert(ItemStack stack, boolean simulate, MountedMediumcannonContraption medcannon, PitchOrientedContraptionEntity poce) {
        if (!(stack.getItem() instanceof riftyboi.cbcmodernwarfare.munitions.medium_cannon.MediumcannonAmmoItem))
            return stack;
        BlockEntity be = (BlockEntity)medcannon.presentBlockEntities.get(medcannon.getStartPos());
        if (be instanceof MediumcannonBreechBlockEntity) {
            MediumcannonBreechBlockEntity breech = (MediumcannonBreechBlockEntity)be;
            if (breech.canBeAutomaticallyLoaded()) {
                ItemStack containedItem = breech.getInputBuffer();
                if (containedItem.getItem() instanceof riftyboi.cbcmodernwarfare.munitions.medium_cannon.MediumcannonAmmoItem &&
                    !CBCModernWarfareItem.EMPTY_MEDIUMCANNON_CARTRIDGE.is(containedItem.getItem()))
                    return stack;
                if (simulate) {
                    stack.setCount(1);
                } else {
                    breech.clearInputBuffer();
                    breech.setInputBuffer(stack);
                    breech.setLoadingCooldown((int)Math.floor(tyh$getLoadingCooldown() * 1.8D));
                }
                return containedItem.isEmpty() ? ItemStack.EMPTY : containedItem;
            }
        }
        return stack;
    }

    @Unique
    private static int tyh$getLoadingCooldown() {
        return CBCConfigs.SERVER.cannons.quickfiringBreechLoadingCooldown.get();
    }
}