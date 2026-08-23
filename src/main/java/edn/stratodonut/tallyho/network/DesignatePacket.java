package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.ILaserDesignator;
import edn.stratodonut.tallyho.item.LaserDesignatorItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

public class DesignatePacket extends SimplePacketBase {
    boolean active;

    public DesignatePacket() {}

    public DesignatePacket(boolean active) {
        this.active = active;
    }

    public DesignatePacket(FriendlyByteBuf buf) {
        this.active = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.active);
    }

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            CameraEntity2 camera = CameraPossessionController.getPossessingConductor((Entity)serverPlayer);
            if (camera instanceof ILaserDesignator) {
                ILaserDesignator d = (ILaserDesignator)camera;
                d.designate();
            } else if (serverPlayer != null) {
                Item item = serverPlayer.getUseItem().getItem();
                if (item instanceof LaserDesignatorItem) {
                    LaserDesignatorItem designatorItem = (LaserDesignatorItem)item;
                    LaserDesignatorItem.designate(serverPlayer.getUseItem(), this.active);
                }
            }
        });
        return true;
    }
}