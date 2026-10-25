package edn.stratodonut.tallyho.camera.entity;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.IClientKeyConsumer;
import edn.stratodonut.tallyho.KeyBinds;
import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.MultiOpticType;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.StabiliserSyncPacket;
import edn.stratodonut.tallyho.network.StabiliserSyncPacketClient;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AutocannonBreechBlock;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterialProperties;
import rbasamoyai.createbigcannons.effects.particles.plumes.AutocannonPlumeParticleData;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;
import rbasamoyai.createbigcannons.munitions.autocannon.AbstractAutocannonProjectile;
import rbasamoyai.createbigcannons.munitions.autocannon.bullet.MachineGunRoundItem;
import rbasamoyai.createbigcannons.utils.CBCUtils;

public class RemoteStationEntity extends CameraEntity2 implements ITurret, IStabilisedTurret, IClientKeyConsumer {
   protected static final MachineGunRoundItem MACHINE_GUN_ROUND = (MachineGunRoundItem)CBCItems.MACHINE_GUN_ROUND.get();
   protected static final AutocannonMaterialProperties MACHINE_GUN_PROPERTIES = ((AutocannonBreechBlock)CBCBlocks.STEEL_AUTOCANNON_BREECH.get())
      .getAutocannonMaterial()
      .properties();
   protected static final int BARREL_LENGTH = 10;
   private static final int BELT_LENGTH = 400;
   private static final int RELOAD_TIME_TICKS = 160;
   private static final int FIRING_COOLDOWN_TICKS = 2;
   @Nullable
   private Vec3 stab_vector;
   MultiOpticType opticMode;
   int belt = 0;
   int reloadProgress = 0;
   int firingCooldown = 0;
   int timeout = 0;

   public RemoteStationEntity(EntityType<?> p_19870_, Level p_19871_) {
      super((EntityType<? extends Entity>)p_19870_, p_19871_);
      this.opticMode = new MultiOpticType(CameraEntity2.PostFXType.VISIBLE, CameraEntity2.PostFXType.THERMALS_WHITE);
   }

   public RemoteStationEntity(Level p_19871_) {
      this((EntityType<?>)AllEntities.CROWS_ENTITY.get(), p_19871_);
   }

   public static RemoteStationEntity spawn(ServerLevel level, Vec3 pos, float base_yaw) {
      RemoteStationEntity result = new RemoteStationEntity(level);
      result.setParams(base_yaw, CameraEntity2.PostFXType.VISIBLE, new AngleLimits(20.0F, 60.0F, 360.0F), -1);
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      result.belt = 400;
      return result;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         Vec2 rot = this.getStabView(0.0F);
         this.setRot(rot.y, rot.x);
      }

