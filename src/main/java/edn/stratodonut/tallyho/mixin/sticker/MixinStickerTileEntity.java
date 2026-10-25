package edn.stratodonut.tallyho.mixin.sticker;

import com.simibubi.create.content.contraptions.chassis.StickerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import edn.stratodonut.tallyho.sticker.ISticker;
import edn.stratodonut.tallyho.sticker.StickerMovementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.clockwork.forge.content.contraptions.sticker.StickerParticleUtil;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.mixinducks.mod_compat.create.IMixinStickerTileEntity;

@Mixin(value = StickerBlockEntity.class, priority = 1010)
public abstract class MixinStickerTileEntity extends SmartBlockEntity implements IMixinStickerTileEntity, ISticker {
   @Shadow
   LerpedFloat piston;
   @Unique
   private boolean vs_clockworkwaitForNoPower = false;
   boolean shipStuck = false;

   public MixinStickerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   @Shadow
   public abstract boolean isAttachedToBlock();

   @Shadow
   public abstract boolean isBlockStateExtended();

   @Inject(method = "tick", at = @At("HEAD"), remap = false)
   private void injectTick(CallbackInfo ci) {
      this.doTick();
   }

   private void doTick() {
      if (this.level != null) {
         Direction myDir = (Direction)this.getBlockState().getValue(DirectionalBlock.FACING);
         Vector3d myDirNormal = VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(myDir.getNormal()));
         boolean blockAttached = this.isAttachedToBlock();
         boolean shipAttached = StickerMovementBehaviour.isAttachedToShipOrWorld(
            false, this.level, VectorConversionsMCKt.toJOML(Vec3.atCenterOf(this.getBlockPos())), myDirNormal, this.getPersistentData()
         );
         if (!blockAttached && this.piston.getValue(0.0F) != this.piston.getValue() && this.piston.getValue() == 1.0F && shipAttached) {
            new StickerParticleUtil().doBluperParticle(this.level, this.worldPosition, myDir);
         }

         if (this.isBlockStateExtended() && !this.shipStuck) {
            this.vs_clockworkwaitForNoPower = false;
            if (!blockAttached
               && shipAttached
               && StickerMovementBehaviour.isAttachedToShipOrWorld(
                  true, this.level, VectorConversionsMCKt.toJOML(Vec3.atCenterOf(this.getBlockPos())), myDirNormal, this.getPersistentData()
               )) {
               this.shipStuck = true;
            }
         } else if (!this.isBlockStateExtended() && this.shipStuck) {
            if (!this.level.isClientSide) {
               this.vs_clockwork$removeConstraint((ServerLevel)this.level, true);
            }

            this.vs_clockworkwaitForNoPower = true;
         } else if (this.isBlockStateExtended()
            && !this.getPersistentData().contains("ShipStickerConstraint")
            && !this.shipStuck
            && !blockAttached
            && shipAttached
            && (Boolean)this.getBlockState().getValue(BlockStateProperties.POWERED)) {
            this.vs_clockworkwaitForNoPower = false;
            if (StickerMovementBehaviour.isAttachedToShipOrWorld(
               true, this.level, VectorConversionsMCKt.toJOML(Vec3.atCenterOf(this.getBlockPos())), myDirNormal, this.getPersistentData()
            )) {
               new StickerParticleUtil().doBluperParticle(this.level, this.worldPosition, myDir);
               this.shipStuck = true;
            }
         }

         if (this.vs_clockworkwaitForNoPower && !(Boolean)this.getBlockState().getValue(BlockStateProperties.POWERED)) {
            this.vs_clockworkwaitForNoPower = false;
            this.shipStuck = false;
         }
      }
   }

   public boolean isAlreadyPowered(boolean reset) {
      boolean result = this.getPersistentData().contains("ShipStickerAlreadyPowered");
      if (reset) {
         this.getPersistentData().remove("ShipStickerAlreadyPowered");
      }

      return result;
   }
}
