package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class SMSCommandPacket extends SimplePacketBase {
    public int entityID;
    private CommandType type;

    public enum CommandType {
        LAUNCH(1),
        SEEKER(2);

        public final int val;

        CommandType(int v) {
            this.val = v;
        }
    }

    public SMSCommandPacket() {}

    public static SMSCommandPacket Launch(int entityID) {
        return new SMSCommandPacket(entityID, CommandType.LAUNCH);
    }

    public static SMSCommandPacket ActivateSeeker(int entityID) {
        return new SMSCommandPacket(entityID, CommandType.SEEKER);
    }

    private SMSCommandPacket(int entityID, CommandType type) {
        this.entityID = entityID;
        this.type = type;
    }

    public SMSCommandPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readInt();
        this.type = buf.readEnum(CommandType.class);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityID);
        buffer.writeEnum(this.type);
    }

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Entity e = context.getSender().level().getEntity(this.entityID);
            if (e instanceof MountedMissileEntity) {
                MountedMissileEntity missile = (MountedMissileEntity)e;
                switch (this.type) {
                    case LAUNCH:
                        missile.launch();
                        break;
                    case SEEKER:
                        if (missile.getGuidance() != null)
                            missile.getGuidance().activateSeeker();
                        break;
                }
            }
        });
        return true;
    }
}