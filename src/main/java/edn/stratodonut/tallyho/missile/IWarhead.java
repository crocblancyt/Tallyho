package edn.stratodonut.tallyho.missile;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IWarhead {
   boolean detonate(Level var1, Vec3 var2, Vec3 var3);

   void save(CompoundTag var1);

   void read(CompoundTag var1);

   void appendHoverText(List<Component> var1);
}
