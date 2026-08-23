package edn.stratodonut.tallyho.camera.entity;

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
    public PeriscopeEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {

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
        return -0.625D;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (CameraPossessionController.isPossessingConductor(player)) {
            return InteractionResult.FAIL;
        }

        if (!player.level().isClientSide()) {
            if (player.getItemInHand(hand).getItem() instanceof com.simibubi.create.content.equipment.wrench.WrenchItem) {
                this.discard();
                return InteractionResult.SUCCESS;
            }
            startViewing((ServerPlayer) player);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    public static PeriscopeEntity spawn(ServerLevel level, Vec3 pos, float baseYaw, AngleLimits angleLimit, int fov) {
        PeriscopeEntity result = new PeriscopeEntity((EntityType<? extends Entity>) AllEntities.PERISCOPE_ENTITY.get(), level);
        result.setParams(baseYaw, CameraEntity2.PostFXType.PERISCOPE, angleLimit, fov);
        result.setPos(pos.x, pos.y, pos.z);
        level.addFreshEntity(result);
        FlexibleSeatEntity.sitDown(level, pos, result);
        result.setYRot(baseYaw);
        return result;
    }

    public static PeriscopeEntity spawnArc(ServerLevel level, Vec3 pos, float baseYaw) {
        return spawn(level, pos, baseYaw, new AngleLimits(0.0F, 0.0F, 67.0F), 70);
    }

    public static PeriscopeEntity spawn360(ServerLevel level, Vec3 pos, float baseYaw) {
        return spawn(level, pos, baseYaw, new AngleLimits(0.0F, 0.0F, 360.0F), 70);
    }
}