package edn.stratodonut.tallyho.mixin.vpb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.EffectLauncher;
import com.vicmatskiv.pointblank.feature.AccuracyFeature;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.HurtingItem;
import edn.stratodonut.tallyho.AllGameRules;
import edn.stratodonut.tallyho.AllSounds;
import edn.stratodonut.tallyho.ServerSuppressionTracker;
import edn.stratodonut.tallyho.mixinducks.MixinEffectLauncherDuck;
import edn.stratodonut.tallyho.mixinducks.MixinGunItemDuck;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Intersectiond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Pseudo
@Mixin({GunItem.class})
public abstract class MixinGunItem extends HurtingItem implements MixinGunItemDuck {
    @Shadow
    private int pelletCount;

    @Shadow
    private double pelletSpread;

    @Shadow
    private double inaccuracy;

    @Shadow
    private double inaccuracyAiming;

    @Shadow
    private double inaccuracySprinting;

    @Shadow
    private EffectLauncher effectLauncher;

    @Shadow
    private List<GunItem.PhasedReload> phasedReloads;

    @Shadow
    private double shakeDecay;

    @Shadow
    private int shotsPerTrace;

    @Shadow
    protected abstract double adjustInaccuracy(Player player, ItemStack itemStack, boolean isAiming);

    @Shadow
    protected abstract AmmoItem getFirstCompatibleProjectile(ItemStack gunStack, FireModeInstance fireModeInstance);

    @Shadow
    protected abstract double getMaxServerShootingDistance(ItemStack itemStack, boolean isAiming, ServerLevel level);

    @Shadow
    protected abstract void hitScanTarget(Player player, ItemStack itemStack, int slotIndex, int correlationId, HitResult hitResult, double maxHitScanDistance, List<BlockPos> blockPosToDestroy);

    @Shadow
    private static long getOrAssignRandomSeed(ItemStack stack) {
        return 0L;
    }

    public MixinGunItem(Item.Properties properties, HurtingItem.Builder<?> builder) {
        super(properties, builder);
    }

    public double tyh$AdjustInaccuracy(Player player, ItemStack itemStack, boolean isAiming) {
        return adjustInaccuracy(player, itemStack, isAiming);
    }

    public AmmoItem tyh$getFirstCompatibleProjectile(ItemStack gunStack, FireModeInstance fireModeInstance) {
        return getFirstCompatibleProjectile(gunStack, fireModeInstance);
    }

    public double tyh$getMaxServerShootingDistance(ItemStack itemStack, boolean isAiming, ServerLevel level) {
        return getMaxServerShootingDistance(itemStack, isAiming, level);
    }

    public void tyh$hitScanTarget(Player player, ItemStack itemStack, int slotIndex, int correlationId, HitResult hitResult, double maxHitScanDistance, List<BlockPos> blockPosToDestroy) {
        hitScanTarget(player, itemStack, slotIndex, correlationId, hitResult, maxHitScanDistance, blockPosToDestroy);
    }

    public long tyh$getOrAssignRandomSeed(ItemStack stack) {
        return getOrAssignRandomSeed(stack);
    }

    @Unique
    @Nullable
    public List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> tyh$getPhaseBuilders(GunItem.FirePhase phase) {
        return ((MixinEffectLauncherDuck)this.effectLauncher).tyh$getPhaseBuilders(phase);
    }

    @Inject(method = {"getMaxServerShootingDistance"}, at = {@At("HEAD")}, remap = false, cancellable = true)
    private void mixinShootingDistance(ItemStack itemStack, boolean isAiming, ServerLevel level, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(300.0D);
    }

    @Inject(method = {"adjustInaccuracy"}, at = {@At("HEAD")}, remap = false, cancellable = true)
    private void mixinAccuracy(Player player, ItemStack itemStack, boolean isAiming, CallbackInfoReturnable<Double> cir) {
        double adjustedInaccuracy;
        double aimingDebuffed = this.inaccuracyAiming;
        if (aimingDebuffed == 0.0D)
            aimingDebuffed = this.inaccuracy / 9.0D;
        if (player.isFallFlying()) {
            adjustedInaccuracy = this.inaccuracySprinting;
        } else if (isAiming) {
            adjustedInaccuracy = aimingDebuffed;
        } else {
            adjustedInaccuracy = this.inaccuracy;
        }
        if (player.isAutoSpinAttack() || player.getVehicle() != null) {
            adjustedInaccuracy *= 0.5D;
        } else if (player.isCrouching()) {
            adjustedInaccuracy *= 0.8D;
        }
        if (player.level().getGameRules().getRule(AllGameRules.RULE_MOVE_AFFECTS_AIM).get()) {
            float walkStrength = (player.xxa - player.zza) / player.getSpeed();
            adjustedInaccuracy = Mth.lerp(walkStrength, adjustedInaccuracy, this.inaccuracySprinting);
        }
        Pair<Integer, Double> pcs = FireModeFeature.getPelletCountAndSpread((LivingEntity)player, null, itemStack);
        if (pcs.getFirst() > 0)
            adjustedInaccuracy += pcs.getSecond();
        float accuracyModifier = AccuracyFeature.getAccuracyModifier(itemStack);
        cir.setReturnValue(adjustedInaccuracy / accuracyModifier);
    }

