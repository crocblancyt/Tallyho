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

public final class FragWarhead implements IWarhead {
    private final float power;
    private final float fragPercent;

    public FragWarhead(float power, float fragPercent) {
        this.power = power;
        this.fragPercent = fragPercent;
    }

    public float power() {
        return this.power;
    }

    public float fragPercent() {
        return this.fragPercent;
    }

    public boolean detonate(Level level, Vec3 hit, Vec3 dir) {
        FragExplosion explosion = new FragExplosion(level, null, null, hit.x, hit.y, hit.z, this.fragPercent, this.power, false, Level.ExplosionInteraction.BLOCK);
        CreateBigCannons.handleCustomExplosion(level, explosion);
        return true;
    }

    public void save(CompoundTag nbt) {}

    public void read(CompoundTag nbt) {}

    public void appendHoverText(List<Component> components) {
        components.add(Component.literal("Blast Fragmentation Warhead").setStyle(Style.EMPTY
                .withColor(ChatFormatting.GRAY)));
        components.add(Component.translatable("info.tallyho.tnt", ItemStack.ATTRIBUTE_MODIFIER_FORMAT
                .format((FragExplosion.effectiveBlockDamagePower(this.power, this.fragPercent) / 4.0F))).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        components.add(Component.translatable("info.tallyho.lethal_radius", ItemStack.ATTRIBUTE_MODIFIER_FORMAT
                .format(MissileRegistry.lethalRadius(this.power))).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
    }
}