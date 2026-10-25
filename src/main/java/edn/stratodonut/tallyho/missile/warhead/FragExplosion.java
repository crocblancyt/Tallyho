package edn.stratodonut.tallyho.missile.warhead;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.block_hit_effects.BlockImpactTransformationHandler;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.effects.particles.explosions.ShellBlastWaveEffectParticleData;
import rbasamoyai.createbigcannons.effects.particles.explosions.ShellExplosionCloudParticleData;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;
import rbasamoyai.createbigcannons.multiloader.NetworkPlatform;
import rbasamoyai.createbigcannons.network.ClientboundCBCExplodePacket;
import rbasamoyai.createbigcannons.network.ClientboundCBCExplodePacket.ExplosionType;
import rbasamoyai.createbigcannons.remix.CustomExplosion.Impl;

public class FragExplosion extends Impl {
   float fragPercent = 0.0F;
   private final Set<BlockPos> changedBlocks = new HashSet<>();
   private final boolean isPlume;

   public FragExplosion(
      Level level,
      @Nullable Entity source,
      @Nullable DamageSource damageSource,
      double toBlowX,
      double toBlowY,
      double toBlowZ,
      float fragPercent,
      float radius,
      boolean fire,
      ExplosionInteraction interaction
   ) {
      super(level, source, damageSource, new FragExplosion.FragmentationCalculator(fragPercent), toBlowX, toBlowY, toBlowZ, radius, fire, interaction);
      BlockPos pos = new BlockPos((int)toBlowX, (int)toBlowY, (int)toBlowZ);
      this.isPlume = level.getBlockState(pos.above()).isAir() && !level.getBlockState(pos.below()).isAir();
   }

   public FragExplosion(Level level, ClientboundCBCExplodePacket packet) {
      super(level, packet);
      BlockPos pos = new BlockPos((int)this.x, (int)this.y, (int)this.z);
      this.isPlume = this.level.getBlockState(pos.above()).isAir() && !this.level.getBlockState(pos.below()).isAir();
   }

   public static float effectiveBlockDamagePower(float power, float fragPercent) {
      return (float)(power * Math.pow(1.0F - fragPercent, 2.0));
   }

   protected void spawnParticles() {
      ShellBlastWaveEffectParticleData blastWave = new ShellBlastWaveEffectParticleData(
         this.size * 12.0F,
         BuiltInRegistries.SOUND_EVENT.wrapAsHolder(CBCSoundEvents.SHELL_EXPLOSION.getMainEvent()),
         SoundSource.BLOCKS,
         Math.max(this.size * 2.0F, 16.0F),
         0.8F + this.level.random.nextFloat() * 0.4F,
         2.0F,
         this.size
      );
      ShellExplosionCloudParticleData explosionCloud = new ShellExplosionCloudParticleData(this.size, this.isPlume);
      this.level.addParticle(blastWave, true, this.x, this.y, this.z, 0.0, 0.0, 0.0);
      this.level.addParticle(explosionCloud, true, this.x, this.y, this.z, 0.0, 0.0, 0.0);
   }

   public void editBlock(Level level, BlockPos pos, BlockState blockState, FluidState fluidState, float power) {
      if ((Boolean)CBCConfigs.SERVER.munitions.projectilesChangeSurroundings.get() && !this.changedBlocks.contains(pos)) {
         BlockState transformed = BlockImpactTransformationHandler.transformBlock(blockState);
         level.setBlock(pos, transformed, 11);
         this.changedBlocks.add(pos);
      }
   }

   public void sendExplosionToClient(ServerPlayer player) {
      if (player.distanceToSqr(this.x, this.y, this.z) < 263000.0) {
         Vec3 knockback = this.getHitPlayers().getOrDefault(player, Vec3.ZERO);
         NetworkPlatform.sendToClientPlayer(
            new ClientboundCBCExplodePacket(
               this.x,
               this.y,
               this.z,
               effectiveBlockDamagePower(this.size, this.fragPercent),
               this.getToBlow(),
               (float)knockback.x,
               (float)knockback.y,
               (float)knockback.z,
               ExplosionType.SHELL
            ),
            player
         );
         NetworkPlatform.sendToClientPlayer(
            new ClientboundCBCExplodePacket(
               this.x, this.y, this.z, this.size, this.getToBlow(), (float)knockback.x, (float)knockback.y, (float)knockback.z, ExplosionType.FLAK
            ),
            player
         );
      }
   }

   public static class FragmentationCalculator extends ExplosionDamageCalculator {
      private final float fragPercent;

      public FragmentationCalculator(float fragPercent) {
         this.fragPercent = (float)org.joml.Math.clamp(0.0, 0.99, fragPercent);
      }

      @Nonnull
      public Optional<Float> getBlockExplosionResistance(Explosion p_46099_, BlockGetter p_46100_, BlockPos p_46101_, BlockState p_46102_, FluidState p_46103_) {
         Optional<Float> orig = super.getBlockExplosionResistance(p_46099_, p_46100_, p_46101_, p_46102_, p_46103_);
         return orig.map(f -> (float)(f.floatValue() * Math.pow(1.0F / (1.0F - this.fragPercent), 2.0)));
      }
   }
}
