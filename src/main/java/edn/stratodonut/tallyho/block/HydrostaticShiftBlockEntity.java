package edn.stratodonut.tallyho.block;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;

public class HydrostaticShiftBlockEntity extends SplitShaftBlockEntity implements IHaveGoggleInformation {
    protected int power;
    protected int lastPower;

    public HydrostaticShiftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        this.lastPower = this.power;
        this.power = getPower(this.level, this.worldPosition);
        if (this.power != this.lastPower) {
            detachKinetics();
        }
    }

    public void detachKinetics() {
        if (this.level == null) return;
        RotationPropagator.handleRemoved(this.level, this.worldPosition, (KineticBlockEntity) this);
        this.level.scheduleTick(this.worldPosition, getBlockState().getBlock(), 0, TickPriority.EXTREMELY_HIGH);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        compound.putInt("Power", this.power);
        compound.putInt("LastPower", this.lastPower);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        this.power = compound.getInt("Power");
        this.lastPower = compound.getInt("LastPower");
    }

    public int getPower(Level level, BlockPos pos) {
        return level.getBestNeighborSignal(pos);
    }

    public int getPower() {
        return this.power;
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource() && face != getSourceFacing()) {
            return this.power / 15.0F;
        }
        return 1.0F;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(componentSpacing.plainCopy().append(
                Component.literal("Analog Input: " + this.power)));
        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }
}