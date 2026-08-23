package edn.stratodonut.tallyho.camera.item;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.camera.entity.CameraEntity2;
import edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class RemoteCameraItem extends Item {

    public RemoteCameraItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("SelectedRemote")) {
            UUID conductorId = tag.getUUID("SelectedRemote");
            tooltip.add(Components.translatable("railways.whistle.tool.bound").withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Components.translatable("railways.whistle.tool.conductor_id", conductorId.toString().substring(0, 5)));
            tooltip.add(Components.translatable("railways.remote_lens.tool.bound_usage"));
        } else {
            tooltip.add(Components.translatable("railways.remote_lens.tool.not_bound").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (stack.hasCustomHoverName() && stack.getOrCreateTag().contains("SelectedRemote")) {
            return this.use(level, context.getPlayer(), context.getHand()).getResult();
        }

        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        }

        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            Player player = context.getPlayer();

            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                Direction side = context.getClickedFace();
                Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);

                Vector3d lookInShip = VectorConversionsMCKt.toJOML(serverPlayer.getViewVector(1.0F));
                if (ship != null) {
                    lookInShip = ship.getWorldToShip().transformDirection(lookInShip);
                }

                RemoteCameraEntity remoteCameraEntity = RemoteCameraEntity.spawn(
                    serverLevel,
                    pos.relative(side),
                    Direction.getNearest(lookInShip.x, 0.0D, lookInShip.z).toYRot()
                );

                CompoundTag nbt = stack.getOrCreateTag();
                nbt.putUUID("SelectedRemote", remoteCameraEntity.getUUID());
                stack.setTag(nbt);

                serverPlayer.setItemInHand(context.getHand(), stack);
                serverPlayer.displayClientMessage(Components.translatable("railways.remote_lens.set"), true);

                AllSoundEvents.PECULIAR_BELL_USE.play(
                    serverPlayer.level(), null,
                    remoteCameraEntity.getX(), remoteCameraEntity.getY(), remoteCameraEntity.getZ(),
                    0.5F, 1.1F
                );

                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;

            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                CompoundTag stackTag = stack.getTag();

                if (stackTag != null && stackTag.contains("SelectedRemote")) {
                    UUID cameraId = stackTag.getUUID("SelectedRemote");

                    if (player.isShiftKeyDown()) {
                        stackTag.remove("SelectedRemote");
                        AllSoundEvents.CONTROLLER_CLICK.play(level, null, player.blockPosition(), 0.5F, 1.1F);
                        player.displayClientMessage(Components.translatable("railways.remote_lens.clear"), true);
                        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                    }

                    Entity entity = serverLevel.getEntity(cameraId);
                    if (entity instanceof CameraEntity2) {
                        CameraEntity2 camera = (CameraEntity2) entity;
                        if (camera.startViewing(serverPlayer)) {
                            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                        } else {
                            return InteractionResultHolder.fail(stack);
                        }
                    }

                    player.displayClientMessage(Component.literal("No connection to camera!"), true);
                    return InteractionResultHolder.fail(stack);
                }

                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}