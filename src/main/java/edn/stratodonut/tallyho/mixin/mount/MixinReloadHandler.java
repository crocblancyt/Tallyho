package edn.stratodonut.tallyho.mixin.mount;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ReloadHandler.class})
public abstract class MixinReloadHandler {
    @WrapOperation(method = {"setReloading"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;")})
    private ItemStack mixinSetReloading(Player player, Operation<ItemStack> original) {
        ItemStack gun = original.call(player);
        if (!(gun.getItem() instanceof com.mrcrayfish.guns.item.GunItem)) {
            Entity entity = player.getVehicle();
            if (entity instanceof GunMountEntity) {
                GunMountEntity mg = (GunMountEntity)entity;
                gun = mg.getMountedGun();
            }
        }
        return gun;
    }

    @WrapOperation(method = {"onClientTick"}, at = {@At(value = "INVOKE", target = "Lcom/mrcrayfish/guns/client/handler/ReloadHandler;setReloading(Z)V")}, remap = false)
    private void mixinOnTick(ReloadHandler instance, boolean tag, Operation<Void> original) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (!(localPlayer.getVehicle() instanceof GunMountEntity))
            original.call(instance, tag);
    }
}