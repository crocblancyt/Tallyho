package edn.stratodonut.tallyho.mixin.client.vpb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.client.ClientEvents;
import edn.stratodonut.tallyho.client.GunMountTracker;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rbasamoyai.createbigcannons.CBCClientCommon;

@Mixin(value = ClientEventHandler.class, remap = false)
public class MixinClientEventHandler {
   @WrapOperation(method = "runSyncTick", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V"))
   private static void mixinRunSyncTick(Runnable instance, Operation<Void> original) {
      original.call(new Object[]{instance});
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (ClientEvents.isControllingMG(mc.player)) {
            if (!CameraClientHandler.isPlayerMountedOnCamera()) {
               mc.player.handsBusy = true;
            }

            if (CBCClientCommon.FIRE_CONTROLLED_CANNON.isDown() && mc.player.getVehicle() instanceof GunMountEntity mg) {
               ItemStack gunStack = mg.getMountedGun();
               GunMountTracker.fireIfPossible(mc.player, mg, gunStack);
            }
         }

         GunMountTracker.tick();
      }
   }
}
