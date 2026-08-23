package edn.stratodonut.tallyho.mixin;

import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ContraptionWingProvider;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;
import org.valkyrienskies.mod.mixinducks.mod_compat.create.MixinAbstractContraptionEntityDuck;

@Mixin({AbstractContraptionEntity.class})
public abstract class MixinMixinAbstractContraptionEntity extends Entity implements MixinAbstractContraptionEntityDuck, ContraptionWingProvider, IEntityDraggingInformationProvider {
    @Shadow(remap = false)
    protected Contraption contraption;

    @Unique
    private boolean forceStall;

    @Shadow
    private boolean skipActorStop;

    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> STALLED;

    public MixinMixinAbstractContraptionEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.forceStall = false;
    }

    @Unique
    private BlockPos getTargetPos(MovementBehaviour instance, MovementContext context, BlockPos pos, Vec3 actorPosition) {
        return pos;
    }

    @Inject(method = {"tickActors"}, at = {@At("HEAD")}, cancellable = true, remap = false)
    private void preTickActors(CallbackInfo ci) {
        ci.cancel();
        boolean stalledPreviously = this.contraption.stalled;
        if (!level().isClientSide)
            this.contraption.stalled = this.forceStall;
        this.skipActorStop = true;
        for (MutablePair<StructureTemplate.StructureBlockInfo, MovementContext> pair : (Iterable<MutablePair<StructureTemplate.StructureBlockInfo, MovementContext>>)this.contraption.getActors()) {
            MovementContext context = pair.right;
            StructureTemplate.StructureBlockInfo blockInfo = pair.left;
            MovementBehaviour actor = AllMovementBehaviours.getBehaviour(blockInfo.state());
            if (actor == null)
                continue;
            Vec3 oldMotion = context.motion;
            Vec3 actorPosition = toGlobalVector(VecHelper.getCenterOf((Vec3i)blockInfo.pos())
                    .add(actor.getActiveAreaOffset(context)), 1.0F);
            BlockPos gridPosition = getTargetPos(actor, context, BlockPos.containing((Position)actorPosition), actorPosition);
            boolean newPosVisited = (!context.stall && shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition));
            context.rotation = (v -> applyRotation(v, 1.0F));
            context.position = actorPosition;
            if (!isActorActive(context, actor) && !actor.mustTickWhileDisabled())
                continue;
            if (newPosVisited && !context.stall) {
                actor.visitNewPosition(context, gridPosition);
                if (!isAlive())
                    break;
                context.firstMovement = false;
            }
            if (!oldMotion.equals(context.motion)) {
                actor.onSpeedChanged(context, oldMotion, context.motion);
                if (!isAlive())
                    break;
            }
            actor.tick(context);
            if (!isAlive())
                break;
            this.contraption.stalled |= context.stall;
        }
        if (!isAlive()) {
            this.contraption.stop(level());
            return;
        }
        this.skipActorStop = false;
        for (Entity entity : getPassengers()) {
            if (entity instanceof OrientedContraptionEntity) {
                OrientedContraptionEntity orientedCE = (OrientedContraptionEntity)entity;
                if (this.contraption.getBearingPosOf(entity.getUUID()) == null)
                    continue;
                if (orientedCE.getContraption() != null && (orientedCE.getContraption()).stalled) {
                    this.contraption.stalled = true;
                    break;
                }
            }
        }
        if (!level().isClientSide) {
            if (!stalledPreviously && this.contraption.stalled)
                onContraptionStalled();
            this.entityData.set(STALLED, this.contraption.stalled);
            return;
        }
        this.contraption.stalled = isStalled();
    }

    public boolean isPickable() {
        return true;
    }

    public void setForceStall(boolean forceStall) {
        this.forceStall = forceStall;
    }

    @Shadow
    public abstract Vec3 applyRotation(Vec3 vec, float partialTicks);

    @Shadow
    public abstract Vec3 toGlobalVector(Vec3 localVec, float partialTicks);

    @Shadow
    public abstract boolean isStalled();

    @Shadow
    protected abstract boolean shouldActorTrigger(MovementContext context, StructureTemplate.StructureBlockInfo blockInfo, MovementBehaviour actor, Vec3 actorPosition, BlockPos gridPosition);

    @Shadow
    protected abstract boolean isActorActive(MovementContext context, MovementBehaviour actor);

    @Shadow
    protected abstract void onContraptionStalled();
}