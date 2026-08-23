package edn.stratodonut.tallyho.mixin.client.vpb;

import com.google.common.base.Objects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import edn.stratodonut.tallyho.client.GunMountTracker;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;

@Mixin(value = {GunItemRenderer.class}, remap = false)
public class MixinGunItemRenderer {
    @Inject(method = {"renderMuzzleFlash"}, at = {@At("HEAD")})
    private void mixinRenderMuzzleFlash(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, CallbackInfo ci) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && localPlayer.getVehicle() instanceof edn.stratodonut.tallyho.entity.GunMountEntity) {
            List<MuzzleFlashEffect> flashes = GunMountTracker.getMuzzleFlashList();
            if (!flashes.isEmpty()) {
                List<GeoCube> cubes = bone.getCubes();
                if (cubes != null && !cubes.isEmpty()) {
                    GeoCube cube = cubes.get(0);
                    GeoQuad quad1 = cube.quads()[0];
                    Vector3f v1Position = quad1.vertices()[0].position();
                    GeoQuad quad2 = cube.quads()[2];
                    Vector3f v3Position = quad2.vertices()[2].position();
                    Vector3f position = new Vector3f();
                    position = v3Position.sub(v1Position, position);
                    position = position.mul(0.5F);
                    position = position.add(v1Position);
                    EffectRenderContext context = (new EffectRenderContext()).withPoseStack(poseStack).withPosition(new Vec3(position.x, position.y, position.z)).withVertexBuffer(buffer).withLightColor(packedLight);
                    for (MuzzleFlashEffect effect : flashes) {
                        UUID effectId = EffectRegistry.getEffectId(effect.getName());
                        if (Objects.equal(effectId, GunItemRenderer.RenderPass.getEffectId()))
                            effect.render(context);
                    }
                }
            }
        }
    }
}