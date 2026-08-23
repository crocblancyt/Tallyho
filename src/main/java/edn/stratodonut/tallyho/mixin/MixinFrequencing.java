package edn.stratodonut.tallyho.mixin;

import com.codinglitch.simpleradio.core.central.Frequencing;
import com.codinglitch.simpleradio.core.central.Frequency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin({Frequencing.class})
public interface MixinFrequencing {
    @Overwrite(remap = false)
    default CompoundTag setFrequency(ItemStack stack, String frequencyName, Frequency.Modulation modulation) {
        CompoundTag tag = stack.getOrCreateTag();
        if (frequencyName != null && modulation != null && modulation.shorthand != null) {
            tag.putString("frequency", frequencyName);
            tag.putString("modulation", modulation.shorthand);
        }
        return tag;
    }
}