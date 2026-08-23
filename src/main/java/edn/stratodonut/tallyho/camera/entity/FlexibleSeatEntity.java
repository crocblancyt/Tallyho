package edn.stratodonut.tallyho.camera.entity;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.camera.ICustomSeatPivot;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.ShipMountedToData;
import org.valkyrienskies.mod.common.entity.ShipMountedToDataProvider;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class FlexibleSeatEntity extends SeatEntity implements ShipMountedToDataProvider {

    int timeout;

    public FlexibleSeatEntity(EntityType<? extends Entity> type, Level level) {
        super(type, level);
        this.timeout = 0;
    }

    public FlexibleSeatEntity(Level world, BlockPos pos) {
        this((EntityType<? extends Entity>) AllEntities.CAMERA_SEAT_ENTITY.get(), world);
        this.noPhysics = true;
    }

    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        EntityType.Builder<FlexibleSeatEntity> entityBuilder = (EntityType.Builder) builder;
        return entityBuilder.sized(0.01F, 0.01F);
    }

    public double getRideHeight() {
        return 0.0D;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (passenger instanceof ICustomSeatPivot) {
            ICustomSeatPivot pivot = (ICustomSeatPivot) passenger;
            Vec3 riderPosition = this.position().add(pivot.getRidingOffset());
            moveFunction.accept(passenger, riderPosition.x, riderPosition.y, riderPosition.z);
        } else {
            super.positionRider(passenger, moveFunction);
        }
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            return;
        }

        if (this.isVehicle() && this.getFirstPassenger() != null) {
            return;
        }

        this.timeout++;
        if (this.timeout > 100) {
            this.discard();
        }
    }

    @Nullable
    @Override
    public ShipMountedToData provideShipMountedToData(@NotNull Entity passenger, @Nullable Float partialTicks) {
        Vector3d position;
        LoadedShip ship = VSGameUtilsKt.getShipObjectManagingPos(
                passenger.level(),
                VectorConversionsMCKt.toJOML(this.position())
        );

        if (ship == null) {
            return null;
        }

        if (partialTicks != null && passenger instanceof ICustomSeatPivot) {
            ICustomSeatPivot pivot = (ICustomSeatPivot) passenger;
            position = VectorConversionsMCKt.toJOML(
                    this.getPosition(partialTicks).add(pivot.getRidingOffset())
            );
        } else {
            position = VectorConversionsMCKt.toJOML(
                    this.getPosition((partialTicks == null) ? 0.0F : partialTicks)
                            .add(0.0D, this.getRideHeight() + passenger.getMyRidingOffset(), 0.0D)
            );
        }

        return new ShipMountedToData(ship, position);
    }

    public static class Render extends EntityRenderer<Entity> {
        public Render(EntityRendererProvider.Context context) {
            super(context);
        }

        public boolean shouldRender(Entity entity, Frustum frustum, double p_225626_3_, double p_225626_5_, double p_225626_7_) {
            return false;
        }

        @NotNull
        public ResourceLocation getTextureLocation(Entity entity) {
            return null;
        }
    }

    public static void sitDown(ServerLevel world, Vec3 pos, Entity entity) {
        FlexibleSeatEntity seat = new FlexibleSeatEntity(world, BlockPos.containing(pos));
        seat.setPos(pos.x, pos.y + 0.25D, pos.z);
        world.addFreshEntity(seat);
        entity.startRiding(seat, true);

        if (entity instanceof TamableAnimal) {
            TamableAnimal tamableAnimal = (TamableAnimal) entity;
            tamableAnimal.setInSittingPose(true);
        }
    }

    public static void sitDown(ServerLevel world, BlockPos pos, Entity entity) {
        sitDown(world, Vec3.atBottomCenterOf(pos), entity);
    }
}