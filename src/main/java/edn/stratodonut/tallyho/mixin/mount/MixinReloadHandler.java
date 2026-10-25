package edn.stratodonut.tallyho.mixin.mount;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.item.GunItem;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ReloadHandler.class)
public abstract class MixinReloadHandler {
   @WrapOperation(
      method = "setReloading",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;")
   )
   private ItemStack mixinSetReloading(Player player, Operation<ItemStack> original) {
      ItemStack gun = (ItemStack)original.call(new Object[]{player});
      if (!(gun.getItem() instanceof GunItem) && player.getVehicle() instanceof GunMountEntity mg) {
         gun = mg.getMountedGun();
      }

      return gun;
   }

   @WrapOperation(
      method = "onClientTick",
      at = @At(value = "INVOKE", target = "Lcom/mrcrayfish/guns/client/handler/ReloadHandler;setReloading(Z)V"),
      remap = false
   )
   private void mixinOnTick(ReloadHandler instance, boolean tag, Operation<Void> original) {
      Player player = Minecraft.getInstance().player;
      if (!(player.getVehicle() instanceof GunMountEntity)) {
         original.call(new Object[]{instance, tag});
      }
   }
}
