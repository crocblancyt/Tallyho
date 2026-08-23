package edn.stratodonut.tallyho.missile.warhead;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
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
import rbasamoyai.createbigcannons.network.RootPacket;
import rbasamoyai.createbigcannons.remix.CustomExplosion;

public class HighExplosion extends CustomExplosion.Impl {
    private final Set<BlockPos> changedBlocks;
    private final boolean isPlume;

    public HighExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator calc, double toBlowX, double toBlowY, double toBlowZ, float radius, boolean fire, Level.ExplosionInteraction interaction) {
        super(level, source, damageSource, calc, toBlowX, toBlowY, toBlowZ, radius, fire, interaction);
        this.changedBlocks = new HashSet<>();
        BlockPos pos = new BlockPos((int)toBlowX, (int)toBlowY, (int)toBlowZ);
        this.isPlume = (level.getBlockState(pos.above()).isAir() && !level.getBlockState(pos.below()).isAir());
    }

    public HighExplosion(Level level, ClientboundCBCExplodePacket packet) {
        super(level, packet);
        this.changedBlocks = new HashSet<>();
        BlockPos pos = new BlockPos((int)this.x, (int)this.y, (int)this.z);
        this.isPlume = (this.level.getBlockState(pos.above()).isAir() && !this.level.getBlockState(pos.below()).isAir());
    }

    protected void spawnParticles() {
        ShellBlastWaveEffectParticleData blastWave = new ShellBlastWaveEffectParticleData((this.size * 12.0F), (Holder<SoundEvent>) BuiltInRegistries.SOUND_EVENT.getKey(CBCSoundEvents.SHELL_EXPLOSION.getMainEvent()), SoundSource.BLOCKS, Math.max(this.size * 2.0F, 16.0F), 0.8F + this.level.random.nextFloat() * 0.4F, 2.0F, this.size);
        ShellExplosionCloudParticleData explosionCloud = new ShellExplosionCloudParticleData(this.size, this.isPlume);
        this.level.addParticle(blastWave, true, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
        this.level.addParticle(explosionCloud, true, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
    }

    public void editBlock(Level level, BlockPos pos, BlockState blockState, FluidState fluidState, float power) {
        if (CBCConfigs.SERVER.munitions.projectilesChangeSurroundings.get() && !this.changedBlocks.contains(pos)) {
            BlockState transformed = BlockImpactTransformationHandler.transformBlock(blockState);
            level.setBlock(pos, transformed, 11);
            this.changedBlocks.add(pos);
        }
    }

    public void sendExplosionToClient(ServerPlayer player) {
        double distSqr = player.distanceToSqr(this.x, this.y, this.z);
        if (distSqr < 263000.0D) {
            Vec3 knockback = (Vec3)getHitPlayers().getOrDefault(player, Vec3.ZERO);

            NetworkPlatform.sendToClientPlayer((RootPacket)new ClientboundCBCExplodePacket(this.x, this.y, this.z, this.size,
                    getToBlow(), (float)knockback.x, (float)knockback.y, (float)knockback.z, ClientboundCBCExplodePacket.ExplosionType.SHELL), player);
        }
    }
}