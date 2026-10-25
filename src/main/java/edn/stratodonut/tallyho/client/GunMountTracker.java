package edn.stratodonut.tallyho.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.client.VertexConsumers;
import com.vicmatskiv.pointblank.client.effect.DetachedProjectileEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder.Context;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect.Builder;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.feature.SoundFeature;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.GunItem.FirePhase;
import com.vicmatskiv.pointblank.util.HitScan;
import com.vicmatskiv.pointblank.util.MiscUtil;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import edn.stratodonut.tallyho.mixinducks.MixinGunItemDuck;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.ServerboundGunFirePacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class GunMountTracker {
   private static final ResourceLocation POINTBLANK_OVERLAY_RESOURCE = new ResourceLocation("pointblank", "textures/gui/ammo.png");
   private static final Random random = new Random();
   private static long fireCooldownStartTime = 0L;
   private static long lastShotFiredTime = 0L;
   private static ItemStack gunStack;
   private static final List<MuzzleFlashEffect> muzzleFlashList = new ArrayList<>();

   public static void tick() {
      muzzleFlashList.removeIf(MuzzleFlashEffect::isExpired);
   }

   public static List<MuzzleFlashEffect> getMuzzleFlashList() {
      return muzzleFlashList;
   }

   public static void reset() {
      fireCooldownStartTime = 0L;
      lastShotFiredTime = 0L;
   }

   public static boolean isFireCooldownInProgress(ItemStack itemStack) {
      return System.nanoTime() - fireCooldownStartTime <= getFireCooldownDuration(itemStack);
   }

   public static long getFireCooldownDuration(ItemStack itemStack) {
      return (long)(6.0E10 / FireModeFeature.getRpm(itemStack));
   }

   public static void addMuzzleFlash(MuzzleFlashEffect effect) {
      muzzleFlashList.add(effect);
   }

   public static void fireIfPossible(Player player, GunMountEntity mountEntity, ItemStack gunStack) {
      if (!gunStack.isEmpty() && GunItem.getAmmo(gunStack, GunItem.getFireModeInstance(gunStack)) > 0 && !isFireCooldownInProgress(gunStack)) {
         MixinGunItemDuck gunItem = (MixinGunItemDuck)gunStack.getItem();
         long sharedSecret = random.nextLong();
         long xorSecret = gunItem.tyh$getOrAssignRandomSeed(gunStack) ^ sharedSecret;
         SoundFeature.playFireSound(player, gunStack);
         fireCooldownStartTime = System.nanoTime();
         lastShotFiredTime = MiscUtil.getLevel(player).getGameTime();
         CreateTypePacketHandler.getChannel().sendToServer(new ServerboundGunFirePacket(sharedSecret));
         Pair<Integer, Double> pcs = FireModeFeature.getPelletCountAndSpread(player, null, gunStack);
         int shotCount = pcs.getFirst() > 0 ? (Integer)pcs.getFirst() : 1;
         if (shotCount <= 1) {
            double maxDistance = 300.0;
            double adjustedInaccuracy = gunItem.tyh$AdjustInaccuracy(player, gunStack, true);
            List<HitResult> hitResults = HitScan.getObjectsInCrosshair(
               player,
               mountEntity.getMuzzlePosition(),
               mountEntity.getLookAngle(),
               1.0F,
               maxDistance,
               shotCount,
               adjustedInaccuracy,
               xorSecret,
               mountEntity.getDestroyBlockByHitScanPredicate(),
               mountEntity.getPassThroughBlocksByHitScanPredicate(),
               new ArrayList()
            );
            Context context = new Context()
               .withStartPosition(mountEntity.getMuzzlePosition())
               .withRandomization(0.0F)
               .withVertexConsumerTransformer(VertexConsumers.PARTICLE);
            List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> firingPhaseEffectBuilders = new ArrayList<>();
            firingPhaseEffectBuilders.addAll(Features.getEnabledPhaseEffects(gunStack, FirePhase.FIRING));
            firingPhaseEffectBuilders.addAll(gunItem.tyh$getPhaseBuilders(FirePhase.HIT_SCAN_ACQUIRED));

            for (Supplier<EffectBuilder<?, ?>> s : firingPhaseEffectBuilders) {
               EffectBuilder<?, ?> effectBuilder = s.get();
               if (effectBuilder instanceof Builder b) {
                  addMuzzleFlash(b.build(context));
               }

               if (effectBuilder instanceof com.vicmatskiv.pointblank.client.effect.DetachedProjectileEffect.Builder b) {
                  for (HitResult hitResult : hitResults) {
                     Context context2 = new Context()
                        .withStartPosition(mountEntity.getMuzzlePosition())
                        .withHitResult(hitResult)
                        .withRandomization(0.0F)
                        .withVertexConsumerTransformer(VertexConsumers.PARTICLE);
                     DetachedProjectileEffect effect = b.build(context2);
                     effect.launch(player);
                  }
               }
            }
         }
      }
   }

   public static void renderOverlay(GuiGraphics guiGraphics, ItemStack stack) {
      int textureWidth = 160;
      int textureHeight = 32;
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      Minecraft mc = Minecraft.getInstance();
      FireModeInstance fireModeInstance = GunItem.getFireModeInstance(stack);
      if (fireModeInstance != null) {
         String fireModeDisplayName = "TURRET";
         int width = 9 + mc.font.width(fireModeDisplayName);
         int height = 22;
         int vOffset = mc.getWindow().getGuiScaledHeight() - height;
         int hOffset = (mc.getWindow().getGuiScaledWidth() >> 1) + 97;
         guiGraphics.blitNineSliced(POINTBLANK_OVERLAY_RESOURCE, hOffset, vOffset, width, height, 18, 4, textureWidth, textureHeight, 0, 0);
         guiGraphics.drawString(mc.font, fireModeDisplayName, hOffset + 5 + 1, vOffset + 7, 0, false);
         guiGraphics.drawString(mc.font, fireModeDisplayName, hOffset + 5 - 1, vOffset + 7, 0, false);
         guiGraphics.drawString(mc.font, fireModeDisplayName, hOffset + 5, vOffset + 7 + 1, 0, false);
         guiGraphics.drawString(mc.font, fireModeDisplayName, hOffset + 5, vOffset + 7 - 1, 0, false);
         guiGraphics.drawString(mc.font, fireModeDisplayName, hOffset + 5, vOffset + 7, 8040160, false);
         int currentAmmo = GunItem.getAmmo(stack, fireModeInstance);
         GunItem gunItem = (GunItem)stack.getItem();
         int maxAmmo = gunItem.getMaxAmmoCapacity(stack, fireModeInstance);
         String counter;
         if (maxAmmo == Integer.MAX_VALUE) {
            counter = "∞";
         } else {
            counter = String.format("%d/%d", currentAmmo, maxAmmo);
         }

         guiGraphics.drawString(mc.font, counter, hOffset + 5 + 1, vOffset - 5, 0, false);
         guiGraphics.drawString(mc.font, counter, hOffset + 5 - 1, vOffset - 5, 0, false);
         guiGraphics.drawString(mc.font, counter, hOffset + 5, vOffset - 5 + 1, 0, false);
         guiGraphics.drawString(mc.font, counter, hOffset + 5, vOffset - 5 - 1, 0, false);
         guiGraphics.drawString(mc.font, counter, hOffset + 5, vOffset - 5, -1, false);
      }
   }
}
