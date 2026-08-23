package edn.stratodonut.tallyho.mixin.mount;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HumanoidModel.class})
public abstract class MixinHumanoidPose extends AgeableListModel {
    @Inject(method = {"setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"}, at = {@At("TAIL")})
    public <T extends net.minecraft.world.entity.LivingEntity> void createbigcannons$setupAnimNearTail(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        HumanoidModel<?> self = (HumanoidModel)(Object)this;
        if (livingEntity.getVehicle() instanceof edn.stratodonut.tallyho.entity.GunMountEntity) {
            self.rightArm.xRot -= 1.57F;
            self.leftArm.z = 0.25F;
            self.leftArm.x = 0.0F;
            self.rightArm.xRot -= 1.57F;
            self.leftLeg.z = -0.25F;
            self.leftLeg.x = 0.0F;
            self.rightLeg.xRot = 0.0F;
            self.leftLeg.xRot = 0.0F;
        }
    }
}