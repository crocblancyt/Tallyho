package edn.stratodonut.tallyho.mixin.mount;

import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidPose extends AgeableListModel {
   @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
   public <T extends LivingEntity> void createbigcannons$setupAnimNearTail(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
      HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
      if (livingEntity.getVehicle() instanceof GunMountEntity) {
         self.leftArm.xRot = self.head.xRot - 1.57F;
         self.leftArm.yRot = 0.25F;
         self.leftArm.zRot = 0.0F;
         self.rightArm.xRot = self.head.xRot - 1.57F;
         self.rightArm.yRot = -0.25F;
         self.rightArm.zRot = 0.0F;
         self.leftLeg.xRot = 0.0F;
         self.rightLeg.xRot = 0.0F;
      }
   }
}
