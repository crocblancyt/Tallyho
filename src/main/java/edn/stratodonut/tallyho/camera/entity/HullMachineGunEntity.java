package edn.stratodonut.tallyho.camera.entity;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AutocannonBreechBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterialProperties;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;
import rbasamoyai.createbigcannons.munitions.autocannon.AbstractAutocannonProjectile;
import rbasamoyai.createbigcannons.munitions.autocannon.bullet.MachineGunRoundItem;
import rbasamoyai.createbigcannons.utils.CBCUtils;

public class HullMachineGunEntity extends CameraEntity2 implements ITurret {
   protected final MachineGunRoundItem MACHINE_GUN_ROUND = (MachineGunRoundItem)CBCItems.MACHINE_GUN_ROUND.get();
   protected final AutocannonMaterialProperties MACHINE_GUN_PROPERTIES = ((AutocannonBreechBlock)CBCBlocks.STEEL_AUTOCANNON_BREECH.get())
      .getAutocannonMaterial()
      .properties();
   protected final int BARREL_LENGTH = 10;
   private final int BELT_LENGTH = 500;
   private final int RELOAD_TIME_TICKS = 160;
   private final int FIRING_COOLDOWN_TICKS = 2;
   protected int muzzle_offset = 0;
   int belt = 0;
   int reloadProgress = 0;
   int firingCooldown = 0;
   int timeout = 0;

   public HullMachineGunEntity(EntityType<?> p_19870_, Level p_19871_) {
      super((EntityType<? extends Entity>)p_19870_, p_19871_);
   }

   public HullMachineGunEntity(Level p_19871_, int muzzle) {
      super((EntityType<? extends Entity>)AllEntities.HULL_MACHINE_GUN_ENTITY.get(), p_19871_);
      this.muzzle_offset = muzzle;
   }

   public static HullMachineGunEntity spawn(ServerLevel level, BlockPos pos, float base_yaw, int muzzle_offset) {
      HullMachineGunEntity result = new HullMachineGunEntity(level, muzzle_offset);
      result.setParams(base_yaw, CameraEntity2.PostFXType.SCOPE, new AngleLimits(45.0F, 45.0F, 45.0F), 50);
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      result.setYRot(base_yaw);
      return result;
   }

   public InteractionResult interact(Player player, InteractionHand p_19979_) {
      if (CameraPossessionController.isPossessingConductor(player)) {
         return InteractionResult.FAIL;
      }

      if (!player.level().isClientSide()) {
         if (player.getItemInHand(p_19979_).getItem() instanceof WrenchItem) {
            this.discard();
            return InteractionResult.CONSUME;
         }

         this.startViewing((ServerPlayer)player);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   protected boolean isShortLived() {
      return false;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.belt == 0) {
            this.reloadProgress++;
            if (this.reloadProgress > 160) {
               this.belt = 500;
               this.reloadProgress = 0;
            }
         }

         if (this.firingCooldown > 0) {
            this.firingCooldown--;
         }
      }
   }

   @Override
   public final void handleShoot(LivingEntity shooter) {
      if (!this.level().isClientSide) {
         ServerLevel serverLevel = (ServerLevel)this.level();
         Ship s = VSGameUtilsKt.getShipManaging(this.getVehicle() != null ? this.getVehicle() : this);
         BlockPos virtualPos = this.blockPosition();
         Vec3 lookVec = this.getLookAngle();
         Vec3 spawnPos = this.position().add(lookVec.scale(this.muzzle_offset));
         if (s != null) {
            virtualPos = BlockPos.containing(
               VectorConversionsMCKt.toMinecraft(s.getWorldToShip().transformPosition(VectorConversionsMCKt.toJOML(this.position())))
            );
         }

         if (this.firingCooldown <= 0 && this.level().isLoaded(virtualPos)) {
            if (this.belt == 0) {
               serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 0.0F);
            } else {
               Vec3 vec1 = spawnPos.subtract(this.position()).normalize();
               Vec3 particlePos = spawnPos.subtract(vec1.scale(1.5));
               AbstractAutocannonProjectile projectile = this.MACHINE_GUN_ROUND.getAutocannonProjectile(null, serverLevel);
               float speed = this.MACHINE_GUN_PROPERTIES.baseSpeed() + this.MACHINE_GUN_PROPERTIES.speedIncreasePerBarrel() * 10.0F;
               float spread = 0.1F;
               if (projectile != null) {
                  projectile.setPos(spawnPos);
                  projectile.setChargePower(10.0F);
                  projectile.setTracer(false);
                  projectile.setLifetime(this.MACHINE_GUN_PROPERTIES.projectileLifetime());
                  projectile.shoot(vec1.x, vec1.y, vec1.z, speed, spread);
                  projectile.xRotO = projectile.getXRot();
                  projectile.yRotO = projectile.getYRot();
                  serverLevel.addFreshEntity(projectile);
               }

               CBCUtils.playBlastLikeSoundOnServer(
                  serverLevel, spawnPos.x, spawnPos.y, spawnPos.z, CBCSoundEvents.FIRE_AUTOCANNON.getMainEvent(), SoundSource.BLOCKS, 12.0F, 1.0F, 5.0F
               );
               this.belt--;
               this.firingCooldown = 2;
            }
         }
      }
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag nbt) {
      super.readAdditionalSaveData(nbt);
      this.belt = nbt.getInt("belt");
      this.reloadProgress = nbt.getInt("reload");
      this.muzzle_offset = nbt.getInt("muzzleOffset");
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag nbt) {
      super.addAdditionalSaveData(nbt);
      nbt.putInt("belt", this.belt);
      nbt.putInt("reload", this.reloadProgress);
      nbt.putInt("muzzleOffset", this.muzzle_offset);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }
}
