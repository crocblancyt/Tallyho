package edn.stratodonut.tallyho.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.voice.client.RenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = {RenderEvents.class}, remap = false)
public abstract class MixinSimpleVCRenderEvent {
    @Inject(method = {"onRenderName"}, at = {@At("HEAD")}, cancellable = true)
    private void mixinRenderNameplate(Entity entity, Component component, PoseStack stack, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && entity instanceof LivingEntity) {
            LivingEntity l = (LivingEntity)entity;
            if (!shouldShowName(l))
                ci.cancel();
        }
    }

    @Unique
    public boolean shouldShowName(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localplayer = minecraft.player;
        boolean flag = !entity.isInvisibleTo(localplayer);
        if (entity != localplayer) {
            Team team = entity.getTeam();
            Team team1 = localplayer.getTeam();
            if (team != null) {
                Team.Visibility visibility = team.getNameTagVisibility();
                switch (visibility) {
                    case ALWAYS:
                        return flag;
                    case NEVER:
                        return false;
                    case HIDE_FOR_OTHER_TEAMS:
                        return (team1 == null) ? flag : (team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag));
                    case HIDE_FOR_OWN_TEAM:
                        return (team1 == null) ? flag : (!team.isAlliedTo(team1) && flag);
                }
                return true;
            }
        }
        return true;
    }
}