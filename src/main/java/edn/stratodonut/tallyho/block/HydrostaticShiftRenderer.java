package edn.stratodonut.tallyho.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.valkyrienskies.clockwork.ClockworkPartials;

public class HydrostaticShiftRenderer extends KineticBlockEntityRenderer<HydrostaticShiftBlockEntity> {

    public HydrostaticShiftRenderer(BlockEntityRendererProvider.Context context) {
      super(context);
    }

    @Override
    protected void renderSafe(HydrostaticShiftBlockEntity be, float partialTicks,
                                PoseStack poseStack, MultiBufferSource buffer,
                                int light, int overlay) {

        Block block = be.getBlockState().getBlock();
        Direction.Axis boxAxis = ((IRotate) block).getRotationAxis(be.getBlockState());
        BlockPos pos = be.getBlockPos();
        float time = AnimationTickHolder.getRenderTime(be.getLevel());

        for (Direction direction : Iterate.directions) {
            Direction.Axis axis = direction.getAxis();
            if (boxAxis == axis) {
                float offset = getRotationOffsetForPosition(be, pos, axis);
                float angle = time * be.getSpeed() * 3.0F / 10.0F % 360.0F;
                float modifier = be.getRotationSpeedModifier(direction);
                angle *= modifier;
                angle += offset;
                angle = (float) ((angle / 180.0F) * Math.PI);

                SuperByteBuffer superByteBuffer = CachedBufferer.partialFacing(
                        AllPartialModels.SHAFT_HALF, be.getBlockState(), direction);
                kineticRotationTransform(superByteBuffer, be, axis, angle, light);
                superByteBuffer.renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
            }
        }

        BlockState state = be.getBlockState();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());
        int color = Color.mixColors(0x2C0300, 0xCD0000, be.getPower() / 15.0F);

        SuperByteBuffer indicator = transform(
                CachedBufferer.partial(ClockworkPartials.INSTANCE.getRESISTOR_INDICATOR(), state),
                state);
        indicator.light(light)
                .color(color)
                .renderInto(poseStack, vertexConsumer);
    }

    private SuperByteBuffer transform(SuperByteBuffer buffer, BlockState state) {
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        SuperByteBuffer b = buffer;

        switch (axis) {
            case X:
                b = b.rotateCentered(Direction.NORTH, (float) Math.toRadians(90.0D));
                break;
            case Z:
                b = buffer.rotateCentered(Direction.EAST, (float) Math.toRadians(90.0D));
                break;
            default:
                break;
        }

        return b;
    }
}