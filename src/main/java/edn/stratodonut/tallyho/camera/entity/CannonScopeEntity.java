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
    WeakReference<PitchOrientedContraptionEntity> coaxialTo = new WeakReference<>(null);

    public CannonScopeEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() { }

    public CannonScopeEntity(Level level) {
        super(AllEntities.CANNON_SCOPE_ENTITY.get(), level);
    }

    public static CannonScopeEntity spawn(ServerLevel level, BlockPos pos, float baseYaw) {
        CannonScopeEntity result = new CannonScopeEntity(level);
        result.setParams(baseYaw, CameraEntity2.PostFXType.SCOPE, new AngleLimits(0.0F, 0.0F, 0.0F), 18);
        result.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        level.addFreshEntity(result);
        FlexibleSeatEntity.sitDown(level, pos, result);
        result.setYRot(baseYaw);
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            return;
        }

        if (this.coaxialTo.get() == null) {
            List<PitchOrientedContraptionEntity> cannons = new ArrayList<>();
            // DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ());
            if (cannons.isEmpty()) {
                return;
            }
            PitchOrientedContraptionEntity pitchOrientedContraptionEntity = cannons.get(0);
            this.coaxialTo = new WeakReference<>(pitchOrientedContraptionEntity);
        }

        PitchOrientedContraptionEntity cannonEntity = this.coaxialTo.get();
        if (cannonEntity.isRemoved() || !cannonEntity.isAlive()) {
            this.coaxialTo = new WeakReference<>(null);
            return;
        }

        Vec3 orientation = Vec3.atLowerCornerOf(cannonEntity.getInitialOrientation().getNormal());
        Vec3 muzzleDirection = cannonEntity.applyRotation(orientation, 1.0F);
        double dx = muzzleDirection.x;
        double dy = muzzleDirection.y;
        double dz = muzzleDirection.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float pitch = Mth.wrapDegrees((float) -(Mth.atan2(dy, horizontalDistance) * 57.2957763671875D));
        float yaw = Mth.wrapDegrees((float) (Mth.atan2(dz, dx) * 57.2957763671875D) - 90.0F);

        this.setRot(yaw, pitch);
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
    public void turnView(double yRot, double xRot) {  }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}