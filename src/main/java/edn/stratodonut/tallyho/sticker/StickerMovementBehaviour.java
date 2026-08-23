package edn.stratodonut.tallyho.sticker;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.gantry.GantryContraption;
import com.simibubi.create.content.contraptions.piston.LinearActuatorBlockEntity;
import com.simibubi.create.content.contraptions.piston.PistonContraption;
import com.simibubi.create.content.contraptions.pulley.PulleyContraption;
import com.simibubi.create.foundation.utility.VecHelper;
import edn.stratodonut.tallyho.mixinducks.MixinAbstractContraptionEntityDuck;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4fc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.clockwork.forge.content.contraptions.sticker.StickerParticleUtil;
import org.valkyrienskies.clockwork.mixin.accessors.IMixinPistonContraption;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;
import org.valkyrienskies.core.apigame.constraints.VSFixedOrientationConstraint;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.mixin.mod_compat.create.blockentity.IMixinMechanicalBearingTileEntity;
import org.valkyrienskies.mod.mixinducks.mod_compat.create.IMixinControlledContraptionEntity;

public class StickerMovementBehaviour implements MovementBehaviour {
    private static final double DISTANCE_BUFFER = 1.05D;

    public boolean isStopped = true;
    private static final Logger LOGGER = LogManager.getLogger("Clockwork.StickerMovementBehaviour");

    public boolean renderAsNormalBlockEntity() {
        return true;
    }

    public void tick(MovementContext context) {
        if (context.world == null || context.world.isClientSide)
            return;
        CompoundTag extraData = context.blockEntityData.getCompound("ForgeData");
        if (!extraData.isEmpty() && extraData.contains("ShipStickerConstraint")) {
            if (!this.isStopped)
                doUpdateConstraint(context, null, null);
        } else if (context.state.getValue(BlockStateProperties.POWERED)) {
            context.blockEntityData.put("ForgeData", new CompoundTag());
            isAttachedToShipOrWorld(true, context.world, VectorConversionsMCKt.toJOML(context.position), VectorConversionsMCKt.toJOML(context.rotation.apply(Vec3.atLowerCornerOf(context.state.getValue(DirectionalBlock.FACING).getNormal()))), context.blockEntityData.getCompound("ForgeData"));
        }
    }

    @Unique
    @Nullable
    Direction.Axis getFacingAxis(MovementContext context) {
        Direction.Axis axis = null;
        Contraption contraption = context.contraption;
        if (contraption instanceof PistonContraption) {
            PistonContraption pistonContraption = (PistonContraption)contraption;
            axis = ((IMixinPistonContraption)pistonContraption).getOrientation().getAxis();
        }
        contraption = context.contraption;
        if (contraption instanceof PulleyContraption) {
            axis = Direction.Axis.Y;
        }
        contraption = context.contraption;
        if (contraption instanceof GantryContraption) {
            GantryContraption gantryContraption = (GantryContraption)contraption;
            axis = gantryContraption.getFacing().getAxis();
        }
        return axis;
    }

    public static Vector3d getAdjustedDirNormal(Vector3d myDirNormal) {
        return (new Vector3d((Vector3dc)myDirNormal)).mul(1.0D);
    }

    public void onSpeedChanged(MovementContext context, Vec3 oldMotion, Vec3 motion) {
        Direction.Axis axis = getFacingAxis(context);
        if (axis != null) {
            float axisMotion = Math.abs(VecHelper.getCoordinate(motion, axis));
            this.isStopped = (axisMotion < 0.001D);
        } else {
            this.isStopped = motion.equals(Vec3.ZERO);
        }
        new Vector3d(1.0D, 45.0D, 1.0D);
    }

