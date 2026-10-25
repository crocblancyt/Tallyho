package edn.stratodonut.tallyho.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.voice.client.RenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.Team.Visibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = RenderEvents.class, remap = false)
public abstract class MixinSimpleVCRenderEvent {
   @Inject(method = "onRenderName", at = @At("HEAD"), cancellable = true)
   private void mixinRenderNameplate(Entity entity, Component component, PoseStack stack, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
      if (Minecraft.getInstance().player != null && entity instanceof LivingEntity l && !this.tyh$shouldShowName(l)) {
         ci.cancel();
      }
   }

   @Unique
   public boolean tyh$shouldShowName(LivingEntity p_115333_) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer localplayer = minecraft.player;
      boolean flag = !p_115333_.isInvisibleTo(localplayer);
      if (p_115333_ != localplayer) {
         Team team = p_115333_.getTeam();
         Team team1 = localplayer.getTeam();
         if (team != null) {
            Visibility team$visibility = team.getNameTagVisibility();
            switch (team$visibility) {
               case ALWAYS:
                  return flag;
               case NEVER:
                  return false;
               case HIDE_FOR_OTHER_TEAMS:
                  return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);
               case HIDE_FOR_OWN_TEAM:
                  return team1 == null ? flag : !team.isAlliedTo(team1) && flag;
               default:
                  return true;
            }
         }
      }

      return true;
   }
}
