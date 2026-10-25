package edn.stratodonut.tallyho.mixinducks;

import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem.FirePhase;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public interface MixinGunItemDuck {
   double tyh$AdjustInaccuracy(Player var1, ItemStack var2, boolean var3);

   AmmoItem tyh$getFirstCompatibleProjectile(ItemStack var1, FireModeInstance var2);

   double tyh$getMaxServerShootingDistance(ItemStack var1, boolean var2, ServerLevel var3);

   void tyh$hitScanTarget(Player var1, ItemStack var2, int var3, int var4, HitResult var5, double var6, List<BlockPos> var8);

   long tyh$getOrAssignRandomSeed(ItemStack var1);

   List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> tyh$getPhaseBuilders(FirePhase var1);
}
