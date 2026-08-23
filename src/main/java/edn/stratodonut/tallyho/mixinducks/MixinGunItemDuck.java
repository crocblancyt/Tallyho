package edn.stratodonut.tallyho.mixinducks;

import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public interface MixinGunItemDuck {
    double tyh$AdjustInaccuracy(Player paramPlayer, ItemStack paramItemStack, boolean paramBoolean);

    AmmoItem tyh$getFirstCompatibleProjectile(ItemStack paramItemStack, FireModeInstance paramFireModeInstance);

    double tyh$getMaxServerShootingDistance(ItemStack paramItemStack, boolean paramBoolean, ServerLevel paramServerLevel);

    void tyh$hitScanTarget(Player paramPlayer, ItemStack paramItemStack, int paramInt1, int paramInt2, HitResult paramHitResult, double paramDouble, List<BlockPos> paramList);

    long tyh$getOrAssignRandomSeed(ItemStack paramItemStack);

    List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> tyh$getPhaseBuilders(GunItem.FirePhase paramFirePhase);
}
