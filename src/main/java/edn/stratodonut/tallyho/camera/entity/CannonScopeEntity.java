package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.camera.AngleLimits;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkHooks;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public class CannonScopeEntity extends CameraEntity2 {
   @Nonnull
   WeakReference<PitchOrientedContraptionEntity> coaxial_to = new WeakReference<>(null);

   public CannonScopeEntity(EntityType<? extends Entity> p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
   }

   public CannonScopeEntity(Level p_20967_) {
      super((EntityType<? extends Entity>)AllEntities.CANNON_SCOPE_ENTITY.get(), p_20967_);
   }

   public static CannonScopeEntity spawn(ServerLevel level, BlockPos pos, float base_yaw) {
      CannonScopeEntity result = new CannonScopeEntity(level);
      result.setParams(base_yaw, CameraEntity2.PostFXType.SCOPE, new AngleLimits(0.0F, 0.0F, 0.0F), 18);
      result.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      return result;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         if (this.coaxial_to.get() == null) {
            List<PitchOrientedContraptionEntity> cannons = new ArrayList<>();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
               AABB testBox = this.getBoundingBox().inflate(2.0);

               for (Entity e : Minecraft.getInstance().level.entitiesForRendering()) {
                  AABB worldBox = VSGameUtilsKt.transformAabbToWorld(this.level(), e.getBoundingBox());
                  if (e instanceof PitchOrientedContraptionEntity poce && worldBox.intersects(testBox)) {
                     cannons.add(poce);
                  }
               }
            });
            if (cannons.isEmpty()) {
               return;
            }

            PitchOrientedContraptionEntity cannonEntity = cannons.get(0);
            this.coaxial_to = new WeakReference<>(cannonEntity);
         }

         PitchOrientedContraptionEntity cannonEntity = this.coaxial_to.get();
         if (!cannonEntity.isRemoved() && cannonEntity.isAlive()) {
            Vec3 orient = Vec3.atLowerCornerOf(cannonEntity.getInitialOrientation().getNormal());
            Vec3 muzzle = cannonEntity.applyRotation(orient, 1.0F);
            double d0 = muzzle.x;
            double d1 = muzzle.y;
            double d2 = muzzle.z;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            float x = Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * 180.0F / (float)Math.PI)));
            float y = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * 180.0F / (float)Math.PI) - 90.0F);
            this.setRot(y, x);
         } else {
            this.coaxial_to = new WeakReference<>(null);
         }
      }
   }

   @Nonnull
   @Override
   public CameraEntity2.PostFXType getOverlayType() {
      return CameraEntity2.PostFXType.SCOPE;
   }

   @Override
   public boolean isFixedByPlayer() {
      return false;
   }

   @Override
   public void turnView(double yRot, double xRot) {
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }
}
