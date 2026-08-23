package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.ITurret;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ServerboundGunFirePacket extends SimplePacketBase {
    long secret;

    public ServerboundGunFirePacket(long secret) {
        this.secret = secret;
    }

    public ServerboundGunFirePacket(FriendlyByteBuf buf) {
        this.secret = buf.readLong();
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.secret);
    }

    public boolean handle(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = context.getSender();
        if (serverPlayer != null)
            context.enqueueWork(() -> {
                CameraEntity2 camera = CameraPossessionController.getPossessingConductor((Entity)serverPlayer);
                if (camera instanceof ITurret) {
                    ITurret fm = (ITurret)camera;
                    fm.handleShoot((LivingEntity)serverPlayer);
                }
                Entity vehicle = serverPlayer.getVehicle();
                if (vehicle instanceof GunMountEntity) {
                    GunMountEntity fm = (GunMountEntity)vehicle;
                    fm.handleShoot((Player)serverPlayer, this.secret);
                }
            });
        return true;
    }
}