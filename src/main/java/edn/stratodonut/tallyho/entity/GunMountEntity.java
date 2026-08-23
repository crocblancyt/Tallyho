package edn.stratodonut.tallyho.entity;

import com.mojang.datafixers.util.Pair;
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
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
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

    public static final Predicate<LivingEntity> HOSTILE_ENTITIES;

    static {
        HOSTILE_ENTITIES = (entity ->
            (entity.getSoundSource() == SoundSource.HOSTILE && !(entity instanceof net.minecraft.world.entity.NeutralMob) && !entity.getType().getCategory().isFriendly()));
    }

    public GunMountEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public GunMountEntity(Level level) {
        super((EntityType)AllEntities.GUN_MOUNT_ENTITY.get(), level);
    }

    public static GunMountEntity spawn(ServerLevel level, Vec3 pos, float baseYaw) {
        GunMountEntity result = (GunMountEntity)AllEntities.GUN_MOUNT_ENTITY.create((Level)level);
        result.setPos(pos);
        level.addFreshEntity(result);
        FlexibleSeatEntity.sitDown(level, pos, result);
        result.setYRot(baseYaw);
        return result;
    }

    public ItemStack asItem() {
        return new ItemStack((ItemLike)AllItems.GUN_MOUNT_ITEM.get());
    }

    protected void spawnPost(float baseYaw, Vec2 angleLimit) {
        this.BASE_YAW = baseYaw;
        this.ANGLE_LIMIT = angleLimit;
    }

    public double getPassengersRidingOffset() {
        return -0.8D + Math.sin(Math.toRadians(getXRot())) * 0.5D;
    }

    public ItemStack getMountedGun() {
        return this.entityData.get(DATA_ID_GUN_ITEM);
    }

    public void setMountedGun(ItemStack stack) {
        this.entityData.set(DATA_ID_GUN_ITEM, stack);
    }

    public Vec3 getMuzzlePosition() {
        return position();
    }

    @NotNull
    public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (CameraPossessionController.isPossessingConductor((Entity)player))
            return InteractionResult.FAIL;
        if (!player.level().isClientSide()) {
            ItemStack heldStack = player.getItemInHand(hand);
            ItemStack gunStack = getMountedGun();
            if (heldStack.getItem() instanceof com.simibubi.create.content.equipment.wrench.WrenchItem) {
                player.addItem(gunStack);
                player.addItem(asItem());
                discard();
                return InteractionResult.CONSUME;
            }
            if (gunStack.isEmpty()) {
                int selected = player.getInventory().selected;
                heldStack = player.getInventory().getItem(selected + 1);
                if (heldStack.getItem() instanceof GunItem) {
                    setMountedGun(heldStack.copy());
                    heldStack.shrink(1);
                    return InteractionResult.CONSUME;
                }
            } else if (player.isShiftKeyDown()) {

            }
            player.startRiding(this);
            FireModeInstance fireModeInstance = GunItem.getFireModeInstance(gunStack);
            if (player instanceof ServerPlayer) {
                ServerPlayer p = (ServerPlayer)player;
                CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new GunMountAmmoSyncPacket(
                        getId(), GunItem.getAmmo(gunStack, fireModeInstance)));
            }
        }
        return InteractionResult.SUCCESS;
    }

    public void tick() {
        super.tick();
        Function<Vec3, Vec3> transform = x -> x;
        if (getVehicle() != null) {
            Ship s = VSGameUtilsKt.getShipManaging(getVehicle());
            if (s != null)
                transform = (x -> VectorConversionsMCKt.toMinecraft((Vector3dc)s.getWorldToShip().transformDirection(VectorConversionsMCKt.toJOML(x))));
        }
        Entity entity = getControllingPassenger();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            Vec3 look = transform.apply(player.getLookAngle());
            double d0 = look.x;
            double d1 = look.y;
            double d2 = look.z;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            float rotX = Mth.wrapDegrees((float)-(Mth.atan2(d1, d3) * 57.2957763671875D));
            float rotY = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * 57.2957763671875D) - 90.0F);
            setXRot(rotX);
            if (this.ANGLE_LIMIT.y >= 180.0F) {
                setYRot(rotY);
            } else {
                setYRot(Mth.clamp(rotY, this.BASE_YAW - this.ANGLE_LIMIT.y, this.BASE_YAW + this.ANGLE_LIMIT.y));
            }
            setYHeadRot(getYRot());
            this.xRotO = getXRot();
            this.yRotO = getYRot();
        }
    }

    public void handleShoot(Player player, long sharedSecret) {
        Level world = player.level();
        ItemStack gunStack = getMountedGun();
        Item item = gunStack.getItem();
        if (item instanceof GunItem) {
            GunItem gun = (GunItem)item;
            MixinGunItemDuck gunDuck = (MixinGunItemDuck)gun;
            FireModeInstance fireModeInstance = GunItem.getFireModeInstance(gunStack);
            AmmoItem projectileItem = gunDuck.tyh$getFirstCompatibleProjectile(gunStack, GunItem.getFireModeInstance(gunStack));
            if (projectileItem != null) {
                shootProjectile(player, gunStack, fireModeInstance, projectileItem, sharedSecret);
            } else {
                shootHitscan(player, gunStack, fireModeInstance, sharedSecret);
            }
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 0.3F, 0.8F);
    }

    public void shootHitscan(Player player, ItemStack gunStack, FireModeInstance fireModeInstance, long sharedSecret) {
        GunItem gun = (GunItem)gunStack.getItem();
        MixinGunItemDuck gunDuck = (MixinGunItemDuck)gun;
        int ammo = GunItem.getAmmo(gunStack, fireModeInstance);
        if (ammo > 0) {
            if (gun.getMaxAmmoCapacity(gunStack, fireModeInstance) < Integer.MAX_VALUE) {
                GunItem.setAmmo(gunStack, fireModeInstance, ammo - 1);
                CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new GunMountAmmoSyncPacket(
                        getId(), GunItem.getAmmo(gunStack, fireModeInstance)));
            }
            SoundFeature.playFireSound(player, gunStack);
            Pair<Integer, Double> pcs = FireModeFeature.getPelletCountAndSpread((LivingEntity)player, null, gunStack);
            int shotCount = (pcs.getFirst() > 0) ? pcs.getFirst() : 1;
            double adjustedInaccuracy = gunDuck.tyh$AdjustInaccuracy(player, gunStack, true);
            long xorSeed = getOrAssignRandomSeed(gunStack) ^ sharedSecret;
            Vec3 eyePos = getMuzzlePosition();
            Vec3 lookVec = getLookAngle();
            ServerLevel level = (ServerLevel)MiscUtil.getLevel((Entity)player);
            double maxHitScanDistance = gunDuck.tyh$getMaxServerShootingDistance(gunStack, true, level);
            List<BlockPos> blockPosToDestroy = new ArrayList<>();
            List<HitResult> hitResults = new ArrayList<>(HitScan.getObjectsInCrosshair((LivingEntity)player, eyePos, lookVec, 0.0F, maxHitScanDistance, shotCount, adjustedInaccuracy, xorSeed,

                getDestroyBlockByHitScanPredicate(),
                getPassThroughBlocksByHitScanPredicate(), blockPosToDestroy));
            for (HitResult hitResult : hitResults)
                gunDuck.tyh$hitScanTarget(player, gunStack, 0, 0, hitResult, maxHitScanDistance, blockPosToDestroy);
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
                    CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new GunMountAmmoSyncPacket(
                            getId(), GunItem.getAmmo(gunStack, fireModeInstance)));
                }
                Vec3 muzzle = getMuzzlePosition();
                SlowProjectile projectile = projectileItem.createProjectile((LivingEntity)player, muzzle.x, muzzle.y, muzzle.z);
                long xorSeed = getOrAssignRandomSeed(gunStack) ^ sharedSecret;
                double adjustedInaccuracy = gunDuck.tyh$AdjustInaccuracy(player, gunStack, true);
                projectile.shootAtLookTarget((LivingEntity)player, adjustedInaccuracy, xorSeed);
                if (gun.getMaxAmmoCapacity(gunStack, fireModeInstance) < Integer.MAX_VALUE)
                    GunItem.setAmmo(gunStack, fireModeInstance, ammo - 1);
                SoundFeature.playFireSound(player, gunStack);
                MiscUtil.getLevel((Entity)player).addFreshEntity((Entity)projectile);
            }
        }
    }

    public void setAmmo(int ammo) {
        ItemStack gunStack = getMountedGun();
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(gunStack);
        GunItem.setAmmo(gunStack, fireModeInstance, ammo);
    }

    private static long getOrAssignRandomSeed(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        long seed = tag.getLong("seed");
        if (tag.contains("seed", 99))
            return seed;
        seed = random.nextLong();
        tag.putLong("seed", seed);
        return seed;
    }

    public Predicate<Block> getDestroyBlockByHitScanPredicate() {
        return block -> (Config.bulletsBreakGlassEnabled && (block instanceof net.minecraft.world.level.block.AbstractGlassBlock || block instanceof net.minecraft.world.level.block.StainedGlassPaneBlock || block == Blocks.GLASS));
    }

    public Predicate<Block> getPassThroughBlocksByHitScanPredicate() {
        return block -> (block instanceof net.minecraft.world.level.block.BushBlock || block instanceof net.minecraft.world.level.block.LeavesBlock);
    }

    public Vec2 getRotationLimit() {
        return this.ANGLE_LIMIT;
    }

    public float getBaseYaw() {
        return this.BASE_YAW;
    }

    public boolean isPickable() {
        return !isRemoved();
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_GUN_ITEM, ItemStack.EMPTY);
    }

    protected void readAdditionalSaveData(CompoundTag nbt) {
        ItemStack g = ItemStack.of(nbt.getCompound("gunItem"));
        if (!g.isEmpty())
            setMountedGun(g);
        this.BASE_YAW = nbt.getFloat("BASE_YAW");
        this.ANGLE_LIMIT = new Vec2(nbt.getFloat("ANGLE_LIMIT_X"), nbt.getFloat("ANGLE_LIMIT_Y"));
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        ItemStack g = getMountedGun();
        if (!g.isEmpty())
            nbt.put("gunItem", g.save(new CompoundTag()));
        Vec2 r = getRotationLimit();
        nbt.putFloat("ANGLE_LIMIT_X", r.x);
        nbt.putFloat("ANGLE_LIMIT_Y", r.y);
        nbt.putFloat("BASE_YAW", getBaseYaw());
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        CompoundTag compound = new CompoundTag();
        addAdditionalSaveData(compound);
        buffer.writeNbt(compound);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        CompoundTag nbt = additionalData.readNbt();
        if (nbt != null)
            readAdditionalSaveData(nbt);
    }

    @Nullable
    public ShipMountedToData provideShipMountedToData(@Nonnull Entity entity, @Nullable Float partialTick) {
        Entity vehicle = getVehicle();
        if (vehicle instanceof ShipMountedToDataProvider) {
            ShipMountedToDataProvider smdp = (ShipMountedToDataProvider)vehicle;
            ShipMountedToData smd = smdp.provideShipMountedToData(entity, partialTick);
            if (smd == null)
                return null;
            return smd.copy(smd
                .getShipMountedTo(), (Vector3dc)smd
                .getMountPosInShip().add(0.0D,

                    getPassengersRidingOffset() + entity.getMyRidingOffset() + 0.33000001311302185D, 0.0D, new Vector3d()));
        }
        return null;
    }
}