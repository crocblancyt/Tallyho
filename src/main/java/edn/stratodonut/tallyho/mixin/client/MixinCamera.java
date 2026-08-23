package edn.stratodonut.tallyho.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

@Mixin(value = {Camera.class}, priority = 1024)
public abstract class MixinCamera {
    @Shadow
    private Entity entity;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    private Vec3 position;

    private double getMaxZoomIgnoringMountedShip(Level level, double maxZoom, @NotNull ClientShip toIgnore) {
        for (int i = 0; i < 8; i++) {
            float f = ((i & 0x1) * 2 - 1);
            float g = ((i >> 1 & 0x1) * 2 - 1);
            float h = ((i >> 2 & 0x1) * 2 - 1);

            f *= 0.1F;
            g *= 0.1F;
            h *= 0.1F;

            Vec3 vec3 = this.position.add(f, g, h);
            Vec3 vec32 = new Vec3(
                    this.position.x - this.forwards.x() * maxZoom + f + h,
                    this.position.y - this.forwards.y() * maxZoom + g,
                    this.position.z - this.forwards.z() * maxZoom + h
            );

            BlockHitResult blockHitResult = RaycastUtilsKt.vanillaClip(level, new ClipContext(vec3, vec32, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity));

            if (blockHitResult.getType() != HitResult.Type.MISS) {
                double e = blockHitResult.getLocation().distanceTo(this.position);
                if (e < maxZoom)
                    maxZoom = e;
            }
        }
        return maxZoom;
    }
}