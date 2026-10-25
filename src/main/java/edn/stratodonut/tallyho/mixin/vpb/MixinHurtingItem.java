package edn.stratodonut.tallyho.mixin.vpb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vicmatskiv.pointblank.item.HurtingItem;
import com.vicmatskiv.pointblank.network.Network;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(HurtingItem.class)
public abstract class MixinHurtingItem {
   @WrapOperation(
      method = "spawnBlockBreakParticles",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraftforge/network/simple/SimpleChannel;send(Lnet/minecraftforge/network/PacketDistributor$PacketTarget;Ljava/lang/Object;)V"
      ),
      remap = false
   )
   private static <MSG> void mixinSpawnParticles(SimpleChannel instance, PacketTarget target, MSG message, Operation<Void> original) {
      Network.networkChannel.send(PacketDistributor.ALL.noArg(), message);
   }
}
