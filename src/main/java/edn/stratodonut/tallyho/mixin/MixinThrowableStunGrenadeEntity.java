package edn.stratodonut.tallyho.mixin;

import com.mrcrayfish.guns.Config.EffectCriteria;
import com.mrcrayfish.guns.entity.ThrowableItemEntity;
import com.mrcrayfish.guns.entity.ThrowableStunGrenadeEntity;
import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.TallyhoMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ThrowableStunGrenadeEntity.class, remap = false)
public abstract class MixinThrowableStunGrenadeEntity extends ThrowableItemEntity {
   public MixinThrowableStunGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn) {
      super(entityType, worldIn);
   }

   @Inject(method = "onDeath", at = @At("HEAD"))
   private void mixinOnDeath(CallbackInfo ci) {
      if (this.getOwner() instanceof Player p) {
         TallyhoMod.warn("{} threw a stun grenade", p.getName());
      }
   }

   @Inject(method = "calculateAndApplyEffect", at = @At("HEAD"))
   private void mixinOnApply(
      MobEffect effect,
      EffectCriteria criteria,
      LivingEntity entity,
      Vec3 grenade,
      Vec3 eyes,
      double distance,
      double angle,
      CallbackInfoReturnable<Boolean> cir
   ) {
      if (this.getOwner() != null && entity instanceof Player p && p.hasPermissions(2) && p.isCreative()) {
         p.sendSystemMessage(Components.literal(this.getOwner().getName() + " threw a stun grenade at you"));
      }
   }
}
