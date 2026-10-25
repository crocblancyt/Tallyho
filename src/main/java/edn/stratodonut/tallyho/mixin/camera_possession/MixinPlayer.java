package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.item.LaserDesignatorItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {
   protected MixinPlayer(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
   }

   @Inject(method = "isScoping", at = @At("RETURN"), cancellable = true)
   public void mixinIsScoping(CallbackInfoReturnable<Boolean> cir) {
      if (this.level().isClientSide()) {
         if (CameraClientHandler.isPlayerMountedOnCamera()) {
            CameraEntity2 ce = CameraClientHandler.getPlayerMountedOnCamera();
            switch (ce.getOverlayType()) {
               case SCOPE:
               case PERISCOPE:
               case THERMALS_GREEN:
               case THERMALS_WHITE:
               case VISIBLE:
                  cir.setReturnValue(true);
            }
         } else if (this.getUseItem().getItem() instanceof LaserDesignatorItem) {
            cir.setReturnValue(true);
         }
      }
   }
}
