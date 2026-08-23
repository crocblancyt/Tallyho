package edn.stratodonut.tallyho.camera.entity;

import net.minecraft.world.phys.Vec3;

public interface IStabilisedTurret {
    boolean isStab();

    void syncStab(Vec3 paramVec3);
}
