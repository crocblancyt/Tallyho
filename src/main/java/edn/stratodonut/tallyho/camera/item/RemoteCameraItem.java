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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class RemoteCameraItem extends Item {
   public RemoteCameraItem(Properties p_41383_) {
      super(p_41383_);
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.appendHoverText(stack, level, tooltip, flag);
      CompoundTag tag = stack.getTag();
      if (tag != null && tag.hasUUID("SelectedRemote")) {
         UUID conductorId = tag.getUUID("SelectedRemote");
         tooltip.add(Components.translatable("railways.whistle.tool.bound").withStyle(ChatFormatting.DARK_GREEN));
         tooltip.add(Components.translatable("railways.whistle.tool.conductor_id", new Object[]{conductorId.toString().substring(0, 5)}));
         tooltip.add(Components.translatable("railways.remote_lens.tool.bound_usage"));
      } else {
         tooltip.add(Components.translatable("railways.remote_lens.tool.not_bound").withStyle(ChatFormatting.DARK_RED));
      }
   }

   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      ItemStack stack = ctx.getItemInHand();
      if (stack.hasTag() && stack.getOrCreateTag().hasUUID("SelectedRemote")) {
         return this.use(ctx.getLevel(), ctx.getPlayer(), ctx.getHand()).getResult();
      }

      if (level.isClientSide) {
         return InteractionResult.CONSUME;
      }

      if (level instanceof ServerLevel serverLevel && ctx.getPlayer() instanceof ServerPlayer serverPlayer) {
         Direction side = ctx.getClickedFace();
         Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
         Vector3d lookInShip = VectorConversionsMCKt.toJOML(serverPlayer.getLookAngle());
         if (s != null) {
            lookInShip = s.getWorldToShip().transformDirection(lookInShip);
         }

         CameraEntity2 c = RemoteCameraEntity.spawn(serverLevel, pos.relative(side), Direction.getNearest(lookInShip.x, 0.0, lookInShip.z).toYRot());
         CompoundTag nbt = stack.getOrCreateTag();
         nbt.putUUID("SelectedRemote", c.getUUID());
         stack.setTag(nbt);
         serverPlayer.setItemInHand(ctx.getHand(), stack);
         serverPlayer.displayClientMessage(Components.translatable("railways.remote_lens.set"), true);
         AllSoundEvents.PECULIAR_BELL_USE.play(serverPlayer.level(), (Player)null, c.getX(), c.getY(), c.getZ(), 0.5F, 1.1F);
         return InteractionResult.SUCCESS;
      } else {
         return super.useOn(ctx);
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
      ItemStack stack = player.getItemInHand(usedHand);
      if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
         CompoundTag stackTag = stack.getTag();
         if (stackTag != null && stackTag.hasUUID("SelectedRemote")) {
            UUID cameraId = stackTag.getUUID("SelectedRemote");
            if (player.isShiftKeyDown()) {
               stack.removeTagKey("SelectedRemote");
               AllSoundEvents.CONTROLLER_CLICK.play(level, (Player)null, player.blockPosition(), 0.5F, 1.1F);
               player.displayClientMessage(Components.translatable("railways.remote_lens.clear"), true);
               return InteractionResultHolder.success(stack);
            } else if (serverLevel.getEntity(cameraId) instanceof CameraEntity2 camera) {
               return camera.startViewing(serverPlayer) ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);
            } else {
               player.displayClientMessage(Components.literal("No connection to camera!"), true);
               return InteractionResultHolder.fail(stack);
            }
         } else {
            return InteractionResultHolder.fail(stack);
         }
      } else {
         return InteractionResultHolder.success(stack);
      }
   }
}
