package edn.stratodonut.tallyho.missile.warhead;

import edn.stratodonut.tallyho.missile.IWarhead;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class TopAttackWarhead implements IWarhead {
    private final float power;
    private final float distance;

    public TopAttackWarhead(float power, float distance) {
        this.power = power;
        this.distance = distance;
    }

    public float power() {
        return this.power;
    }

    public float distance() {
        return this.distance;
    }

    public boolean detonate(Level world, Vec3 origin, Vec3 direction) {
        Vec3 dir = new Vec3(Direction.DOWN.step());
        ShapedChargeWarhead warhead = new ShapedChargeWarhead(this.power, this.distance);
        warhead.detonate(world, origin, dir);
        return true;
    }

    public void save(CompoundTag nbt) {}

    public void read(CompoundTag nbt) {}

    public void appendHoverText(List<Component> components) {
        components.add(Component.literal("Top Attack Warhead").setStyle(Style.EMPTY
                .withColor(ChatFormatting.GRAY)));
        components.add(Component.translatable("info.tallyho.shaped_charge", ItemStack.ATTRIBUTE_MODIFIER_FORMAT
                .format(this.power)).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
    }
}