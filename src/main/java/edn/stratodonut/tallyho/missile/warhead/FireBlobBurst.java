package edn.stratodonut.tallyho.missile.warhead;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.FluidBlobBurst;

public class FireBlobBurst extends FluidBlobBurst {
    public FireBlobBurst(EntityType<? extends FireBlobBurst> entityType, Level level) {
        super(entityType, level);
    }

    protected void setBlobSize(byte size) {
        super.setBlobSize(size);
    }
}
