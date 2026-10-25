package edn.stratodonut.tallyho.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.ExplosionSuppressPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(Explosion.class)
public abstract class MixinExplosion {
   @Inject(method = "getSeenPercent", at = @At("HEAD"), cancellable = true)
   private static void mixinExplosion(Vec3 p_46065_, Entity p_46066_, CallbackInfoReturnable<Float> cir) {
      if (VSGameUtilsKt.isBlockInShipyard(p_46066_.level(), p_46066_.position())) {
         cir.setReturnValue(0.0F);
      }

      if (p_46066_.getBoundingBox().getSize() > 3.0) {
         cir.setReturnValue(0.0F);
      }
   }

   @WrapOperation(
      method = "explode",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
   )
   private boolean mixinHurt(Entity entity, DamageSource p_19946_, float p_19947_, Operation<Boolean> original) {
      if (entity instanceof ServerPlayer p && !p.isCreative()) {
         CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new ExplosionSuppressPacket(p_19947_ * 10.0F));
      }

      return (Boolean)original.call(new Object[]{entity, p_19946_, p_19947_});
   }
}
