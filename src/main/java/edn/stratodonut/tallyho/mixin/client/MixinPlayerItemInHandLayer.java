package edn.stratodonut.tallyho.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.item.LaserDesignatorItem;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerItemInHandLayer.class)
public abstract class MixinPlayerItemInHandLayer {
   @WrapOperation(
      method = "renderArmWithItem",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
   )
   private boolean mixinIsSpyglass(ItemStack instance, Item item, Operation<Boolean> original) {
      return (Boolean)original.call(new Object[]{instance, item}) || instance.getItem() instanceof LaserDesignatorItem;
   }
}
