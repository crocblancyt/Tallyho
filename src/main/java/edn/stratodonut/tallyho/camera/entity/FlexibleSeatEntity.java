package edn.stratodonut.tallyho.camera.entity;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.camera.ICustomSeatPivot;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.ShipMountedToData;
import org.valkyrienskies.mod.common.entity.ShipMountedToDataProvider;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class FlexibleSeatEntity extends SeatEntity implements ShipMountedToDataProvider {
   int timeout = 0;

   public FlexibleSeatEntity(EntityType<? extends Entity> type, Level level) {
      super(type, level);
   }

   public FlexibleSeatEntity(Level world, BlockPos pos) {
      this((EntityType<? extends Entity>)AllEntities.CAMERA_SEAT_ENTITY.get(), world);
      this.noPhysics = true;
   }

   public static Builder<?> build(Builder<?> builder) {
      Builder<FlexibleSeatEntity> entityBuilder = (Builder<FlexibleSeatEntity>)builder;
      return entityBuilder.sized(0.01F, 0.01F);
   }

   public double getPassengersRidingOffset() {
      return 0.0;
   }

   protected void positionRider(Entity pEntity, MoveFunction pCallback) {
      if (pEntity instanceof ICustomSeatPivot p) {
         Vec3 riderPos = this.position().add(p.getRidingOffset());
         pCallback.accept(pEntity, riderPos.x, riderPos.y, riderPos.z);
      } else {
         super.positionRider(pEntity, pCallback);
      }
   }

   public void tick() {
      if (!this.level().isClientSide) {
         if (!this.isVehicle() || this.getFirstPassenger() == null) {
            this.timeout++;
            if (this.timeout > 100) {
               this.discard();
            }
         }
      }
   }

   @Nullable
   public ShipMountedToData provideShipMountedToData(@NotNull Entity passenger, @Nullable Float partialTicks) {
      LoadedShip shipObjectEntityMountedTo = VSGameUtilsKt.getShipObjectManagingPos(passenger.level(), VectorConversionsMCKt.toJOML(this.position()));
      if (shipObjectEntityMountedTo == null) {
         return null;
      }

      Vector3dc mountedPosInShip;
      if (partialTicks != null && passenger instanceof ICustomSeatPivot p) {
         mountedPosInShip = VectorConversionsMCKt.toJOML(this.getPosition(partialTicks).add(p.getRidingOffset()));
      } else {
         mountedPosInShip = VectorConversionsMCKt.toJOML(
            this.getPosition(partialTicks == null ? 0.0F : partialTicks).add(0.0, this.getPassengersRidingOffset() + passenger.getMyRidingOffset(), 0.0)
         );
      }

      return new ShipMountedToData(shipObjectEntityMountedTo, mountedPosInShip);
   }

   public static void sitDown(ServerLevel world, Vec3 pos, Entity entity) {
      FlexibleSeatEntity seat = new FlexibleSeatEntity(world, BlockPos.containing(pos));
      seat.setPos(pos.x, pos.y + 0.25, pos.z);
      world.addFreshEntity(seat);
      entity.startRiding(seat, true);
      if (entity instanceof TamableAnimal ta) {
         ta.setInSittingPose(true);
      }
   }

   public static void sitDown(ServerLevel world, BlockPos pos, Entity entity) {
      sitDown(world, Vec3.atBottomCenterOf(pos), entity);
   }

   public static class Render extends EntityRenderer<FlexibleSeatEntity> {
      public Render(Context context) {
         super(context);
      }

      public boolean shouldRender(FlexibleSeatEntity p_225626_1_, Frustum p_225626_2_, double p_225626_3_, double p_225626_5_, double p_225626_7_) {
         return false;
      }

      @NotNull
      public ResourceLocation getTextureLocation(FlexibleSeatEntity p_110775_1_) {
         return null;
      }
   }
}
