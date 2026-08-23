package edn.stratodonut.tallyho.mixin.camera_possession;

import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.ICameraStatus;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.ILaserDesignator;
import edn.stratodonut.tallyho.item.LaserDesignatorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Gui.class})
public abstract class MixinGui {
    @Unique
    private static final ResourceLocation SCOPE_LOCATION = TallyhoMod.asResource("textures/misc/scope.png");
    @Unique
    private static final ResourceLocation PERISCOPE_LOCATION = TallyhoMod.asResource("textures/misc/periscope.png");
    @Unique
    private static final ResourceLocation THERMALS_LOCATION = TallyhoMod.asResource("textures/misc/thermals.png");
    @Unique
    private static final ResourceLocation MULTISPEC_LOCATION = TallyhoMod.asResource("textures/misc/apache.png");
    @Unique
    private static final ResourceLocation LASER_CROSS_LOCATION = TallyhoMod.asResource("textures/misc/laser.png");

    @Shadow
    protected int screenWidth;
    @Shadow
    protected int screenHeight;

    @Shadow
    public abstract Font getFont();

    @Inject(method = {"renderExperienceBar"}, at = {@At("HEAD")}, cancellable = true)
    private void railways$cancelExperienceBar(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        if (CameraClientHandler.isPlayerMountedOnCamera())
            ci.cancel();
    }

    @Inject(method = {"renderCrosshair"}, at = {@At("HEAD")}, cancellable = true)
    private void mixinRenderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (CameraClientHandler.isPlayerMountedOnCamera())
            ci.cancel();
    }

    @Inject(method = {"renderSpyglassOverlay"}, at = {@At("HEAD")}, cancellable = true)
    private void mixinRenderSpyglassOverlay(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        if (CameraClientHandler.isPlayerMountedOnCamera()) {
            CameraEntity2 ce = CameraClientHandler.getPlayerMountedOnCamera();
            switch (ce.getOverlayType()) {
                case THERMALS_GREEN:
                    tyh$renderOverlay(guiGraphics, THERMALS_LOCATION, partialTick);
                    break;
                case PERISCOPE:
                    tyh$renderOverlay(guiGraphics, PERISCOPE_LOCATION, partialTick);
                    break;
                case SCOPE:
                    tyh$renderOverlay(guiGraphics, SCOPE_LOCATION, partialTick);
                    break;
                case THERMALS_WHITE:
                case VISIBLE:
                    tyh$renderOverlay(guiGraphics, MULTISPEC_LOCATION, partialTick);
                    break;
                case NONE:
                    break;
                default:
                    return;
            }
            if (ce instanceof ICameraStatus) {
                ICameraStatus status = (ICameraStatus)ce;
                tyh$renderStatus(guiGraphics, status.getStatus());
            }
            if (ce instanceof ILaserDesignator) {
                ILaserDesignator d = (ILaserDesignator)ce;
                if (d.isDesignatorActive())
                    tyh$renderOverlay(guiGraphics, LASER_CROSS_LOCATION, partialTick);
            }
            ci.cancel();
        } else if (Minecraft.getInstance().player != null) {
            LocalPlayer localPlayer = Minecraft.getInstance().player;
            ItemStack stack = localPlayer.getUseItem();
            if (stack.getItem() instanceof LaserDesignatorItem) {
                tyh$renderOverlay(guiGraphics, MULTISPEC_LOCATION, partialTick);
                if (LaserDesignatorItem.isActiveDesignating(stack))
                    tyh$renderOverlay(guiGraphics, LASER_CROSS_LOCATION, partialTick);
                ci.cancel();
            }
        }
    }

    @Unique
    protected void tyh$renderStatus(GuiGraphics gui, String status) {
        int width = this.screenWidth;
        int y = this.screenHeight - 75;
        gui.drawString(getFont(), status, width / 2, y - 10, 13426175);
    }

    @Unique
    protected void tyh$renderOverlay(GuiGraphics guiGraphics, ResourceLocation resource, float partialTick) {
        int i = Mth.floor(this.screenWidth * partialTick);
        int j = Mth.floor(this.screenHeight * partialTick);
        int k = (this.screenWidth - i) / 2;
        int l = (this.screenHeight - j) / 2;
        int i1 = k + i;
        int j1 = l + j;
        guiGraphics.blit(resource, k, l, -90, 0.0F, 0.0F, i, j, i, j);
        guiGraphics.fill(RenderType.guiOverlay(), 0, j1, this.screenWidth, this.screenHeight, -90, -16777216);
        guiGraphics.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, l, -90, -16777216);
        guiGraphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
        guiGraphics.fill(RenderType.guiOverlay(), i1, l, this.screenWidth, j1, -90, -16777216);
    }
}