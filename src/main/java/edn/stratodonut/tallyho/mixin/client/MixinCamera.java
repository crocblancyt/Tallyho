package edn.stratodonut.tallyho.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

@Mixin(value = Camera.class, priority = 1024)
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
         float f = (i & 1) * 2 - 1;
         float g = (i >> 1 & 1) * 2 - 1;
         float h = (i >> 2 & 1) * 2 - 1;
         f *= 0.1F;
         g *= 0.1F;
         h *= 0.1F;
         Vec3 vec3 = this.position.add(f, g, h);
         Vec3 vec32 = new Vec3(
            this.position.x - this.forwards.x() * maxZoom + f + h,
            this.position.y - this.forwards.y() * maxZoom + g,
            this.position.z - this.forwards.z() * maxZoom + h
         );
         HitResult hitResult = RaycastUtilsKt.vanillaClip(level, new ClipContext(vec3, vec32, Block.VISUAL, Fluid.NONE, this.entity));
         if (hitResult.getType() != Type.MISS) {
            double e = hitResult.getLocation().distanceTo(this.position);
            if (e < maxZoom) {
               maxZoom = e;
            }
         }
      }

      return maxZoom;
   }
}
