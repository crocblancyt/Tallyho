package edn.stratodonut.tallyho.mixin.cbc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.client.SuppressionTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Intersectiond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.autocannon.AbstractAutocannonProjectile;

@Mixin({AbstractAutocannonProjectile.class})
public abstract class MixinClientAutocannonProjectile extends AbstractCannonProjectile {
    protected MixinClientAutocannonProjectile(EntityType<? extends AbstractCannonProjectile> type, Level level) {
        super(type, level);
    }

    @WrapOperation(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;expandTowards(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;")})
    public AABB suppress(AABB instance, Vec3 reverseDir, Operation<AABB> original) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && !localPlayer.isCreative() && this.tickCount > 1 && (
            !localPlayer.isPassenger() || VSGameUtilsKt.getShipManaging(localPlayer.getRootVehicle()) == null)) {
            Vector3d bp = VectorConversionsMCKt.toJOML(position());
            Vector3d be = VectorConversionsMCKt.toJOML(position().add(reverseDir));
            Vector3d po = VectorConversionsMCKt.toJOML(localPlayer.position());
            if (Intersectiond.testLineSegmentSphere(
                VectorConversionsMCKt.toJOML(position()),
                VectorConversionsMCKt.toJOML(position().add(reverseDir)),
                VectorConversionsMCKt.toJOML(localPlayer.position()), 25.0D)) {
                float distance = (float)localPlayer.position().distanceTo(VectorConversionsMCKt.toMinecraft(Intersectiond.findClosestPointOnLineSegment(bp.x, bp.y, bp.z, be.x, be.y, be.z, po.x, po.y, po.z, new Vector3d())));
                distance = Math.max(distance, 0.5F);
                SuppressionTracker.get().suppress(20.0F / distance);
            }
        }
        return original.call(instance, reverseDir);
    }
}