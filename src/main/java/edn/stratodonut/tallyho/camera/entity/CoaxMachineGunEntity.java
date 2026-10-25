package edn.stratodonut.tallyho.camera.entity;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import edn.stratodonut.tallyho.AllEntities;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AutocannonBreechBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterialProperties;
import rbasamoyai.createbigcannons.effects.particles.plumes.AutocannonPlumeParticleData;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;
import rbasamoyai.createbigcannons.munitions.autocannon.AbstractAutocannonProjectile;
import rbasamoyai.createbigcannons.munitions.autocannon.bullet.MachineGunRoundItem;
import rbasamoyai.createbigcannons.utils.CBCUtils;

public class CoaxMachineGunEntity extends Entity implements IEntityAdditionalSpawnData {
   private float BASE_YAW;
   protected final MachineGunRoundItem MACHINE_GUN_ROUND = (MachineGunRoundItem)CBCItems.MACHINE_GUN_ROUND.get();
   protected final AutocannonMaterialProperties MACHINE_GUN_PROPERTIES = ((AutocannonBreechBlock)CBCBlocks.STEEL_AUTOCANNON_BREECH.get())
      .getAutocannonMaterial()
      .properties();
   protected final int BARREL_LENGTH = 10;
   private final int BELT_LENGTH = 500;
   private final int RELOAD_TIME_TICKS = 160;
   private final int FIRING_COOLDOWN_TICKS = 2;
   protected int muzzle_offset = 0;
   @Nonnull
   WeakReference<PitchOrientedContraptionEntity> coaxial_to = new WeakReference<>(null);
   int belt = 0;
   int reloadProgress = 0;
   int firingCooldown = 0;
   int timeout = 0;

   public CoaxMachineGunEntity(EntityType<?> p_19870_, Level p_19871_) {
      super(p_19870_, p_19871_);
   }

   public CoaxMachineGunEntity(Level p_19871_, int muzzle) {
      super((EntityType)AllEntities.COAX_MACHINE_GUN_ENTITY.get(), p_19871_);
      this.muzzle_offset = muzzle;
   }

   public static CoaxMachineGunEntity spawn(ServerLevel level, BlockPos pos, float base_yaw, int muzzle_offset) {
      CoaxMachineGunEntity result = new CoaxMachineGunEntity(level, muzzle_offset);
      result.BASE_YAW = base_yaw;
      result.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      return result;
   }

   public InteractionResult interact(Player p_19978_, InteractionHand p_19979_) {
      if (!p_19978_.level().isClientSide() && p_19978_.getItemInHand(p_19979_).getItem() instanceof WrenchItem) {
         this.discard();
         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.level() instanceof ServerLevel serverLevel) {
            if (this.belt == 0) {
               this.reloadProgress++;
               if (this.reloadProgress > 160) {
                  this.belt = 500;
                  this.reloadProgress = 0;
               }
            }

            if (this.coaxial_to.get() == null) {
               List<PitchOrientedContraptionEntity> cannons = serverLevel.getEntitiesOfClass(
                  PitchOrientedContraptionEntity.class, this.getBoundingBox().inflate(2.0)
               );
               if (cannons.isEmpty()) {
                  return;
               }

               this.coaxial_to = new WeakReference<>(cannons.get(0));
            }

            PitchOrientedContraptionEntity cannonEntity = this.coaxial_to.get();
            if (cannonEntity.isRemoved() || !cannonEntity.isAlive()) {
               this.coaxial_to = new WeakReference<>(null);
               return;
            }

            Ship s = VSGameUtilsKt.getShipMountedTo(this);
            Vec3 orient = Vec3.atLowerCornerOf(cannonEntity.getInitialOrientation().getNormal());
            Vec3 muzzle = cannonEntity.applyRotation(orient, 1.0F);
            this.lookAt(Anchor.EYES, this.getEyePosition().add(muzzle));
            BlockPos virtualPos = this.blockPosition();
            Vec3 lookVec = this.getLookAngle();
            Vec3 spawnPos = this.getEyePosition().add(lookVec.scale(this.muzzle_offset + 1));
            if (s != null && this.isPassenger()) {
               virtualPos = this.getVehicle().blockPosition();
            }

            if (this.firingCooldown > 0) {
               this.firingCooldown--;
            }

            if (this.firingCooldown <= 0 && this.level().isLoaded(virtualPos) && this.level().hasNeighborSignal(virtualPos)) {
               if (this.belt == 0) {
                  serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 0.0F);
               } else {
                  Vec3 vec1 = spawnPos.subtract(this.getEyePosition()).normalize();
                  Vec3 particlePos = spawnPos.subtract(vec1.scale(1.5));
                  AbstractAutocannonProjectile projectile = this.MACHINE_GUN_ROUND.getAutocannonProjectile(null, serverLevel);
                  float speed = this.MACHINE_GUN_PROPERTIES.baseSpeed() + this.MACHINE_GUN_PROPERTIES.speedIncreasePerBarrel() * 10.0F;
                  float spread = 0.717F;
                  if (projectile != null) {
                     projectile.setPos(spawnPos);
                     projectile.setChargePower(10.0F);
                     projectile.setTracer(true);
                     projectile.setLifetime(this.MACHINE_GUN_PROPERTIES.projectileLifetime() * 2);
                     projectile.shoot(vec1.x, vec1.y, vec1.z, speed, spread);
                     projectile.xRotO = projectile.getXRot();
                     projectile.yRotO = projectile.getYRot();
                     serverLevel.addFreshEntity(projectile);
                  }

                  Vec3 particleVel = vec1.scale(1.25);

                  for (ServerPlayer player : ((ServerLevel) this.level()).players()) {
                     serverLevel.sendParticles(
                        player,
                        new AutocannonPlumeParticleData(1.0F),
                        true,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        0,
                        particleVel.x,
                        particleVel.y,
                        particleVel.z,
                        1.0
                     );
                  }

                  CBCUtils.playBlastLikeSoundOnServer(
                     serverLevel, spawnPos.x, spawnPos.y, spawnPos.z, CBCSoundEvents.FIRE_MACHINE_GUN.getMainEvent(), SoundSource.BLOCKS, 10.0F, 0.75F, 3.0F
                  );
                  this.belt--;
                  this.firingCooldown = 2;
               }
            }
         }

         if (this.getVehicle() == null) {
            this.timeout++;
         }

         if (this.timeout > 100) {
            this.discard();
         }
      }
   }

   public float getBaseYaw() {
      return this.BASE_YAW;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag nbt) {
      this.belt = nbt.getInt("belt");
      this.reloadProgress = nbt.getInt("reload");
      this.muzzle_offset = nbt.getInt("muzzleOffset");
      this.BASE_YAW = nbt.getFloat("BASE_YAW");
   }

   protected void addAdditionalSaveData(CompoundTag nbt) {
      nbt.putInt("belt", this.belt);
      nbt.putInt("reload", this.reloadProgress);
      nbt.putInt("muzzleOffset", this.muzzle_offset);
      nbt.putFloat("BASE_YAW", this.getBaseYaw());
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public void writeSpawnData(FriendlyByteBuf buffer) {
   }

   public void readSpawnData(FriendlyByteBuf additionalData) {
   }
}
