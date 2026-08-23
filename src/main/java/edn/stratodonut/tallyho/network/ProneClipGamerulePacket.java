package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import edn.stratodonut.tallyho.AllGameRules;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class ProneClipGamerulePacket extends SimplePacketBase {
    boolean value;

    public ProneClipGamerulePacket() {}

    public ProneClipGamerulePacket(FriendlyByteBuf buf) {
        this.value = buf.readBoolean();
    }

    public ProneClipGamerulePacket(boolean value) {
        this.value = value;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.value);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null)
                return;
            mc.player.level().getGameRules().getRule(AllGameRules.RULE_PRONE_CLIPPING).set(this.value, null);
        });
        return true;
    }
}