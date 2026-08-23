package edn.stratodonut.tallyho.mixin.cbc;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;

@Mixin(value = {ControlPitchContraption.class}, remap = false)
public interface MixinControlPitchContraption {
    @Overwrite
    default void onRecoil(Vec3 vector, AbstractContraptionEntity cannon) {
        if (cannon.level().isClientSide())
            return;
        if (vector.length() == 0.0D)
            return;
        LoadedShip loadedShip = VSGameUtilsKt.getShipObjectManagingPos(cannon.level(), VectorConversionsMCKt.toJOML(cannon.position()));
        if (loadedShip instanceof ServerShip) {
            ServerShip s = (ServerShip)loadedShip;
            GameTickForceApplier gt = s.getAttachment(GameTickForceApplier.class);
            double mass = s.getInertiaData().getMass();
            double recoilForce = vector.length() * 100000.0D;
            Vector3d vector3d = s.getTransform().getShipToWorldRotation().transform(VectorConversionsMCKt.toJOML(vector).normalize()).mul(recoilForce);
            Vec3 centeredPos = cannon.getAnchorVec().add(0.5D, 0.5D, 0.5D);
            gt.applyInvariantForceToPos(vector3d, VectorConversionsMCKt.toJOML(centeredPos).sub(s.getTransform().getPositionInShip()));
        }
    }
}