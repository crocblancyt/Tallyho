package edn.stratodonut.tallyho.mixin;

import alpvax.mc.goprone.PlayerProneData;
import alpvax.mc.goprone.platform.service.IPronePlatform;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.AllGameRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.util.EntityShipCollisionUtils;

@Mixin(value = {PlayerProneData.class}, remap = false)
public abstract class MixinPlayerProneData {
    @Shadow
    private boolean shouldBeProne;

    @Unique
    private static final Vec3 TEST_MOVEMENT = new Vec3(0.0D, 1.0D, 0.0D);

    @WrapOperation(method = {"updateProneState()V"}, at = {@At(value = "INVOKE", target = "Lalpvax/mc/goprone/platform/service/IPronePlatform;setForcedPose(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Pose;)V")})
    private void mixinUpdateProneState(IPronePlatform instance, Player player, Pose pose, Operation<Void> original) {
        if (pose == null && !player.level().getGameRules().getRule(AllGameRules.RULE_PRONE_CLIPPING).get()) {
            Vec3 checkMovement = EntityShipCollisionUtils.INSTANCE.adjustEntityMovementForShipCollisions(player, TEST_MOVEMENT, player
                    .getBoundingBox(), player
                    .level());
            if (checkMovement.subtract(TEST_MOVEMENT).lengthSqr() > 0.010000000000000002D) {
                this.shouldBeProne = true;
                return;
            }
        }
        original.call(instance, player, pose);
    }
}