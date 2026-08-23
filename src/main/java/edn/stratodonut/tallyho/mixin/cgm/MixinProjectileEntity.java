package edn.stratodonut.tallyho.mixin.cgm;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mrcrayfish.guns.entity.ProjectileEntity;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.SuppressionPacket;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Intersectiond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

@Pseudo
@Mixin({ProjectileEntity.class})
public abstract class MixinProjectileEntity extends Entity {
    @Shadow
    protected LivingEntity shooter;

    public MixinProjectileEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    @Nonnull
    private Vec3 getVectorFromRotation(float pitch, float yaw) {
        TallyhoMod.warn("MIXIN SHADOW FAILED!!!");
        return null;
    }

    @Shadow
    public abstract float getDamage();

    @Inject(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lcom/mrcrayfish/guns/entity/ProjectileEntity;setPos(DDD)V")})
    private void onTickEnd(CallbackInfo ci) {
        if (getDeltaMovement().length() > 10.0D)
            if (!level().isClientSide)
                level().players().stream()
                    .filter(p -> (!p.isCreative() && (!p.isPassenger() || VSGameUtilsKt.getShipManaging(p.getRootVehicle()) == null) && getDeltaMovement().dot(p.position().subtract(position())) >= 0.0D && ((this.shooter != null && !Objects.equals(this.shooter.getTeam(), p.getTeam())) || this.tickCount > 0)))
                    .forEach(p -> {
                        Vector3d bp = VectorConversionsMCKt.toJOML(position());
                        Vector3d be = VectorConversionsMCKt.toJOML(position().add(getDeltaMovement()));
                        Vector3d po = VectorConversionsMCKt.toJOML(p.position());
                        if (Intersectiond.testLineSegmentSphere(VectorConversionsMCKt.toJOML(position()), VectorConversionsMCKt.toJOML(position().add(getDeltaMovement())), (Vector3dc)VectorConversionsMCKt.toJOML(p.position()), 25.0D)) {
                            float distance = (float)p.position().distanceTo(VectorConversionsMCKt.toMinecraft(Intersectiond.findClosestPointOnLineSegment(bp.x, bp.y, bp.z, be.x, be.y, be.z, po.x, po.y, po.z, new Vector3d())));
                            distance = Math.max(distance, 0.5F);
                            CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)p), new SuppressionPacket(getDamage() / distance));
                        }
                    });
    }

    @Inject(method = {"rayTraceBlocks"}, at = {@At("HEAD")}, cancellable = true, remap = false)
    private static void injectRayTraceBlocks(Level world, ClipContext context, Predicate<BlockState> ignorePredicate, CallbackInfoReturnable<BlockHitResult> cir) {
        cir.setReturnValue(RaycastUtilsKt.clipIncludeShips(world, context));
    }

    @WrapOperation(method = {"getDirection"}, at = {@At(value = "INVOKE", target = "Lcom/mrcrayfish/guns/util/GunModifierHelper;getModifiedSpread(Lnet/minecraft/world/item/ItemStack;F)F")}, remap = false)
    private float mixinGetDirection(ItemStack modifier, float modifiers, Operation<Float> original, @Local LivingEntity shooter) {
        float spread = original.call(modifier, modifiers);
        if (shooter instanceof Player) {
            Player player = (Player)shooter;
            if (!ModSyncedDataKeys.AIMING.getValue(player) && spread != 0.0F && !(shooter.getVehicle() instanceof GunMountEntity))
                spread += 7.0F;
            float walkStrength = (player.xxa - player.zza) / player.getSpeed();
            spread += walkStrength * 10.0F;
            if (player.isCrouching() || player.getVehicle() != null)
                spread *= 0.9F;
            if (player.isAutoSpinAttack())
                spread *= 0.5F;
        }
        return spread;
    }

    @Redirect(method = {"getDirection"}, at = @At(value = "INVOKE", target = "Lcom/mrcrayfish/guns/entity/ProjectileEntity;getVectorFromRotation(FF)Lnet/minecraft/world/phys/Vec3;"), remap = false)
    private Vec3 mixinVectorFromRotation(ProjectileEntity instance, float pitch, float yaw, @Local LivingEntity e) {
        LoadedShip loadedShip = null;
        Ship s = null;
        Vec3 vec = getVectorFromRotation(pitch, yaw);
        if (e.isPassenger()) {
            Entity entity = e.getVehicle();
            if (entity instanceof GunMountEntity) {
                GunMountEntity gme = (GunMountEntity)entity;
                vec = getVectorFromRotation(gme.getXRot(), gme.getYRot());
            }
            loadedShip = VSGameUtilsKt.getShipObjectManagingPos(e.level(), VectorConversionsMCKt.toJOML(e.getRootVehicle().position()));
        }
        if (loadedShip == null)
            return vec;
        return VectorConversionsMCKt.toMinecraft(loadedShip.getTransform().getShipToWorldRotation().transform(VectorConversionsMCKt.toJOML(vec)));
    }
}