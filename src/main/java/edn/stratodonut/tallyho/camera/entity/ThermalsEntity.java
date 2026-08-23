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

    public ThermalsEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() { }

    public static ThermalsEntity spawn(ServerLevel level, BlockPos pos, float baseYaw, AngleLimits angleLimit, int fov) {
        ThermalsEntity result = new ThermalsEntity(AllEntities.THERMALS_ENTITY.get(), level);
        result.setParams(baseYaw, CameraEntity2.PostFXType.THERMALS_GREEN, angleLimit, fov);
        result.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        level.addFreshEntity(result);
        FlexibleSeatEntity.sitDown(level, pos, result);
        result.setYRot(baseYaw);
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