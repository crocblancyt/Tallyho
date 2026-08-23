package edn.stratodonut.tallyho.missile;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IWarhead {
    boolean detonate(Level paramLevel, Vec3 paramVec31, Vec3 paramVec32);

    void save(CompoundTag paramCompoundTag);

    void read(CompoundTag paramCompoundTag);

    void appendHoverText(List<Component> paramList);
}
