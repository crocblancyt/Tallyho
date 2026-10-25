package edn.stratodonut.tallyho.camera.entity;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class PeriscopeEntity extends CameraEntity2 {
   public PeriscopeEntity(EntityType<? extends Entity> p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
   }

   @NotNull
   @Override
   public CameraEntity2.PostFXType getOverlayType() {
      return CameraEntity2.PostFXType.PERISCOPE;
   }

   @Override
   protected boolean isShortLived() {
      return false;
   }

   @Override
   public double getMyRidingOffset() {
      return -0.625;
   }

   public InteractionResult interact(Player p_19978_, InteractionHand p_19979_) {
      if (CameraPossessionController.isPossessingConductor(p_19978_)) {
         return InteractionResult.FAIL;
      }

      if (!p_19978_.level().isClientSide()) {
         if (p_19978_.getItemInHand(p_19979_).getItem() instanceof WrenchItem) {
            this.discard();
            return InteractionResult.SUCCESS;
         }

         this.startViewing((ServerPlayer)p_19978_);
      }

      return InteractionResult.CONSUME;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public static PeriscopeEntity spawn(ServerLevel level, Vec3 pos, float base_yaw, AngleLimits angle_limit, int fov) {
      PeriscopeEntity result = new PeriscopeEntity((EntityType<? extends Entity>)AllEntities.PERISCOPE_ENTITY.get(), level);
      result.setParams(base_yaw, CameraEntity2.PostFXType.PERISCOPE, angle_limit, fov);
      result.setPos(pos.x, pos.y, pos.z());
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      return result;
   }

   public static PeriscopeEntity spawnArc(ServerLevel level, Vec3 pos, float base_yaw) {
      return spawn(level, pos, base_yaw, new AngleLimits(0.0F, 0.0F, 67.0F), 70);
   }

   public static PeriscopeEntity spawn360(ServerLevel level, Vec3 pos, float base_yaw) {
      return spawn(level, pos, base_yaw, new AngleLimits(0.0F, 0.0F, 360.0F), 70);
   }
}