    @Inject(method = {"hitScanTarget"}, at = {@At(value = "INVOKE", target = "Lcom/vicmatskiv/pointblank/item/GunItem;handleBlockHit(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/entity/Entity;)V")}, remap = false)
    private void mixinOnHandleBlockHit(Player player, ItemStack gunStack, int slotIndex, int correlationId, HitResult hitResult, double maxHitScanDistance, List<BlockPos> blockPosToDestroy, CallbackInfo ci) {
        BlockHitResult blockHitResult = (BlockHitResult)hitResult;
        double distanceToPlayer = player.position().subtract(blockHitResult.getBlockPos().getCenter()).length();
        double dmg = Math.max(calcDamage(gunStack, distanceToPlayer), 0.0D);
        player.level().playSound(null, blockHitResult.getBlockPos(), (SoundEvent)AllSounds.BULLET_IMPACT.get(), SoundSource.BLOCKS, (float)(dmg / 2.0D), 1.0F + (player
                .getRandom().nextFloat() - 0.5F) / 5.0F);
    }

    @WrapOperation(method = {"handleClientHitScanFireRequest"}, at = {@At(value = "INVOKE", target = "Lcom/vicmatskiv/pointblank/util/HitScan;getObjectsInCrosshair(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;FDIDJLjava/util/function/Predicate;Ljava/util/function/Predicate;Ljava/util/List;)Ljava/util/List;")}, remap = false)
    private List<HitResult> mixinOnScan(LivingEntity entity, Vec3 eyePos, Vec3 lookVec, float partialTicks, double maxHitScanDistance, int shotCount, double adjustedInaccuracy, long seed, Predicate<Block> isBreakableBlock, Predicate<Block> isPassThroughBlock, List<BlockPos> blockPosToBreakOutput, Operation<List<HitResult>> original, @Local(ordinal = 0) int slotIndex) {
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            ItemStack gunStack = serverPlayer.getInventory().getItem(slotIndex);
            Vec3 endVec = eyePos.add(lookVec.scale(maxHitScanDistance));
            Vector3d bp = VectorConversionsMCKt.toJOML(eyePos);
            Vector3d be = VectorConversionsMCKt.toJOML(endVec);
            for (Player p : serverPlayer.level().players()) {
                if (!p.equals(serverPlayer) &&
                    !p.isCreative() && (!p.isPassenger() || VSGameUtilsKt.getShipManaging(p.getRootVehicle()) == null) && lookVec
                    .dot(p.getEyePosition().subtract(eyePos)) >= 0.0D &&
                    !Objects.equals(serverPlayer.getTeam(), p.getTeam())) {
                    Vector3d po = VectorConversionsMCKt.toJOML(p.getEyePosition());
                    float passDistance = (float)p.getEyePosition().distanceTo(VectorConversionsMCKt.toMinecraft((Vector3dc)Intersectiond.findClosestPointOnLineSegment(bp.x, bp.y, bp.z, be.x, be.y, be.z, po.x, po.y, po.z, new Vector3d())));
                    if (passDistance < 7.0F) {
                        passDistance = Math.max(passDistance, 0.5F);
                        double distanceToPlayer = p.getEyePosition().distanceTo(eyePos);
                        double dmg = calcDamage(gunStack, distanceToPlayer);
                        ServerSuppressionTracker.suppress(p, (float)(dmg / passDistance));
                    }
                }
            }
        }
        return original.call(entity, eyePos, lookVec, partialTicks, maxHitScanDistance, shotCount, adjustedInaccuracy, seed, isBreakableBlock, isPassThroughBlock, blockPosToBreakOutput);
    }

    @Unique
    private double calcDamage(ItemStack gunStack, double distanceToPlayer) {
        return FireModeFeature.getDamage(gunStack) *
            Mth.clamp(1.0D - Math.pow(Math.sqrt(distanceToPlayer) / this.maxShootingDistance * 0.5D, 6.0D), 0.0D, 1.0D);
    }
}