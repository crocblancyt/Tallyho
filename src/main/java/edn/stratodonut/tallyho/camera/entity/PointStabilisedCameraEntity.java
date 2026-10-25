package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.StabiliserSyncPacket;
import edn.stratodonut.tallyho.network.StabiliserSyncPacketClient;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public abstract class PointStabilisedCameraEntity extends CameraEntity2 implements IStabilisedTurret {
   @Nonnull
   protected Vec3 virtualStabVec = Vec3.ZERO;

   public PointStabilisedCameraEntity(EntityType<? extends Entity> p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
   }

   @Override
   public void tick() {
      super.tick();
      if ((this.level().isClientSide || !this.isPossessed()) && this.isStab()) {
         Vec2 rot = this.getStabView(0.0F);
         this.setRot(rot.y, rot.x);
      }

      if (!this.level().isClientSide) {
         if (this.tickCount % 20 == 0) {
            CreateTypePacketHandler.getChannel()
               .send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new StabiliserSyncPacketClient(this, this.virtualStabVec));
         }
      }
   }

   public float getViewXRot(float p_20268_) {
      return this.level().isClientSide && this.isStab() ? this.getStabView(p_20268_).x : super.getViewXRot(p_20268_);
   }

   public float getViewYRot(float p_20279_) {
      return this.level().isClientSide && this.isStab() ? this.getStabView(p_20279_).y : super.getViewYRot(p_20279_);
   }

   @Nonnull
   protected Vec2 getStabView(float partial_tick) {
      Ship s = VSGameUtilsKt.getShipMountedTo(this);
      return s != null && this.isStab() ? this.getLimitedRotationFromTarget(this.virtualStabVec, partial_tick) : this.getRotationVector();
   }

   protected void setStabVector(@Nonnull Vec3 v) {
      this.virtualStabVec = v;
      if (this.level().isClientSide) {
         CreateTypePacketHandler.getChannel().sendToServer(new StabiliserSyncPacket(this, v));
      } else {
         CreateTypePacketHandler.getChannel()
            .send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new StabiliserSyncPacketClient(this, this.virtualStabVec));
      }
   }

   @Override
   public void syncStab(Vec3 v) {
      if (!this.isPossessedAndClient()) {
         this.virtualStabVec = v;
      }
   }

   @Nonnull
   public String getCode() {
      Ship s = VSGameUtilsKt.getShipMountedTo(this);
      if (s != null && s.getSlug() != null) {
         return s.getSlug();
      } else {
         return this.currentlyViewing.get() != null ? this.currentlyViewing.get().getName().getString() : "";
      }
   }
}
