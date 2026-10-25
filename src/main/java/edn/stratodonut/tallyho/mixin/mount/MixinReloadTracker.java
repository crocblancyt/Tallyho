package edn.stratodonut.tallyho.mixin.mount;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mrcrayfish.guns.common.ReloadTracker;
import com.mrcrayfish.guns.item.GunItem;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadTracker.class)
public abstract class MixinReloadTracker {
   @Shadow
   @Final
   private ItemStack stack;

   @Inject(method = "isSameWeapon", at = @At("RETURN"), cancellable = true, remap = false)
   private void mixinIsSameWeapon(Player player, CallbackInfoReturnable<Boolean> cir) {
      if (player.getVehicle() instanceof GunMountEntity mg && this.stack != null && this.stack.equals(mg.getMountedGun(), true)) {
         cir.setReturnValue(true);
      }
   }

   @WrapOperation(
      method = "onPlayerTick(Lnet/minecraftforge/event/TickEvent$PlayerTickEvent;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelected()Lnet/minecraft/world/item/ItemStack;")
   )
   private static ItemStack isHoldingGun(Inventory instance, Operation<ItemStack> original, @Local Player player) {
      ItemStack g = (ItemStack)original.call(new Object[]{instance});
      return !(g.getItem() instanceof GunItem) && player.getVehicle() instanceof GunMountEntity mg ? mg.getMountedGun() : g;
   }

   @WrapOperation(
      method = "<init>",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelected()Lnet/minecraft/world/item/ItemStack;")
   )
   private ItemStack switchStack(Inventory instance, Operation<ItemStack> original, @Local Player player) {
      ItemStack g = (ItemStack)original.call(new Object[]{instance});
      return !(g.getItem() instanceof GunItem) && player.getVehicle() instanceof GunMountEntity mg ? mg.getMountedGun() : g;
   }
}
