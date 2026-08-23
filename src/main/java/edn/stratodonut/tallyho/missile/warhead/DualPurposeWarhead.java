package edn.stratodonut.tallyho.missile.warhead;

import edn.stratodonut.tallyho.missile.IWarhead;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class DualPurposeWarhead implements IWarhead {
    private final ShapedChargeWarhead sw;
    private final FragWarhead fw;

    public DualPurposeWarhead(ShapedChargeWarhead sw, FragWarhead fw) {
        this.sw = sw;
        this.fw = fw;
    }

    public ShapedChargeWarhead sw() {
        return this.sw;
    }

    public FragWarhead fw() {
        return this.fw;
    }

    public boolean detonate(Level level, Vec3 hit, Vec3 dir) {
        return (this.sw.detonate(level, hit, dir) && this.fw.detonate(level, hit, dir));
    }

    public void save(CompoundTag nbt) {
        this.sw.save(nbt);
        this.fw.save(nbt);
    }

    public void read(CompoundTag nbt) {
        this.sw.save(nbt);
        this.fw.save(nbt);
    }

    public void appendHoverText(List<Component> components) {
        components.add(Component.literal("Dual Purpose Warhead").setStyle(Style.EMPTY
                .withColor(ChatFormatting.GRAY)));
        components.add(Component.translatable("info.tallyho.tnt", ItemStack.ATTRIBUTE_MODIFIER_FORMAT
                .format((FragExplosion.effectiveBlockDamagePower(this.fw.power(), this.fw.fragPercent()) / 4.0F))).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        components.add(Component.translatable("info.tallyho.lethal_radius", ItemStack.ATTRIBUTE_MODIFIER_FORMAT
                .format(MissileRegistry.lethalRadius(this.fw.power()))).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        components.add(Component.translatable("info.tallyho.shaped_charge", ItemStack.ATTRIBUTE_MODIFIER_FORMAT
                .format(this.sw.power())).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
    }
}