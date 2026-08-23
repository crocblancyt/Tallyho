package edn.stratodonut.tallyho.missile;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public abstract class MotorComponent {
    public abstract void tick(MountedMissileEntity paramMountedMissileEntity);

    public abstract boolean launch(MountedMissileEntity paramMountedMissileEntity, Vec3 paramVec3, float paramFloat);

    public void deserialiseNBT(@Nonnull CompoundTag nbt) {}

    @Nonnull
    public CompoundTag serialiseNBT() {
      return new CompoundTag();
    }
}
