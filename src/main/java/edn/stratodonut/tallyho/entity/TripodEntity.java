package edn.stratodonut.tallyho.entity;

import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.AllItems;
import edn.stratodonut.tallyho.camera.entity.FlexibleSeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TripodEntity extends GunMountEntity {
   public TripodEntity(EntityType<?> p_19870_, Level p_19871_) {
      super(p_19870_, p_19871_);
   }

   public TripodEntity(Level p_19871_) {
      super((EntityType<?>)AllEntities.TRIPOD_MOUNT_ENTITY.get(), p_19871_);
   }

   @Override
   public double getPassengersRidingOffset() {
      return -0.5 + Math.sin(Math.toRadians(this.getXRot())) * 0.5;
   }

   public static TripodEntity spawn(ServerLevel level, BlockPos pos, float base_yaw) {
      TripodEntity result = (TripodEntity)AllEntities.TRIPOD_MOUNT_ENTITY.create(level);
      result.setPos(Vec3.atBottomCenterOf(pos));
      result.spawnPost(base_yaw, new Vec2(60.0F, 45.0F));
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      return result;
   }

   @Override
   public Vec3 getMuzzlePosition() {
      return super.getMuzzlePosition().add(Vec3.directionFromRotation(0.0F, this.getBaseYaw()));
   }

   @Override
   public ItemStack asItem() {
      return new ItemStack((ItemLike)AllItems.TRIPOD_ITEM.get());
   }
}
