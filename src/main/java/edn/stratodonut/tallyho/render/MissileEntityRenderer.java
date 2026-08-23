package edn.stratodonut.tallyho.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import edn.stratodonut.tallyho.client.RenderUtil;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import edn.stratodonut.tallyho.missile.MotorComponent;
import edn.stratodonut.tallyho.missile.motor.RocketMotor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.effects.particles.smoke.ShellExplosionSmokeParticleData;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile;

public class MissileEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    private final ItemRenderer ir;

    public MissileEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.ir = Minecraft.getInstance().getItemRenderer();
    }

    public ResourceLocation getTextureLocation(T entity) {
        return null;
    }

    public void render(MountedMissileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
        if (entity.tickCount > 1) {
            ItemStack stack = entity.modelItem;
            BakedModel m = this.ir.getModel(stack, entity.level(), null, 0);
            if (entity.isDeployed())
                m = MissileRegistry.getDeployModel((MountedMissileEntity)entity).orElse(m);
            poseStack.pushPose();
            if (!RenderUtil.getModel(stack).usesBlockLight()) {
                poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, renderTypeBuffer, entity.level(), 0);
            } else {
                poseStack.translate(0.0F, entity.getHitboxScale() * 0.5F, 0.0F);
                poseStack.scale(1.5F, 1.5F, 1.5F);
                poseStack.translate(0.0F, 0.5F, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
                poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot()));
                this.ir.render(stack, ItemDisplayContext.NONE, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, m);
                MotorComponent motorComponent = entity.getMotor();
                if (motorComponent instanceof RocketMotor) {
                    RocketMotor motor = (RocketMotor)motorComponent;
                    if (entity.getTicksSinceLaunch() > 0 && entity.getTicksSinceLaunch() <= motor.getBurnTime()) {
                        Vec3 lookVel = entity.getLookAngle();
                        Vec3 thrustVel = entity.getDeltaMovement().add(lookVel.scale(-1.0D));
                        Vec3 spawnPos = entity.getEyePosition().add(lookVel.scale(-motor.renderOffset));
                        Vec3 vel = entity.getDeltaMovement();
                        float scale = entity.getHitboxScale();
                        Vec3 oldPos = spawnPos.subtract(entity.getDeltaMovement());
                        AbstractBigCannonProjectile.TrailType trailType = (AbstractBigCannonProjectile.TrailType)CBCConfigs.SERVER.munitions.bigCannonTrailType.get();
                        int lifetime = (trailType == AbstractBigCannonProjectile.TrailType.SHORT) ? 20 : (40 + entity.level().random.nextInt(50));
                        if (trailType != AbstractBigCannonProjectile.TrailType.NONE) {
                            ShellExplosionSmokeParticleData shellExplosionSmokeParticleData = new ShellExplosionSmokeParticleData(lifetime, scale * 10.0F);
                            for (int i = 0; i < 10; i++) {
                                double partial = (i * 0.1F);
                                Vec3 dPos = oldPos.lerp(spawnPos, partial);
                                entity.level().addParticle((ParticleOptions)shellExplosionSmokeParticleData, true, dPos.x, dPos.y + 0.05D, dPos.z, thrustVel.x, thrustVel.y, thrustVel.z);
                            }
                        }
                    }
                }
            }
            poseStack.popPose();
        }
    }

    public float getVelocityBasedScale(Vec3 velocity) {
        double velocityMagnitude = velocity.length();
        float minScale = 1.0F;
        float maxScale = 8.0F;
        float baselineSpeed = 0.5F;
        if (velocityMagnitude == 0.0D)
            return maxScale;
        float scale = maxScale - (float)(Math.log(velocityMagnitude / baselineSpeed) * 1.5D);
        scale = Math.max(minScale, Math.min(maxScale, scale));
        return scale;
    }
}