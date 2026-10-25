package edn.stratodonut.tallyho.mixin.cbc;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import riftyboi.cbcmodernwarfare.cannons.medium_cannon.MediumcannonBlockEntity;
import riftyboi.cbcmodernwarfare.cannons.medium_cannon.breech.MediumcannonBreechBlockEntity;
import riftyboi.cbcmodernwarfare.index.CBCModernWarfareItem;
import riftyboi.cbcmodernwarfare.munitions.medium_cannon.MediumcannonAmmoItem;

@Mixin(value = MediumcannonBreechBlockEntity.class, remap = false)
public abstract class MixinMediumCannonBreechBE extends MediumcannonBlockEntity {
   @Shadow
   private int loadingCooldown;
   @Unique
   private boolean playSound;

   public MixinMediumCannonBreechBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   @Shadow
   public abstract boolean isOpen();

   @Shadow
   public abstract void toggleOpening();

   @Shadow
   public abstract boolean canBeAutomaticallyLoaded();

   @Inject(method = "setInputBuffer", at = @At("RETURN"))
   private void mixinSetInputBuffer(ItemStack stack, CallbackInfo ci) {
      Item i = stack.getItem();
      if (i instanceof MediumcannonAmmoItem && !CBCModernWarfareItem.EMPTY_MEDIUMCANNON_CARTRIDGE.is(i)) {
         if (this.isOpen()) {
            this.toggleOpening();
         }

         this.playSound = true;
      }
   }

   @Inject(method = "tickFromContraption", at = @At("RETURN"))
   public void mixinTickFromContraption(Level level, PitchOrientedContraptionEntity poce, BlockPos localPos, CallbackInfo ci) {
      if (this.level != null && !this.level.isClientSide && this.playSound) {
         this.playSound = false;
         Vec3 v = poce.toGlobalVector(localPos.getCenter(), 0.0F);
         SoundEvent s = SoundEvents.IRON_TRAPDOOR_CLOSE;
         level.playSound((Player)null, BlockPos.containing(v), s, SoundSource.BLOCKS, 1.0F, 1.5F);
      }
   }

   @Inject(method = "canBeAutomaticallyLoaded", at = @At("HEAD"), cancellable = true)
   private void mixinCanBeAutomaticallyLoaded(CallbackInfoReturnable<Boolean> cir) {
      cir.setReturnValue(this.loadingCooldown <= 0 && this.isOpen());
   }
}
