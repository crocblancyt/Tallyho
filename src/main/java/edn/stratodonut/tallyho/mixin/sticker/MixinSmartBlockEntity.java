package edn.stratodonut.tallyho.mixin.sticker;

import com.simibubi.create.content.contraptions.chassis.StickerBlockEntity;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import edn.stratodonut.tallyho.sticker.ISticker;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(value = SmartBlockEntity.class, priority = 1010)
public abstract class MixinSmartBlockEntity extends CachedRenderBBBlockEntity implements ISticker {
   public MixinSmartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   @Override
   public void vs_clockwork$removeConstraint(@Nullable ServerLevel level, boolean removeTags) {
      if (this.getPersistentData().contains("ShipStickerConstraint")) {
         if (level != null) {
            VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(this.getPersistentData().getInt("ShipStickerConstraint"));
            VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(this.getPersistentData().getInt("ShipStickerConstraint2"));
         }

         if (removeTags) {
            this.getPersistentData().remove("ShipStickerConstraint");
            this.getPersistentData().remove("ShipStickerShip1Id");
            this.getPersistentData().remove("ShipStickerShip1Vec");
            this.getPersistentData().remove("ShipStickerShip1Quat");
            this.getPersistentData().remove("ShipStickerShip2Id");
            this.getPersistentData().remove("ShipStickerShip2Vec");
            this.getPersistentData().remove("ShipStickerShip2Quat");
         }
      }
   }

   @Inject(method = "destroy", at = @At("TAIL"), remap = false)
   private void vs_clockwork$destroy(CallbackInfo ci) {
      if ((SmartBlockEntity) (Object) this instanceof StickerBlockEntity) {
         if (this.level == null) {
            throw new RuntimeException("ERROR Couldn't try to clean up constraint!");
         }

         if (!this.level.isClientSide) {
            this.vs_clockwork$removeConstraint((ServerLevel)this.level, true);
         }
      }
   }
}
