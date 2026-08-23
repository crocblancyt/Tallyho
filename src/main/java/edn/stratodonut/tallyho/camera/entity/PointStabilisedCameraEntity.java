package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.StabiliserSyncPacket;
import edn.stratodonut.tallyho.network.StabiliserSyncPacketClient;
import javax.annotation.Nonnull;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public abstract class PointStabilisedCameraEntity extends CameraEntity2 implements IStabilisedTurret {

    @Nonnull
    protected Vec3 virtualStabVec = Vec3.ZERO;

    public PointStabilisedCameraEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        if ((this.level().isClientSide() || !isPossessed()) && isStab()) {
            Vec2 rot = getStabView(0.0F);
            this.setRot(rot.y, rot.x);
        }

        if (this.level().isClientSide()) {
            return;
        }

        if (this.tickCount % 20 == 0) {
            CreateTypePacketHandler.getChannel().send(
                PacketDistributor.TRACKING_ENTITY.with(() -> this),
                new StabiliserSyncPacketClient(this, this.virtualStabVec)
            );
        }
    }

    @Override
    public float getViewXRot(float partialTick) {
        if (this.level().isClientSide() && isStab()) {
            return (getStabView(partialTick)).x;
        }
        return super.getViewXRot(partialTick);
    }

    @Override
    public float getViewYRot(float partialTick) {
        if (this.level().isClientSide() && isStab()) {
            return (getStabView(partialTick)).y;
        }
        return super.getViewYRot(partialTick);
    }

    @Nonnull
    protected Vec2 getStabView(float partialTick) {
        LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(this);
        if (loadedShip != null && isStab()) {
            return getLimitedRotationFromTarget(this.virtualStabVec, partialTick);
        }
        return this.getRotationVector();
    }

    protected void setStabVector(@Nonnull Vec3 v) {
        this.virtualStabVec = v;
        if (this.level().isClientSide()) {
            CreateTypePacketHandler.getChannel().sendToServer(new StabiliserSyncPacket(this, v));
        } else {
            CreateTypePacketHandler.getChannel().send(
                PacketDistributor.TRACKING_ENTITY.with(() -> this),
                new StabiliserSyncPacketClient(this, this.virtualStabVec)
            );
        }
    }

    public void syncStab(Vec3 v) {
        if (!isPossessedAndClient()) {
            this.virtualStabVec = v;
        }
    }

    @Nonnull
    public String getCode() {
        LoadedShip loadedShip = VSGameUtilsKt.getShipMountedTo(this);
        if (loadedShip != null && loadedShip.getSlug() != null) {
            return loadedShip.getSlug();
        }
        if (this.currentlyViewing.get() != null) {
            return (this.currentlyViewing.get()).getGameProfile().getName();
        }
        return "";
    }
}