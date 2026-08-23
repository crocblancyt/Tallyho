package edn.stratodonut.tallyho.mixin.client;

import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.playerrevive.server.PlayerReviveServer;

@Mixin(value = {GunItem.class}, remap = false)
public abstract class MixinClientGunItem {
    @Inject(method = {"tryFire"}, at = {@At("HEAD")}, cancellable = true)
    private void mixinTryFire(LocalPlayer player, ItemStack itemStack, Entity targetEntity, CallbackInfoReturnable<Boolean> cir) {
        if (isDown(player) || player.isCrouching())
            cir.setReturnValue(false);
    }

    @Inject(method = {"tryReload"}, at = {@At("HEAD")}, cancellable = true)
    private void mixinTryReload(Player player, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (!isDown(player)) {
            if (player instanceof LocalPlayer) {
                LocalPlayer lp = (LocalPlayer)player;
                if (lp.isCrouching()) {
                    cir.setReturnValue(false);
                    return;
                }
            }
            return;
        }
        cir.setReturnValue(false);
    }

    @Unique
    private boolean isDown(Player player) {
        return PlayerReviveServer.isBleeding(player);
    }
}