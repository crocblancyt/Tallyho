package edn.stratodonut.tallyho;

import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class Utils {
    public record PosVel(Vec3 pos, Vec3 vel) { }

    public static PosVel moveToWorld(Vec3 spawnPos, Ship s, float margin) {
        spawnPos = VSGameUtilsKt.toWorldCoordinates(s, spawnPos);
        return moveInWorld(spawnPos, s, margin);
    }

    public static PosVel moveInWorld(Vec3 spawnPos, Ship s, float margin) {
        Vec3 vel = VectorConversionsMCKt.toMinecraft(s.getVelocity()).add(VectorConversionsMCKt.toMinecraft(s.getOmega()).cross(spawnPos.subtract(VectorConversionsMCKt.toMinecraft(s.getTransform().getPositionInWorld())))).scale(0.05D);
        return new PosVel(spawnPos.add(vel.scale(margin)), vel);
    }
}