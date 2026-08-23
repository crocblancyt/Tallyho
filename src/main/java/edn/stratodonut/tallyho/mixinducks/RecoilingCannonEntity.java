package edn.stratodonut.tallyho.mixinducks;

import edn.stratodonut.tallyho.network.RecoilingCannonUpdatePacket;
import net.minecraft.world.phys.Vec3;

public interface RecoilingCannonEntity {
    void recoil(Vec3 paramVec3);

    void handlePacket(RecoilingCannonUpdatePacket paramRecoilingCannonUpdatePacket);
}
