package edn.stratodonut.tallyho.missile;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public abstract class MotorComponent {
   public abstract void tick(MountedMissileEntity var1);

   public abstract boolean launch(MountedMissileEntity var1, Vec3 var2, float var3);

   public void deserialiseNBT(@Nonnull CompoundTag nbt) {
   }

   @Nonnull
   public CompoundTag serialiseNBT() {
      return new CompoundTag();
   }
}