      if (!this.level().isClientSide) {
         if (this.belt == 0) {
            if (this.reloadProgress > 160) {
               this.belt = 400;
               this.reloadProgress = 0;
               AllSoundEvents.PECULIAR_BELL_USE.play(this.level(), null, this.getX(), this.getY(), this.getZ(), 0.5F, 1.1F);
            }
         } else {
            this.reloadProgress = 0;
         }

         if (this.stab_vector == null) {
            this.setStabVector(this.getLookAngle());
         } else if (this.tickCount % 20 == 0) {
            CreateTypePacketHandler.getChannel()
               .send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new StabiliserSyncPacketClient(this, this.stab_vector));
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
         Vec3 spawnPos = this.getEyePosition().add(0.0, 0.375, 0.0).add(lookVec.scale(1.0));
         if (s != null && this.isPassenger()) {
            virtualPos = this.getVehicle().blockPosition();
         }

         if (this.firingCooldown <= 0 && this.level().isLoaded(virtualPos)) {
            if (this.belt == 0) {
               serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 0.0F);
            } else {
               AbstractAutocannonProjectile projectile = MACHINE_GUN_ROUND.getAutocannonProjectile(null, serverLevel);
               float speed = MACHINE_GUN_PROPERTIES.baseSpeed() + MACHINE_GUN_PROPERTIES.speedIncreasePerBarrel() * 10.0F;
               float spread = 0.717F;
               if (projectile != null) {
                  projectile.setPos(spawnPos);
                  projectile.setChargePower(10.0F);
                  projectile.setTracer(true);
                  projectile.setLifetime(MACHINE_GUN_PROPERTIES.projectileLifetime() * 2);
                  projectile.shoot(lookVec.x, lookVec.y, lookVec.z, speed, spread);
                  projectile.xRotO = projectile.getXRot();
                  projectile.yRotO = projectile.getYRot();
                  serverLevel.addFreshEntity(projectile);
               }

               Vec3 particleVel = lookVec.scale(1.25);

               for (Player value : this.level().players()) {
                  ServerPlayer player = (ServerPlayer)value;
                  serverLevel.sendParticles(
                     player,
                     new AutocannonPlumeParticleData(0.3F),
                     true,
                     spawnPos.x,
                     spawnPos.y,
                     spawnPos.z,
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
   }

   public InteractionResult interact(Player p_19978_, InteractionHand p_19979_) {
      if (!p_19978_.level().isClientSide()) {
         if (p_19978_.getItemInHand(p_19979_).getItem() instanceof WrenchItem) {
            this.discard();
         } else if (this.belt == 0) {
            Vector3f localUp = Direction.UP.step();
            Ship s = VSGameUtilsKt.getShipMountedTo(this);
            if (s != null) {
               localUp = s.getShipToWorld().transformDirection(localUp);
            }

            if (VectorConversionsMCKt.toJOML(p_19978_.getEyePosition().subtract(this.getEyePosition())).dot(localUp.get(new Vector3d())) > 0.0) {
               this.reloadProgress++;
               this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 0.0F);
               p_19978_.displayClientMessage(Components.literal(String.format("Reloading: %d%%", this.reloadProgress * 100 / 160)), true);
            } else {
               p_19978_.displayClientMessage(Components.literal("Cannot reload the turret from here!"), true);
            }
         }

         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   @Nonnull
   protected Vec2 getStabView(float partial_tick) {
      Ship s = VSGameUtilsKt.getShipMountedTo(this);
      if (s != null) {
         if (this.stab_vector == null) {
            this.setStabVector(this.getLookAngle());
         }

         return this.getLimitedRotationFromDirection(this.stab_vector);
      } else {
         return this.getRotationVector();
      }
   }

   protected void setStabVector(@Nonnull Vec3 v) {
      this.stab_vector = v;
      if (this.level().isClientSide) {
         CreateTypePacketHandler.getChannel().sendToServer(new StabiliserSyncPacket(this, v));
      }
   }

   @Override
   public boolean isStab() {
      return true;
   }

   @Override
   public void syncStab(Vec3 v) {
      if (!this.isPossessedAndClient()) {
         this.stab_vector = v;
      }
   }

   @Override
   public void onKeyPressed(byte keyMap) {
      if (KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.LEFT) || KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.RIGHT)) {
         this.opticMode.nextType();
         Minecraft.getInstance().gameRenderer.checkEntityPostEffect(this);
      }
   }

   public float getViewXRot(float p_20268_) {
      if (this.level().isClientSide) {
         Vec2 rot = this.getStabView(p_20268_);
         if (rot != null) {
            return rot.x;
         }
      }

      return super.getViewXRot(p_20268_);
   }

   public float getViewYRot(float p_20279_) {
      if (this.level().isClientSide) {
         Vec2 rot = this.getStabView(p_20279_);
         if (rot != null) {
            return rot.y;
         }
      }

      return super.getViewYRot(p_20279_);
   }

   @Override
   public void turnView(double yRot, double xRot) {
      super.turnView(yRot * 0.5, xRot * 0.5);
      if (yRot != 0.0 || xRot != 0.0) {
         this.setStabVector(this.getLookAngle());
      }
   }

   @NotNull
   @Override
   public CameraEntity2.PostFXType getOverlayType() {
      return this.opticMode.getCurrentOptic();
   }

   @Override
   protected boolean isShortLived() {
      return false;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   @Override
   protected float getEyeHeight(@NotNull Pose p, EntityDimensions dim) {
      return dim.height * 0.5F;
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag nbt) {
      super.readAdditionalSaveData(nbt);
      this.belt = nbt.getInt("belt");
      this.reloadProgress = nbt.getInt("reload");
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag nbt) {
      super.addAdditionalSaveData(nbt);
      nbt.putInt("belt", this.belt);
      nbt.putInt("reload", this.reloadProgress);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }
}
