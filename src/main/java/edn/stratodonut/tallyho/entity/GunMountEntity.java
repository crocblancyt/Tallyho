package edn.stratodonut.tallyho.entity;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.entity.SlowProjectile;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.feature.SoundFeature;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.HitScan;
import com.vicmatskiv.pointblank.util.MiscUtil;
import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.AllItems;
import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.FlexibleSeatEntity;
import edn.stratodonut.tallyho.mixinducks.MixinGunItemDuck;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.GunMountAmmoSyncPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.ShipMountedToData;
import org.valkyrienskies.mod.common.entity.ShipMountedToDataProvider;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class GunMountEntity extends Entity implements IEntityAdditionalSpawnData, ShipMountedToDataProvider {
   private static final EntityDataAccessor<ItemStack> DATA_ID_GUN_ITEM = SynchedEntityData.defineId(GunMountEntity.class, EntityDataSerializers.ITEM_STACK);
   private float BASE_YAW;
   private Vec2 ANGLE_LIMIT = new Vec2(90.0F, 181.0F);
   private static final Random random = new Random();
   public static final Predicate<LivingEntity> HOSTILE_ENTITIES = entity -> entity.getSoundSource() == SoundSource.HOSTILE
      && !(entity instanceof NeutralMob)
      && !entity.getType().getCategory().isFriendly();

   public GunMountEntity(EntityType<?> p_19870_, Level p_19871_) {
      super(p_19870_, p_19871_);
   }

   public GunMountEntity(Level p_19871_) {
      super((EntityType)AllEntities.GUN_MOUNT_ENTITY.get(), p_19871_);
   }

   public static GunMountEntity spawn(ServerLevel level, Vec3 pos, float base_yaw) {
      GunMountEntity result = (GunMountEntity)AllEntities.GUN_MOUNT_ENTITY.create(level);
      result.setPos(pos);
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      return result;
   }

   public ItemStack asItem() {
      return new ItemStack((ItemLike)AllItems.GUN_MOUNT_ITEM.get());
   }

   protected void spawnPost(float base_yaw, Vec2 angle_limit) {
      this.BASE_YAW = base_yaw;
      this.ANGLE_LIMIT = angle_limit;
   }

   public double getPassengersRidingOffset() {
      return -0.8 + Math.sin(Math.toRadians(this.getXRot())) * 0.5;
   }

   public ItemStack getMountedGun() {
      return (ItemStack)this.entityData.get(DATA_ID_GUN_ITEM);
   }

   public void setMountedGun(ItemStack stack) {
      this.entityData.set(DATA_ID_GUN_ITEM, stack);
   }

   public Vec3 getMuzzlePosition() {
      return this.getEyePosition();
   }

   @NotNull
   public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand p_19979_) {
      if (CameraPossessionController.isPossessingConductor(player)) {
         return InteractionResult.FAIL;
      }

      if (!player.level().isClientSide()) {
         ItemStack heldStack = player.getItemInHand(p_19979_);
         ItemStack gunStack = this.getMountedGun();
         if (heldStack.getItem() instanceof WrenchItem) {
            this.spawnAtLocation(gunStack);
            this.spawnAtLocation(this.asItem());
            this.discard();
            return InteractionResult.CONSUME;
         }

         if (gunStack.isEmpty()) {
            int selected = player.getInventory().selected;
            heldStack = player.getInventory().getItem(selected + 1);
            if (heldStack.getItem() instanceof GunItem) {
               this.setMountedGun(heldStack.copy());
               heldStack.shrink(1);
               return InteractionResult.CONSUME;
            }
         } else if (player.isShiftKeyDown()) {
         }

         player.startRiding(this);
         FireModeInstance fireModeInstance = GunItem.getFireModeInstance(gunStack);
         if (player instanceof ServerPlayer p) {
            CreateTypePacketHandler.getChannel()
               .send(PacketDistributor.PLAYER.with(() -> p), new GunMountAmmoSyncPacket(this.getId(), GunItem.getAmmo(gunStack, fireModeInstance)));
         }
      }

      return InteractionResult.SUCCESS;
   }

   public void tick() {
      super.tick();
      Function<Vec3, Vec3> transform = x -> x;
      if (this.getVehicle() != null) {
         Ship s = VSGameUtilsKt.getShipManaging(this.getVehicle());
         if (s != null) {
            transform = x -> VectorConversionsMCKt.toMinecraft(s.getWorldToShip().transformDirection(VectorConversionsMCKt.toJOML(x)));
         }
      }

      if (this.getFirstPassenger() instanceof Player player) {
         Vec3 look = transform.apply(player.getLookAngle());
         double d0 = look.x;
         double d1 = look.y;
         double d2 = look.z;
         double d3 = Math.sqrt(d0 * d0 + d2 * d2);
         float rX = Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * 180.0F / (float)Math.PI)));
         float rY = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * 180.0F / (float)Math.PI) - 90.0F);
         this.setXRot(rX);
         if (this.ANGLE_LIMIT.y >= 180.0F) {
            this.setYRot(rY);
         } else {
            this.setYRot(Mth.rotateIfNecessary(rY, this.BASE_YAW, this.ANGLE_LIMIT.y));
         }

         this.setYHeadRot(this.getYRot());
         this.xRotO = this.getXRot();
         this.yRotO = this.getYRot();
      }
   }

   public void handleShoot(Player player, long sharedSecret) {
      Level world = player.level();
      ItemStack gunStack = this.getMountedGun();
      if (gunStack.getItem() instanceof GunItem gun) {
         MixinGunItemDuck gunDuck = (MixinGunItemDuck)gun;
         FireModeInstance fireModeInstance = GunItem.getFireModeInstance(gunStack);
         AmmoItem projectileItem = gunDuck.tyh$getFirstCompatibleProjectile(gunStack, GunItem.getFireModeInstance(gunStack));
         if (projectileItem != null) {
            this.shootProjectile(player, gunStack, fireModeInstance, projectileItem, sharedSecret);
         } else {
            this.shootHitscan(player, gunStack, fireModeInstance, sharedSecret);
         }
      }

      world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.8F);
   }

   public void shootHitscan(Player player, ItemStack gunStack, FireModeInstance fireModeInstance, long sharedSecret) {
      GunItem gun = (GunItem)gunStack.getItem();
      MixinGunItemDuck gunDuck = (MixinGunItemDuck)gun;
      int ammo = GunItem.getAmmo(gunStack, fireModeInstance);
      if (ammo > 0) {
         if (gun.getMaxAmmoCapacity(gunStack, fireModeInstance) < Integer.MAX_VALUE) {
            GunItem.setAmmo(gunStack, fireModeInstance, ammo - 1);
            CreateTypePacketHandler.getChannel()
               .send(
                  PacketDistributor.PLAYER.with(() -> (ServerPlayer)player),
                  new GunMountAmmoSyncPacket(this.getId(), GunItem.getAmmo(gunStack, fireModeInstance))
               );
         }

         SoundFeature.playFireSound(player, gunStack);
         Pair<Integer, Double> pcs = FireModeFeature.getPelletCountAndSpread(player, null, gunStack);
         int shotCount = pcs.getFirst() > 0 ? (Integer)pcs.getFirst() : 1;
         double adjustedInaccuracy = gunDuck.tyh$AdjustInaccuracy(player, gunStack, true);
         long xorSeed = getOrAssignRandomSeed(gunStack) ^ sharedSecret;
         Vec3 eyePos = this.getMuzzlePosition();
         Vec3 lookVec = this.getLookAngle();
         ServerLevel level = (ServerLevel)MiscUtil.getLevel(player);
         double maxHitScanDistance = gunDuck.tyh$getMaxServerShootingDistance(gunStack, true, level);
         List<BlockPos> blockPosToDestroy = new ArrayList<>();

         for (HitResult hitResult : new ArrayList<>(
            HitScan.getObjectsInCrosshair(
               player,
               eyePos,
               lookVec,
               0.0F,
               maxHitScanDistance,
               shotCount,
               adjustedInaccuracy,
               xorSeed,
               this.getDestroyBlockByHitScanPredicate(),
               this.getPassThroughBlocksByHitScanPredicate(),
               blockPosToDestroy
            )
         )) {
            gunDuck.tyh$hitScanTarget(player, gunStack, 0, 0, hitResult, maxHitScanDistance, blockPosToDestroy);
         }
      }
   }

   public void shootProjectile(Player player, ItemStack gunStack, FireModeInstance fireModeInstance, AmmoItem projectileItem, long sharedSecret) {
      GunItem gun = (GunItem)gunStack.getItem();
      MixinGunItemDuck gunDuck = (MixinGunItemDuck)gun;
      if (projectileItem == null) {
         TallyhoMod.warn("Attempted to handle client projectile fire request with an item that does not support projectiles: " + this);
      } else if (gun.isEnabled() && projectileItem.isEnabled()) {
         int ammo = GunItem.getAmmo(gunStack, fireModeInstance);
         if (ammo > 0) {
            if (gun.getMaxAmmoCapacity(gunStack, fireModeInstance) < Integer.MAX_VALUE) {
               GunItem.setAmmo(gunStack, fireModeInstance, ammo - 1);
               CreateTypePacketHandler.getChannel()
                  .send(
                     PacketDistributor.PLAYER.with(() -> (ServerPlayer)player),
                     new GunMountAmmoSyncPacket(this.getId(), GunItem.getAmmo(gunStack, fireModeInstance))
                  );
            }

            Vec3 muzzle = this.getMuzzlePosition();
            SlowProjectile projectile = projectileItem.createProjectile(player, muzzle.x, muzzle.y, muzzle.z);
            long xorSeed = getOrAssignRandomSeed(gunStack) ^ sharedSecret;
            double adjustedInaccuracy = gunDuck.tyh$AdjustInaccuracy(player, gunStack, true);
            projectile.shootAtLookTarget(player, adjustedInaccuracy, xorSeed);
            if (gun.getMaxAmmoCapacity(gunStack, fireModeInstance) < Integer.MAX_VALUE) {
               GunItem.setAmmo(gunStack, fireModeInstance, ammo - 1);
            }

            SoundFeature.playFireSound(player, gunStack);
            MiscUtil.getLevel(player).addFreshEntity(projectile);
         }
      }
   }

   public void setAmmo(int ammo) {
      ItemStack gunStack = this.getMountedGun();
      FireModeInstance fireModeInstance = GunItem.getFireModeInstance(gunStack);
      GunItem.setAmmo(gunStack, fireModeInstance, ammo);
   }

   private static long getOrAssignRandomSeed(ItemStack stack) {
      CompoundTag tag = stack.getOrCreateTag();
      long seed = tag.getLong("seed");
      if (tag.contains("seed", 99)) {
         return seed;
      }

      seed = random.nextLong();
      tag.putLong("seed", seed);
      return seed;
   }

   public Predicate<Block> getDestroyBlockByHitScanPredicate() {
      return block -> Config.bulletsBreakGlassEnabled
         && (block instanceof AbstractGlassBlock || block instanceof StainedGlassPaneBlock || block == Blocks.GLASS_PANE);
   }

   public Predicate<Block> getPassThroughBlocksByHitScanPredicate() {
      return block -> block instanceof BushBlock || block instanceof LeavesBlock;
   }

   public Vec2 getRotationLimit() {
      return this.ANGLE_LIMIT;
   }

   public float getBaseYaw() {
      return this.BASE_YAW;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_ID_GUN_ITEM, ItemStack.EMPTY);
   }

   protected void readAdditionalSaveData(CompoundTag nbt) {
      ItemStack g = ItemStack.of(nbt.getCompound("gunItem"));
      if (!g.isEmpty()) {
         this.setMountedGun(g);
      }

      this.BASE_YAW = nbt.getFloat("BASE_YAW");
      this.ANGLE_LIMIT = new Vec2(nbt.getFloat("ANGLE_LIMIT_X"), nbt.getFloat("ANGLE_LIMIT_Y"));
   }

   protected void addAdditionalSaveData(@NotNull CompoundTag nbt) {
      ItemStack g = this.getMountedGun();
      if (!g.isEmpty()) {
         nbt.put("gunItem", g.save(new CompoundTag()));
      }

      Vec2 r = this.getRotationLimit();
      nbt.putFloat("ANGLE_LIMIT_X", r.x);
      nbt.putFloat("ANGLE_LIMIT_Y", r.y);
      nbt.putFloat("BASE_YAW", this.getBaseYaw());
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public void writeSpawnData(FriendlyByteBuf buffer) {
      CompoundTag compound = new CompoundTag();
      this.addAdditionalSaveData(compound);
      buffer.writeNbt(compound);
   }

   public void readSpawnData(FriendlyByteBuf additionalData) {
      CompoundTag nbt = additionalData.readNbt();
      if (nbt != null) {
         this.readAdditionalSaveData(nbt);
      }
   }

   @Nullable
   public ShipMountedToData provideShipMountedToData(@Nonnull Entity entity, @Nullable Float partialTick) {
      if (this.getVehicle() instanceof ShipMountedToDataProvider smdp) {
         ShipMountedToData smd = smdp.provideShipMountedToData(entity, partialTick);
         return smd == null
            ? null
            : smd.copy(
               smd.getShipMountedTo(),
               smd.getMountPosInShip().add(0.0, this.getPassengersRidingOffset() + entity.getMyRidingOffset() + 0.33F, 0.0, new Vector3d())
            );
      } else {
         return null;
      }
   }
}
