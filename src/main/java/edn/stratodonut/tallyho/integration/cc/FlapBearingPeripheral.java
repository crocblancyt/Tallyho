package edn.stratodonut.tallyho.integration.cc;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.valkyrienskies.clockwork.content.contraptions.flap.FlapBearingBlockEntity;

public class FlapBearingPeripheral implements IPeripheral {
    private final Level level;
    private final BlockPos pos;
    private final FlapBearingBlockEntity flap;

    public FlapBearingPeripheral(FlapBearingBlockEntity be) {
        this.flap = be;
        this.level = be.getLevel();
        this.pos = be.getBlockPos();
    }

    @Nonnull
    public String getType() {
        return "flap_bearing";
    }

    public boolean equals(@Nullable IPeripheral iPeripheral) {
        return (this.level != null && this.level.getBlockEntity(this.pos) instanceof FlapBearingBlockEntity);
    }

    @LuaFunction
    public final void setFlapAngle(double angle) {
        this.flap.setAngle((float)angle);
    }

    @LuaFunction
    public final double getFlapSpeed() {
        return this.flap.getFlapSpeed();
    }

    @LuaFunction
    public final double getAngularSpeed() {
        return this.flap.getAngularSpeed();
    }

    @LuaFunction
    public final boolean isValid() {
        return this.flap.isValid();
    }
}