    @Unique
    private boolean getAssembleNextTick(MovementContext context) {
        boolean result = false;
        if (context.contraption.entity instanceof com.simibubi.create.content.contraptions.ControlledContraptionEntity) {
            if (context.contraption instanceof com.simibubi.create.content.contraptions.TranslatingContraption)
                result = ((LinearActuatorBlockEntity)((IMixinControlledContraptionEntity)context.contraption.entity).grabController()).assembleNextTick;
            if (context.contraption instanceof BearingContraption || context.contraption instanceof StabilizedContraption)
                result = ((IMixinMechanicalBearingTileEntity)((IMixinControlledContraptionEntity)context.contraption.entity).grabController()).isAssembleNextTick();
        }
        return result;
    }

    public void startMoving(MovementContext context) {
        this.isStopped = false;
    }

    public void stopMoving(MovementContext context) {
        this.isStopped = true;
        Vector3d position = null;
        Quaterniond quaterniond = null;
        CompoundTag extraData = context.blockEntityData.getCompound("ForgeData");
        double distance = 1.05D;
        if (extraData.contains("ShipStickerDistance"))
            distance = extraData.getDouble("ShipStickerDistance");
        Direction myDir = context.state.getValue(DirectionalBlock.FACING);
        Vec3 myDirNormal = VectorConversionsMCKt.toMinecraft((Vector3dc)VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(myDir.getNormal())).mul(1.0D));
        if (!getAssembleNextTick(context)) {
            if (context.state.getValue(BlockStateProperties.POWERED))
                extraData.putBoolean("ShipStickerAlreadyPowered", true);
            StructureTransform structureTransform = ((MixinAbstractContraptionEntityDuck)context.contraption.entity).getStructureTransform();
            position = VectorConversionsMCKt.toJOML(Vec3.atCenterOf((Vec3i)structureTransform.apply(context.localPos)).add(structureTransform.applyWithoutOffsetUncentered(myDirNormal)));
            if (distance < 1.05D)
                position.add((Vector3dc)VectorConversionsMCKt.toJOML(structureTransform.applyWithoutOffsetUncentered(VectorConversionsMCKt.toMinecraft((Vector3dc)VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(myDir.getNormal())).mul(distance / -1.0D + 1.05D)))));
            extraData.put("ShipStickerShip1Vec", (Tag)writeVector3D(position));
            Contraption contraption = context.contraption;
            if (contraption instanceof BearingContraption) {
                BearingContraption bearingContraption = (BearingContraption)contraption;
                Quaterniond tempQuat = VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf((Vec3i)structureTransform.applyWithoutOffset(context.localPos))).rotationTo((Vector3dc)VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf((Vec3i)context.localPos)), new Quaterniond());
                quaterniond = new Quaterniond();
                tempQuat.mul((Quaterniondc)readQuatd(extraData.getCompound("ShipStickerShip1Quat")), quaterniond);
                extraData.put("ShipStickerShip1Quat", (Tag)writeQuatd(quaterniond));
            }
        } else {
            Vector3d tempShip1Pos = VectorConversionsMCKt.toJOML(context.position.add(context.rotation.apply(myDirNormal)));
            extraData.put("ShipStickerShip1Vec", (Tag)writeVector3D(tempShip1Pos));
        }
        if (!extraData.isEmpty() && extraData.contains("ShipStickerConstraint"))
            doUpdateConstraint(context, position, quaterniond);
    }

    static void doUpdateConstraint(MovementContext context, @Nullable Vector3d ship1Pos, @Nullable Quaterniond ship1Rot) {
        if (context.world.isClientSide)
            return;
        Ship ship1 = null;
        Ship ship2 = null;
        double distance = 1.05D;
        CompoundTag compoundTag = context.blockEntityData.getCompound("ForgeData");
        if (compoundTag.contains("ShipStickerConstraint")) {
            Vec3 myDirNormal;
            Vector3d ship2Pos = null;
            Quaterniond ship2Rot = null;
            if (compoundTag.contains("ShipStickerDistance"))
                distance = compoundTag.getDouble("ShipStickerDistance");
            if (compoundTag.contains("ShipStickerShip1Id"))
                ship1 = VSGameUtilsKt.getShipObjectWorld(context.world).getAllShips().getById(compoundTag.getLong("ShipStickerShip1Id"));
            if (compoundTag.contains("ShipStickerShip2Id"))
                ship2 = VSGameUtilsKt.getShipObjectWorld(context.world).getAllShips().getById(compoundTag.getLong("ShipStickerShip2Id"));
            if (compoundTag.contains("ShipStickerShip2Vec"))
                ship2Pos = new Vector3d((Vector3dc)readVector3D(compoundTag.getCompound("ShipStickerShip2Vec")));
            if (compoundTag.contains("ShipStickerShip2Quat"))
                ship2Rot = new Quaterniond((Quaterniondc)readQuatd(compoundTag.getCompound("ShipStickerShip2Quat")));
            if (ship1 == null && ship2 == null)
                return;

            Direction myDir = context.state.getValue(DirectionalBlock.FACING);

            if (ship1 != null && ship2 != null) {
                myDirNormal = VectorConversionsMCKt.toMinecraft((Vector3dc)VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(myDir.getNormal())).mul(1.0D));
            } else {
                myDirNormal = VectorConversionsMCKt.toMinecraft((Vector3dc)VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(myDir.getNormal())).mul(1.0D));
            }
            if (ship1Pos == null) {
                if (compoundTag.contains("ShipStickerShip1Vec"))
                    ship1Pos = new Vector3d((Vector3dc)readVector3D(compoundTag.getCompound("ShipStickerShip1Vec")));
                ship1Pos = VectorConversionsMCKt.toJOML(context.position.add(context.rotation.apply(myDirNormal)));
                Contraption contraption = context.contraption;
                if (contraption instanceof StabilizedContraption) {
                    StabilizedContraption stabilizedContraption = (StabilizedContraption)contraption;
                    ship1Pos.add((Vector3dc)VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(stabilizedContraption.getFacing().getNormal())).mul(0.125D));
                }
                ship1Pos.add((Vector3dc)VectorConversionsMCKt.toJOML(context.motion));
            } else {
                compoundTag.put("ShipStickerShip1Vec", (Tag)writeVector3D(ship1Pos));
            }
            if (ship1Rot == null && compoundTag.contains("ShipStickerShip1Quat")) {
                ship1Rot = new Quaterniond((Quaterniondc)readQuatd(compoundTag.getCompound("ShipStickerShip1Quat")));
                AbstractContraptionEntity.ContraptionRotationState rotState = context.contraption.entity.getRotationState();
                ship1Rot = (new Quaterniond()).setFromNormalized((Matrix4fc)rotState.asMatrix().getAsMatrix4f()).mul((Quaterniondc)ship1Rot);
            }
            Pair<VSAttachmentConstraint, VSFixedOrientationConstraint> constraints = makeConstraint(ship1Pos, new Vector3d((Vector3dc)ship1Pos), ship1, ship2, (ServerLevel)context.world, ship1Rot, ship2Rot, ship2Pos);
            ServerShipWorldCore shipWorld = VSGameUtilsKt.getShipObjectWorld((ServerLevel)context.world);
            shipWorld.removeConstraint(compoundTag.getInt("ShipStickerConstraint"));
            shipWorld.removeConstraint(compoundTag.getInt("ShipStickerConstraint2"));
            Integer constraintId = shipWorld.createNewConstraint((VSConstraint)constraints.getFirst());
            Integer constraintId2 = shipWorld.createNewConstraint((VSConstraint)constraints.getSecond());
            compoundTag.putInt("ShipStickerConstraint", constraintId);
            compoundTag.putInt("ShipStickerConstraint2", constraintId2);
        }
    }

    @Unique
    private static Pair<VSAttachmentConstraint, VSFixedOrientationConstraint> makeConstraint(Vector3d ship1ConstraintPos, Vector3d ship2ConstraintPos, Ship ship1, Ship ship2, ServerLevel level, @Nullable Quaterniond ship1Rot, @Nullable Quaterniond ship2Rot, @Nullable Vector3d ship2Pos) {
        long ship1Id, ship2Id;
        if (ship1 == null && ship2 == null)
            return null;
        long groundId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable().get(VSGameUtilsKt.getDimensionId((Level)level));
        if (ship1Rot == null && ship1 == null)
            ship1Rot = new Quaterniond();
        if (ship2Rot == null && ship2 == null)
            ship2Rot = new Quaterniond();
        double mass = 100.0D;
        if (ship1 != null) {
            ship1.getShipToWorld().transformPosition(ship2ConstraintPos);
            ship1Id = ship1.getId();
            if (ship1Rot == null)
                ship1Rot = ship1.getTransform().getShipToWorldRotation().invert(new Quaterniond());
            mass = Math.min(mass, ((ServerShip)VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(ship1Id)).getInertiaData().getMass());
        } else {
            ship1Id = groundId;
        }
        if (ship2 != null) {
            ship2.getWorldToShip().transformPosition(ship2ConstraintPos);
            ship2Id = ship2.getId();
            if (ship2Rot == null)
                ship2Rot = ship2.getTransform().getShipToWorldRotation().invert(new Quaterniond());
            mass = Math.min(mass, ((ServerShip)VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(ship2Id)).getInertiaData().getMass());
        } else {
            ship2Id = groundId;
        }
        if (ship2Pos != null)
            ship2ConstraintPos = ship2Pos;
        Quaterniond quaterniond1 = ship1Rot;
        Quaterniond quaterniond2 = ship2Rot;
        VSAttachmentConstraint constraint = new VSAttachmentConstraint(ship1Id, ship2Id, 1.0E-9D / mass, (Vector3dc)ship1ConstraintPos, (Vector3dc)ship2ConstraintPos, 1.0E10D, 0.001D);
        VSFixedOrientationConstraint o_constraint = new VSFixedOrientationConstraint(ship1Id, ship2Id, 1.0E-9D / mass, (Quaterniondc)quaterniond1, (Quaterniondc)quaterniond2, 1.0E8D);
        return new Pair<>(constraint, o_constraint);
    }

    static Vector3d readVector3D(CompoundTag compoundTag) {
        return new Vector3d(compoundTag.getDouble("x"), compoundTag.getDouble("y"), compoundTag.getDouble("z"));
    }

    static CompoundTag writeVector3D(Vector3d vector3d) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putDouble("x", vector3d.x);
        compoundTag.putDouble("y", vector3d.y);
        compoundTag.putDouble("z", vector3d.z);
        return compoundTag;
    }

    static Quaterniond readQuatd(CompoundTag compoundTag) {
        return new Quaterniond(compoundTag.getDouble("x"), compoundTag.getDouble("y"), compoundTag.getDouble("z"), compoundTag.getDouble("w"));
    }

    static CompoundTag writeQuatd(Quaterniond quaterniond) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putDouble("x", quaterniond.x);
        compoundTag.putDouble("y", quaterniond.y);
        compoundTag.putDouble("z", quaterniond.z);
        compoundTag.putDouble("w", quaterniond.w);
        return compoundTag;
    }

    public static boolean isAttachedToShipOrWorld(boolean attach, Level level, Vector3d myPosCentered, Vector3d myDirNormal, CompoundTag compoundTag) {
        boolean result = false;
        if (level == null)
            return false;
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, (Vector3dc)myPosCentered);
        Ship ship2 = null;
        Vector3d tempDirNormal = getAdjustedDirNormal(myDirNormal);
        Vector3d searchPos = (new Vector3d((Vector3dc)myPosCentered)).add((Vector3dc)tempDirNormal);
        if (ship != null)
            ship.getShipToWorld().transformPosition((Vector3dc)searchPos, searchPos);
        Vec3 spos = VectorConversionsMCKt.toMinecraft((Vector3dc)searchPos);
        BlockPos searchBlockPos = new BlockPos((int)spos.x, (int)spos.y, (int)spos.z);
        BlockState worldBlockState = level.getBlockState(searchBlockPos);
        double distance = 0.0D;
        if (!worldBlockState.isAir()) {
            distance = Vector3d.distance(myPosCentered.x, myPosCentered.y, myPosCentered.z, searchBlockPos.getX(), searchBlockPos.getY(), searchBlockPos.getZ());
            result = true;
        } else {
            double bounds = 0.5D;
            AABB searchAABB = new AABB(searchPos.x - bounds, searchPos.y - bounds, searchPos.z - bounds, searchPos.x + bounds, searchPos.y + bounds, searchPos.z + bounds);
            Iterator<Ship> ships = VSGameUtilsKt.getShipsIntersecting(level, searchAABB).iterator();
            Vector3d transformedSearchPos = new Vector3d((Vector3dc)searchPos);
            if (ships.hasNext())
                do {
                    Ship shipItr = ships.next();
                    if (shipItr == ship)
                        continue;
                    shipItr.getWorldToShip().transformPosition(transformedSearchPos);
                    Vec3 tpos = VectorConversionsMCKt.toMinecraft((Vector3dc)transformedSearchPos);
                    BlockPos blockPos = new BlockPos((int)tpos.x, (int)tpos.y, (int)tpos.z);
                    if (!VSGameUtilsKt.isBlockInShipyard(level, blockPos))
                        continue;
                    BlockState blockState = level.getBlockState(blockPos);
                    if (blockState.isAir() || !blockState.isFaceSturdy((BlockGetter)level, blockPos, Direction.UP, SupportType.RIGID))
                        continue;
                    Vec3 sbpos = VectorConversionsMCKt.toMinecraft((Vector3dc)shipItr.getShipToWorld().transformPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ(), new Vector3d()));
                    searchBlockPos = new BlockPos((int)sbpos.x, (int)sbpos.y, (int)sbpos.z);
                    distance = Vector3d.distance(myPosCentered.x, myPosCentered.y, myPosCentered.z, searchBlockPos.getX(), searchBlockPos.getY(), searchBlockPos.getZ());
                    result = true;
                    ship2 = shipItr;
                } while (ships.hasNext() && !result);
        }
        if (result && !level.isClientSide && attach)
            doAttach((ServerLevel)level, ship, ship2, myPosCentered, myDirNormal, compoundTag, distance);
        return result;
    }

    public static void doAttach(ServerLevel level, Ship ship1, Ship ship2, Vector3d myPos, Vector3d myDirNormal, CompoundTag compoundTag, double distance) {
        if (ship1 == null && ship2 == null)
            return;
        removeConstraint(level, false, compoundTag);
        Vector3d adjustedDirNormal = getAdjustedDirNormal(myDirNormal);
        Vector3d ship1Pos = (new Vector3d((Vector3dc)myPos)).add((Vector3dc)adjustedDirNormal);
        Vector3d ship2ConstraintPos = new Vector3d((Vector3dc)ship1Pos);
        Vector3d ship2Pos = null;
        Quaterniond ship1Rot = null;
        Quaterniond ship2Rot = null;
        if (compoundTag.contains("ShipStickerConstraint")) {
            if (compoundTag.contains("ShipStickerDistance"))
                distance = compoundTag.getDouble("ShipStickerDistance");
            if (compoundTag.contains("ShipStickerShip1Id"))
                ship1 = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(compoundTag.getLong("ShipStickerShip1Id"));
            if (compoundTag.contains("ShipStickerShip1Vec"))
                ship1Pos = new Vector3d((Vector3dc)readVector3D(compoundTag.getCompound("ShipStickerShip1Vec")));
            if (compoundTag.contains("ShipStickerShip1Quat"))
                ship1Rot = new Quaterniond((Quaterniondc)readQuatd(compoundTag.getCompound("ShipStickerShip1Quat")));
            if (compoundTag.contains("ShipStickerShip2Id"))
                ship2 = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(compoundTag.getLong("ShipStickerShip2Id"));
            if (compoundTag.contains("ShipStickerShip2Vec"))
                ship2Pos = new Vector3d((Vector3dc)readVector3D(compoundTag.getCompound("ShipStickerShip2Vec")));
            if (compoundTag.contains("ShipStickerShip2Quat"))
                ship2Rot = new Quaterniond((Quaterniondc)readQuatd(compoundTag.getCompound("ShipStickerShip2Quat")));
        }
        Pair<VSAttachmentConstraint, VSFixedOrientationConstraint> constraints = makeConstraint(ship1Pos, ship2ConstraintPos, ship1, ship2, level, ship1Rot, ship2Rot, ship2Pos);
        if (constraints.getFirst() != null && constraints.getSecond() != null && adjustedDirNormal != null) {
            VSAttachmentConstraint constraint = constraints.getFirst();
            VSFixedOrientationConstraint constraint2 = constraints.getSecond();
            long groundId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable().get(VSGameUtilsKt.getDimensionId((Level)level));
            if (constraint.getShipId0() != groundId)
                compoundTag.putLong("ShipStickerShip1Id", constraint.getShipId0());
            if (constraint.getShipId1() != groundId)
                compoundTag.putLong("ShipStickerShip2Id", constraint.getShipId1());
            compoundTag.put("ShipStickerShip1Vec", (Tag)writeVector3D((Vector3d)constraint.getLocalPos0()));
            compoundTag.put("ShipStickerShip1Quat", (Tag)writeQuatd((Quaterniond)constraint2.getLocalRot0()));
            compoundTag.put("ShipStickerShip2Vec", (Tag)writeVector3D((Vector3d)constraint.getLocalPos1()));
            compoundTag.put("ShipStickerShip2Quat", (Tag)writeQuatd((Quaterniond)constraint2.getLocalRot1()));
            compoundTag.putDouble("ShipStickerDistance", distance);
            Integer constraintID = VSGameUtilsKt.getShipObjectWorld(level).createNewConstraint((VSConstraint)constraint);
            Integer constraintID2 = VSGameUtilsKt.getShipObjectWorld(level).createNewConstraint((VSConstraint)constraint2);
            compoundTag.putInt("ShipStickerConstraint", constraintID);
            compoundTag.putInt("ShipStickerConstraint2", constraintID2);
            Vec3 mPos = VectorConversionsMCKt.toMinecraft((Vector3dc)myPos);
            (new StickerParticleUtil()).doBluperParticle((Level)level, new BlockPos((int)mPos.x, (int)mPos.y, (int)mPos.z), Direction.getNearest(adjustedDirNormal.x, adjustedDirNormal.y, adjustedDirNormal.z));
        }
    }

    public static void removeConstraint(@Nullable ServerLevel level, boolean removeTags, CompoundTag compoundTag) {
        if (compoundTag.contains("ShipStickerConstraint")) {
            if (level != null) {
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(compoundTag.getInt("ShipStickerConstraint"));
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(compoundTag.getInt("ShipStickerConstraint2"));
            }
            if (removeTags) {
                compoundTag.remove("ShipStickerConstraint");
                compoundTag.remove("ShipStickerShip1Id");
                compoundTag.remove("ShipStickerShip1Vec");
                compoundTag.remove("ShipStickerShip1Quat");
                compoundTag.remove("ShipStickerShip2Id");
                compoundTag.remove("ShipStickerShip2Vec");
                compoundTag.remove("ShipStickerShip2Quat");
            }
        }
    }
}