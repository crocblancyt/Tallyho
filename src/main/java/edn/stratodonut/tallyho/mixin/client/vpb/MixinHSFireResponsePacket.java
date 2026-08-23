package edn.stratodonut.tallyho.mixin.client.vpb;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.VertexConsumers;
import com.vicmatskiv.pointblank.client.effect.DetachedProjectileEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.GunStateResponsePacket;
import com.vicmatskiv.pointblank.network.HitScanFireResponsePacket;
import com.vicmatskiv.pointblank.util.SimpleHitResult;
import edn.stratodonut.tallyho.client.GunMountTracker;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import edn.stratodonut.tallyho.mixinducks.MixinGunItemDuck;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.util.ClientUtils;

@Mixin(value = {HitScanFireResponsePacket.class}, remap = false)
public abstract class MixinHSFireResponsePacket extends GunStateResponsePacket {
    @Shadow
    private SimpleHitResult hitResult;
    @Shadow
    private float damage;

    @Inject(method = {"getItemStackAndState"}, at = {@At("RETURN")}, cancellable = true)
    private <T extends GunStateResponsePacket> void mixinGetItemStackAndState(T packet, Entity entity, CallbackInfoReturnable<Tuple<ItemStack, GunClientState>> cir) {
        Entity var5 = entity.getVehicle();
        if (var5 instanceof GunMountEntity mg) {
            ItemStack itemStack = mg.getMountedGun();
            if (itemStack.getItem() instanceof GunItem) {
                cir.setReturnValue(new Tuple<>(itemStack, null));
            }
        }
    }

    @Inject(method = {"handleEnqueued"}, at = {@At("RETURN")})
    private void mixinHandleEnqueued(Player player, ItemStack itemStack, GunClientState gunClientState, CallbackInfo ci) {
        if (player == ClientUtils.getClientPlayer())
            return;
        Entity entity = player.getVehicle();
        if (entity instanceof GunMountEntity) {
            GunMountEntity mg = (GunMountEntity)entity;
            Item item = itemStack.getItem();
            if (item instanceof GunItem) {
                GunItem gunItem = (GunItem)item;
                EffectBuilder.Context context = (new EffectBuilder.Context()).withGunState(gunClientState).withStartPosition(mg.getMuzzlePosition()).withHitResult((HitResult)this.hitResult).withDamage(this.damage).withRandomization(0.0F).withVertexConsumerTransformer(VertexConsumers.PARTICLE);
                List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> firingPhaseEffectBuilders = new ArrayList<>();
                firingPhaseEffectBuilders.addAll(Features.getEnabledPhaseEffects(itemStack, GunItem.FirePhase.FIRING));
                firingPhaseEffectBuilders.addAll(((MixinGunItemDuck)gunItem).tyh$getPhaseBuilders(GunItem.FirePhase.HIT_SCAN_ACQUIRED));
                for (Supplier<EffectBuilder<?, ?>> s : firingPhaseEffectBuilders) {
                    EffectBuilder<?, ?> effectBuilder = s.get();
                    if (effectBuilder instanceof MuzzleFlashEffect.Builder) {
                        MuzzleFlashEffect.Builder b = (MuzzleFlashEffect.Builder)effectBuilder;
                        GunMountTracker.addMuzzleFlash(b.build(context));
                    }
                    if (effectBuilder instanceof DetachedProjectileEffect.Builder) {
                        DetachedProjectileEffect.Builder b = (DetachedProjectileEffect.Builder)effectBuilder;
                        DetachedProjectileEffect effect = b.build(context);
                        effect.launch(player);
                    }
                }
            }
        }
    }
}