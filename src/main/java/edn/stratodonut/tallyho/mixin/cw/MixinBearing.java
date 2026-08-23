package edn.stratodonut.tallyho.mixin.cw;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import edn.stratodonut.tallyho.block.IJackableBearing;
import edn.stratodonut.tallyho.mixinducks.MixinBearingControllerDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.clockwork.content.contraptions.phys.bearing.PhysBearingBlockEntity;
import org.valkyrienskies.clockwork.content.contraptions.phys.bearing.data.PhysBearingData;
import org.valkyrienskies.clockwork.content.contraptions.phys.bearing.data.PhysBearingUpdateData;
import org.valkyrienskies.clockwork.content.forces.contraption.BearingController;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;
import org.valkyrienskies.core.apigame.constraints.VSHingeOrientationConstraint;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin({PhysBearingBlockEntity.class})
public abstract class MixinBearing extends GeneratingKineticBlockEntity implements IJackableBearing {
    @Shadow(remap = false)
    private Integer bearingID;

    @Shadow(remap = false)
    private long shiptraptionID;

    @Shadow(remap = false)
    private boolean isRunning;

    @Unique
    private Vec3 attachOffset = Vec3.ZERO;

    @Unique
    float clockworkdev$lazyFixTickCounter;

    public MixinBearing(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.clockworkdev$lazyFixTickCounter = 0.0F;
    }

    public void clockworkdev$setOffset(Direction.Axis plane, Vec3 offset) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel slevel = (ServerLevel)level;
            float x = (float)Mth.clamp(offset.x(), -0.5D, 0.5D);
            float y = (float)Mth.clamp(offset.y(), -0.5D, 0.5D);
            float z = (float)Mth.clamp(offset.z(), -0.5D, 0.5D);
            ServerShip s = VSGameUtilsKt.getShipObjectWorld(slevel).getAllShips().getById(this.shiptraptionID);
            if (s == null || this.bearingID == null)
                return;
            BearingController controller = BearingController.Companion.getOrCreate(s);
            if (controller == null)
                return;
            PhysBearingData bearingData = (PhysBearingData)controller.getBearingData().get(this.bearingID);
            if (bearingData == null)
                return;
            VSAttachmentConstraint attachConstraint = bearingData.getAttachConstraint();
            Integer attachId = bearingData.getAttachID();
            Integer secondAttachId = bearingData.getSecondAttachId();
            if (attachConstraint == null || attachId == null || secondAttachId == null || attachId == -1 || secondAttachId == -1)
                return;
            Direction bearingDirection = getBlockState().getValue(BearingBlock.FACING);
            Vector3f bearingDir = bearingDirection.step().mul(0.475F);
            Vector3d vector3d1 = VectorConversionsMCKt.toJOML(Vec3.atCenterOf((Vec3i)getBlockPos()));
            Vector3d vector3d2 = attachConstraint.getLocalPos1().sub((Vector3dc)vector3d1, new Vector3d());
            Vector3d newVec;
            switch (plane) {
                case X:
                    newVec = new Vector3d(x, y, vector3d2.z());
                    break;
                case Y:
                    newVec = new Vector3d(x, y, vector3d2.z());
                    break;
                case Z:
                    newVec = new Vector3d(x, y, vector3d2.z());
                    break;
                default:
                    throw new IncompatibleClassChangeError();
            }
            newVec.add(VectorConversionsMCKt.toJOML(Vec3.atCenterOf((Vec3i)getBlockPos())));
            if (plane != bearingDirection.getAxis())
                newVec.add(bearingDir);
            Vector3d vector3d3 = newVec.sub(attachConstraint.getLocalPos1());
            VSAttachmentConstraint newAttachConstraint = attachConstraint.offsetLocalPositions((Vector3dc)new Vector3d(0.0D, 0.0D, 0.0D), (Vector3dc)vector3d3);
            VSAttachmentConstraint newAttachConstraint2 = attachConstraint.offsetLocalPositions((Vector3dc)new Vector3d(0.0D, 0.0D, 0.0D), (Vector3dc)vector3d3);
            ServerShipWorldCore world = VSGameUtilsKt.getShipObjectWorld(slevel);
            Integer newAttach1 = world.createNewConstraint(newAttachConstraint);
            Integer newAttach2 = world.createNewConstraint(newAttachConstraint2);
            if (newAttach1 != null && newAttach2 != null) {
                world.removeConstraint(attachId);
                world.removeConstraint(secondAttachId);
                bearingData.setAttachConstraint(newAttachConstraint);
                bearingData.setSecondAttachConstraint(newAttachConstraint2);
                bearingData.setAttachID(newAttach1);
                bearingData.setSecondAttachId(newAttach2);
            } else {
                if (newAttach1 != null)
                    world.removeConstraint(newAttach1);
                if (newAttach2 != null)
                    world.removeConstraint(newAttach2);
            }
        }
    }

    @Inject(method = {"tick()V"}, at = {@At("RETURN")}, remap = false)
    public void mixinTick(CallbackInfo ci) {
        if (!this.level.isClientSide() && this.isRunning) {
            this.clockworkdev$lazyFixTickCounter = ++this.clockworkdev$lazyFixTickCounter % 20.0F;
            if (this.clockworkdev$lazyFixTickCounter != 0.0F)
                return;
            if (this.shiptraptionID != -1L) {
                ServerLevel slevel = (ServerLevel)this.level;
                ServerShip s = VSGameUtilsKt.getShipObjectWorld(slevel).getAllShips().getById(this.shiptraptionID);
                if (s == null)
                    return;
                BearingController controller = BearingController.Companion.getOrCreate(s);
                if (controller == null)
                    return;
                PhysBearingData bearingData = controller.getBearingData().get(this.bearingID);
                if (bearingData == null)
                    return;
                VSAttachmentConstraint attachConstraint = bearingData.getAttachConstraint();
                Integer attachId = bearingData.getAttachID();
                VSHingeOrientationConstraint hingeConstraint = bearingData.getHingeConstraint();
                Integer hingeId = bearingData.getHingeID();
                if (attachConstraint == null || hingeConstraint == null)
                    return;
                if (attachId == null || hingeId == null)
                    return;
                ServerShipWorldCore world = VSGameUtilsKt.getShipObjectWorld(slevel);
                if (!world.updateConstraint(attachId, attachConstraint)) {
                    Integer f = world.createNewConstraint(attachConstraint);
                    if (f != null)
                        bearingData.setAttachID(f);
                }
                if (!world.updateConstraint(hingeId, hingeConstraint)) {
                    Integer h = world.createNewConstraint(hingeConstraint);
                    if (h != null)
                        bearingData.setHingeID(h);
                }
            }
        }
    }

    @WrapOperation(method = {"tick()V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;")})
    private Comparable mixinDumbfixDirection(BlockState instance, Property property, Operation<Comparable> original) {
        return Direction.UP;
    }

    @WrapOperation(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lorg/valkyrienskies/clockwork/content/forces/contraption/BearingController;updatePhysBearing(ILorg/valkyrienskies/clockwork/content/contraptions/phys/bearing/data/PhysBearingUpdateData;)V")}, remap = false)
    private void man(BearingController instance, int id, PhysBearingUpdateData data, Operation<Void> original) {
        Direction direction = getBlockState().getValue(BearingBlock.FACING);

        ((MixinBearingControllerDuck) (Object) instance).tyh$updateIsFacingNegativeDirection((direction == Direction.DOWN || direction == Direction.WEST || direction == Direction.NORTH));

        original.call(instance, id, data);
    }
}