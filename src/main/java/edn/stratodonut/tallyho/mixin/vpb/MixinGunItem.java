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
import com.vicmatskiv.pointblank.item.GunItem.FirePhase;
import com.vicmatskiv.pointblank.item.GunItem.PhasedReload;
import com.vicmatskiv.pointblank.item.HurtingItem.Builder;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Intersectiond;
import org.joml.Vector3d;
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
@Mixin(GunItem.class)
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
   private List<PhasedReload> phasedReloads;
   @Shadow
   private double shakeDecay;
   @Shadow
   private int shotsPerTrace;

   @Shadow
   protected abstract double adjustInaccuracy(Player var1, ItemStack var2, boolean var3);

   @Shadow
   protected abstract AmmoItem getFirstCompatibleProjectile(ItemStack var1, FireModeInstance var2);

   @Shadow
   protected abstract double getMaxServerShootingDistance(ItemStack var1, boolean var2, ServerLevel var3);

   @Shadow
   protected abstract void hitScanTarget(Player var1, ItemStack var2, int var3, int var4, HitResult var5, double var6, List<BlockPos> var8);

   @Shadow
   private static long getOrAssignRandomSeed(ItemStack stack) {
      return 0L;
   }

   public MixinGunItem(Properties properties, Builder<?> builder) {
      super(properties, builder);
   }

   @Override
   public double tyh$AdjustInaccuracy(Player player, ItemStack itemStack, boolean isAiming) {
      return this.adjustInaccuracy(player, itemStack, isAiming);
   }

   @Override
   public AmmoItem tyh$getFirstCompatibleProjectile(ItemStack gunStack, FireModeInstance fireModeInstance) {
      return this.getFirstCompatibleProjectile(gunStack, fireModeInstance);
   }

   @Override
   public double tyh$getMaxServerShootingDistance(ItemStack itemStack, boolean isAiming, ServerLevel level) {
      return this.getMaxServerShootingDistance(itemStack, isAiming, level);
   }

   @Override
   public void tyh$hitScanTarget(
      Player player, ItemStack itemStack, int slotIndex, int correlationId, HitResult hitResult, double maxHitScanDistance, List<BlockPos> blockPosToDestroy
   ) {
      this.hitScanTarget(player, itemStack, slotIndex, correlationId, hitResult, maxHitScanDistance, blockPosToDestroy);
   }

   @Override
   public long tyh$getOrAssignRandomSeed(ItemStack stack) {
      return getOrAssignRandomSeed(stack);
   }

   @Unique
   @Nullable
   @Override
   public List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> tyh$getPhaseBuilders(FirePhase phase) {
      return ((MixinEffectLauncherDuck)this.effectLauncher).tyh$getPhaseBuilders(phase);
   }

   @Inject(method = "getMaxServerShootingDistance", at = @At("HEAD"), remap = false, cancellable = true)
   private void mixinShootingDistance(ItemStack itemStack, boolean isAiming, ServerLevel level, CallbackInfoReturnable<Double> cir) {
      cir.setReturnValue(300.0);
   }

   @Inject(method = "adjustInaccuracy", at = @At("HEAD"), remap = false, cancellable = true)
   private void mixinAccuracy(Player player, ItemStack itemStack, boolean isAiming, CallbackInfoReturnable<Double> cir) {
      double aimingDebuffed = this.inaccuracyAiming;
      if (aimingDebuffed == 0.0) {
         aimingDebuffed = this.inaccuracy / 9.0;
      }

      double adjustedInaccuracy;
      if (player.isSprinting()) {
         adjustedInaccuracy = this.inaccuracySprinting;
      } else if (isAiming) {
         adjustedInaccuracy = aimingDebuffed;
      } else {
         adjustedInaccuracy = this.inaccuracy;
      }

      if (player.isVisuallyCrawling() || player.getVehicle() != null) {
         adjustedInaccuracy *= 0.5;
      } else if (player.isCrouching()) {
         adjustedInaccuracy *= 0.8;
      }

      if (((BooleanValue)player.level().getGameRules().getRule(AllGameRules.RULE_MOVE_AFFECTS_AIM)).get()) {
         float walkStrength = (player.walkDist - player.walkDistO) / player.getSpeed();
         adjustedInaccuracy = Mth.clampedLerp(adjustedInaccuracy, this.inaccuracySprinting, walkStrength);
      }

      Pair<Integer, Double> pcs = FireModeFeature.getPelletCountAndSpread(player, null, itemStack);
      if ((Integer)pcs.getFirst() > 0) {
         adjustedInaccuracy += pcs.getSecond();
      }

      float accuracyModifier = AccuracyFeature.getAccuracyModifier(itemStack);
      cir.setReturnValue(adjustedInaccuracy / accuracyModifier);
   }

   @Inject(
      method = "hitScanTarget",
      at = @At(
         value = "INVOKE",
         target = "Lcom/vicmatskiv/pointblank/item/GunItem;handleBlockHit(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/entity/Entity;)V"
      ),
      remap = false
   )
   private void mixinOnHandleBlockHit(
      Player player,
      ItemStack gunStack,
      int slotIndex,
      int correlationId,
      HitResult hitResult,
      double maxHitScanDistance,
      List<BlockPos> blockPosToDestroy,
      CallbackInfo ci
   ) {
      BlockHitResult blockHitResult = (BlockHitResult)hitResult;
      double distanceToPlayer = player.position().subtract(blockHitResult.getBlockPos().getCenter()).length();
      double dmg = Math.max(this.tyh$calcDamage(gunStack, distanceToPlayer), 0.0);
      player.level()
         .playSound(
            null,
            blockHitResult.getBlockPos(),
            (SoundEvent)AllSounds.BULLET_IMPACT.get(),
            SoundSource.BLOCKS,
            (float)(dmg / 2.0),
            1.0F + (player.getRandom().nextFloat() - 0.5F) / 5.0F
         );
   }

   @WrapOperation(
      method = "handleClientHitScanFireRequest",
      at = @At(
         value = "INVOKE",
         target = "Lcom/vicmatskiv/pointblank/util/HitScan;getObjectsInCrosshair(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;FDIDJLjava/util/function/Predicate;Ljava/util/function/Predicate;Ljava/util/List;)Ljava/util/List;"
      ),
      remap = false
   )
   private List<HitResult> mixinOnScan(
      LivingEntity entity,
      Vec3 eyePos,
      Vec3 lookVec,
      float partialTicks,
      double maxHitScanDistance,
      int shotCount,
      double adjustedInaccuracy,
      long seed,
      Predicate<Block> isBreakableBlock,
      Predicate<Block> isPassThroughBlock,
      List<BlockPos> blockPosToBreakOutput,
      Operation<List<HitResult>> original,
      @Local(ordinal = 0) int slotIndex
   ) {
      if (entity instanceof ServerPlayer serverPlayer) {
         ItemStack gunStack = serverPlayer.getInventory().getItem(slotIndex);
         Vec3 endVec = eyePos.add(lookVec.scale(maxHitScanDistance));
         Vector3d bp = VectorConversionsMCKt.toJOML(eyePos);
         Vector3d be = VectorConversionsMCKt.toJOML(endVec);

         for (Player p : serverPlayer.level().players()) {
            if (!p.equals(serverPlayer)
               && !p.isCreative()
               && (!p.isPassenger() || VSGameUtilsKt.getShipManaging(p.getRootVehicle()) == null)
               && lookVec.dot(p.getEyePosition().subtract(eyePos)) >= 0.0
               && !Objects.equals(serverPlayer.getTeam(), p.getTeam())) {
               Vector3d po = VectorConversionsMCKt.toJOML(p.getEyePosition());
               float passDistance = (float)p.getEyePosition()
                  .subtract(
                     VectorConversionsMCKt.toMinecraft(
                        Intersectiond.findClosestPointOnLineSegment(bp.x, bp.y, bp.z, be.x, be.y, be.z, po.x, po.y, po.z, new Vector3d())
                     )
                  )
                  .length();
               if (passDistance < 7.0F) {
                  passDistance = Math.max(passDistance, 0.5F);
                  double distanceToPlayer = p.getEyePosition().subtract(eyePos).length();
                  double dmg = this.tyh$calcDamage(gunStack, distanceToPlayer);
                  ServerSuppressionTracker.suppress(p, (float)(dmg / passDistance));
               }
            }
         }
      }

      return (List<HitResult>)original.call(
         new Object[]{
            entity, eyePos, lookVec, 0.0F, maxHitScanDistance, shotCount, adjustedInaccuracy, seed, isBreakableBlock, isPassThroughBlock, blockPosToBreakOutput
         }
      );
   }

   @Unique
   private double tyh$calcDamage(ItemStack gunStack, double distanceToPlayer) {
      return FireModeFeature.getDamage(gunStack) * Mth.clamp(1.0 - Math.pow(Math.sqrt(distanceToPlayer) / this.maxShootingDistance * 0.5, 6.0), 0.0, 1.0);
   }
}
