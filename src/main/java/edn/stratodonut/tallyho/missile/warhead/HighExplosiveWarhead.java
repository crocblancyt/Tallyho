package edn.stratodonut.tallyho.missile.warhead;

import edn.stratodonut.tallyho.missile.IWarhead;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.CreateBigCannons;

public final class HighExplosiveWarhead implements IWarhead {
    private final float power;

    public HighExplosiveWarhead(float power) {
        this.power = power;
    }

    public float power() {
        return this.power;
    }

    public boolean detonate(Level level, Vec3 hit, Vec3 dir) {
        HighExplosion explosion = new HighExplosion(level, null, null, new FragExplosion.FragmentationCalculator(0.5F), hit.x, hit.y, hit.z, this.power, false, Level.ExplosionInteraction.BLOCK);
        CreateBigCannons.handleCustomExplosion(level, explosion);
        return true;
    }

    public void save(CompoundTag nbt) {}

    public void read(CompoundTag nbt) {}

    public void appendHoverText(List<Component> components) {
        components.add(Component.literal("High Explosive Warhead").setStyle(Style.EMPTY
                .withColor(ChatFormatting.GRAY)));
        components.add(Component.translatable("info.tallyho.tnt", ItemStack.ATTRIBUTE_MODIFIER_FORMAT
                .format((this.power / 8.0F))).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        components.add(Component.translatable("info.tallyho.lethal_radius", ItemStack.ATTRIBUTE_MODIFIER_FORMAT
                .format(MissileRegistry.lethalRadius(this.power))).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
    }

    public String toString() {
        return "";
    }
}