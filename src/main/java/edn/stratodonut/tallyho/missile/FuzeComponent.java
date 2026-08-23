package edn.stratodonut.tallyho.missile;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import java.util.Optional;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface FuzeComponent {
    Optional<Vec3> tick(MountedMissileEntity paramMountedMissileEntity, Level paramLevel, Vec3 paramVec31, Vec3 paramVec32);

    default boolean impact(MountedMissileEntity missile, Level level, Vec3 pos, Vec3 del) {
      return true;
    }

    default boolean explodesOnExpiry() {
      return true;
    }
}
