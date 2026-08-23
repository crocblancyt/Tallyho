package edn.stratodonut.tallyho.missile.fuze;

import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.FuzeComponent;
import edn.stratodonut.tallyho.missile.IFuzeFactory;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TopAttackFuze implements FuzeComponent {
    private static final int ARMING_TICKS = 30;

    public final float fuzeDist;
    private final float delayDist;
    private Double detDistanceTicker;

    public TopAttackFuze(float fuzeDist, float delayDist) {
        this.fuzeDist = fuzeDist;
        this.delayDist = delayDist;
    }

    public static class Factory implements IFuzeFactory<TopAttackFuze> {
        public final float fuzeDist;
        private final float delayDist;

        public Factory(float fuzeDist, float delayDist) {
            this.fuzeDist = fuzeDist;
            this.delayDist = delayDist;
        }

        public TopAttackFuze create() {
            return new TopAttackFuze(this.fuzeDist, this.delayDist);
        }

        public void appendHoverText(List<Component> components) {}
    }

    public Optional<Vec3> tick(MountedMissileEntity missile, Level level, Vec3 pos, Vec3 deltaPos) {
        if (missile.level().isClientSide)
            return Optional.empty();
        if (missile.getTicksSinceLaunch() < 30)
            return Optional.empty();
        Vec3 lastPos = pos.subtract(deltaPos);
        if (this.detDistanceTicker == null) {
            AABB aabb = missile.getBoundingBox().move(0.0D, -4.0D, 0.0D).move(deltaPos.scale(-1.0D));
            Vec3 newDel = missile.collideOnlyShips(deltaPos, aabb);
            if (newDel.distanceToSqr(deltaPos) > 0.1D)
                this.detDistanceTicker = newDel.length() + this.delayDist;
        }
        if (this.detDistanceTicker != null)
            if (this.detDistanceTicker > deltaPos.length()) {
                this.detDistanceTicker = this.detDistanceTicker - deltaPos.length();
            } else {
                Vec3 detPos = lastPos.add(deltaPos.normalize().scale(this.detDistanceTicker));
                return Optional.of(detPos);
            }
        return Optional.empty();
    }
}