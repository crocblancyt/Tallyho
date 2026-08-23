package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Player.class})
public abstract class MixinPlayer extends LivingEntity {
    protected MixinPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = {"isScoping"}, at = {@At("RETURN")}, cancellable = true)
    public void mixinIsScoping(CallbackInfoReturnable<Boolean> cir) {
        if (!level().isClientSide())
            return;
        if (CameraClientHandler.isPlayerMountedOnCamera()) {
            CameraEntity2 ce = CameraClientHandler.getPlayerMountedOnCamera();
            switch (ce.getOverlayType()) {
                case SCOPE:
                case PERISCOPE:
                case THERMALS_GREEN:
                case THERMALS_WHITE:
                case VISIBLE:
                    cir.setReturnValue(true);
                    break;
                default:
                    break;
            }
        } else if (getUseItem().getItem() instanceof edn.stratodonut.tallyho.item.LaserDesignatorItem) {
            cir.setReturnValue(true);
        }
    }
}