package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.camera.AngleLimits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ThermalsEntity extends CameraEntity2 {
   public ThermalsEntity(EntityType<? extends Entity> p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
   }

   public static ThermalsEntity spawn(ServerLevel level, BlockPos pos, float base_yaw, AngleLimits angle_limit, int fov) {
      ThermalsEntity result = new ThermalsEntity((EntityType<? extends Entity>)AllEntities.THERMALS_ENTITY.get(), level);
      result.setParams(base_yaw, CameraEntity2.PostFXType.THERMALS_GREEN, angle_limit, fov);
      result.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      return result;
   }

   @Override
   public void tick() {
      super.tick();
   }

   @NotNull
   @Override
   public CameraEntity2.PostFXType getOverlayType() {
      return CameraEntity2.PostFXType.THERMALS_GREEN;
   }
}
