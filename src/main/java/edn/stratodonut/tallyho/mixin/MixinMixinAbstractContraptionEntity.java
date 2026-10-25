package edn.stratodonut.tallyho.mixin;

import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
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

@Mixin(AbstractContraptionEntity.class)
public abstract class MixinMixinAbstractContraptionEntity
   extends Entity
   implements MixinAbstractContraptionEntityDuck,
   ContraptionWingProvider,
   IEntityDraggingInformationProvider {
   @Shadow(remap = false)
   protected Contraption contraption;
   @Unique
   private boolean vs$forceStall = false;
   @Shadow
   private boolean skipActorStop;
   @Shadow
   @Final
   private static EntityDataAccessor<Boolean> STALLED;

   public MixinMixinAbstractContraptionEntity(EntityType<?> entityType, Level level) {
      super(entityType, level);
   }

   @Shadow
   public abstract Vec3 applyRotation(Vec3 var1, float var2);

   @Shadow
   public abstract Vec3 toGlobalVector(Vec3 var1, float var2);

   @Unique
   private BlockPos vs$getTargetPos(MovementBehaviour instance, MovementContext context, BlockPos pos, Vec3 actorPosition) {
      return pos;
   }

   @Shadow
   public abstract boolean isStalled();

   @Shadow
   protected abstract boolean shouldActorTrigger(MovementContext var1, StructureBlockInfo var2, MovementBehaviour var3, Vec3 var4, BlockPos var5);

   @Shadow
   protected abstract boolean isActorActive(MovementContext var1, MovementBehaviour var2);

   @Shadow
   protected abstract void onContraptionStalled();

   @Inject(method = "tickActors", at = @At("HEAD"), cancellable = true, remap = false)
   private void preTickActors(CallbackInfo ci) {
      ci.cancel();
      boolean stalledPreviously = this.contraption.stalled;
      if (!this.level().isClientSide) {
         this.contraption.stalled = this.vs$forceStall;
      }

      this.skipActorStop = true;

      for (MutablePair<StructureBlockInfo, MovementContext> pair : this.contraption.getActors()) {
         MovementContext context = (MovementContext)pair.right;
         StructureBlockInfo blockInfo = (StructureBlockInfo)pair.left;
         MovementBehaviour actor = AllMovementBehaviours.getBehaviour(blockInfo.state());
         if (actor != null) {
            Vec3 oldMotion = context.motion;
            Vec3 actorPosition = this.toGlobalVector(VecHelper.getCenterOf(blockInfo.pos()).add(actor.getActiveAreaOffset(context)), 1.0F);
            BlockPos gridPosition = this.vs$getTargetPos(actor, context, BlockPos.containing(actorPosition), actorPosition);
            boolean newPosVisited = !context.stall && this.shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition);
            context.rotation = v -> this.applyRotation(v, 1.0F);
            context.position = actorPosition;
            if (this.isActorActive(context, actor) || actor.mustTickWhileDisabled()) {
               if (newPosVisited && !context.stall) {
                  actor.visitNewPosition(context, gridPosition);
                  if (!this.isAlive()) {
                     break;
                  }

                  context.firstMovement = false;
               }

               if (!oldMotion.equals(context.motion)) {
                  actor.onSpeedChanged(context, oldMotion, context.motion);
                  if (!this.isAlive()) {
                     break;
                  }
               }

               actor.tick(context);
               if (!this.isAlive()) {
                  break;
               }

               this.contraption.stalled = this.contraption.stalled | context.stall;
            }
         }
      }

      if (!this.isAlive()) {
         this.contraption.stop(this.level());
      } else {
         this.skipActorStop = false;

         for (Entity entity : this.getPassengers()) {
            if (entity instanceof OrientedContraptionEntity orientedCE
               && this.contraption.getBearingPosOf(entity.getUUID()) != null
               && orientedCE.getContraption() != null
               && orientedCE.getContraption().stalled) {
               this.contraption.stalled = true;
               break;
            }
         }

         if (!this.level().isClientSide) {
            if (!stalledPreviously && this.contraption.stalled) {
               this.onContraptionStalled();
            }

            this.entityData.set(STALLED, this.contraption.stalled);
         } else {
            this.contraption.stalled = this.isStalled();
         }
      }
   }

   public boolean ignoreExplosion() {
      return true;
   }

   public void vs$setForceStall(boolean forceStall) {
      this.vs$forceStall = forceStall;
   }
}
