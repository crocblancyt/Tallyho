package edn.stratodonut.tallyho.mixin.cgm;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrcrayfish.framework.entity.sync.SyncedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(SyncedEntityData.class)
public abstract class MixinSyncedEntityData {
   @WrapOperation(
      method = "hasSyncedDataKey",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;level()Lnet/minecraft/world/level/Level;")
   )
   public Level mixinGetLevel(Entity instance, Operation<Level> original) {
      Level l = (Level)original.call(new Object[]{instance});
      if (l == null) {
         l = (Level)DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().level);
      }

      return l;
   }
}
