package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.missile.MissileRegistry;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class MissileItem extends Item {
   public final String id;

   public MissileItem(Properties p_41383_) {
      super(p_41383_);
      this.id = "null";
   }

   public MissileItem(Properties p_41383_, String id) {
      super(p_41383_);
      this.id = id;
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
      Player player = ctx.getPlayer();
      if (s == null || player == null) {
         return InteractionResult.FAIL;
      }

      if (level.isClientSide) {
         return InteractionResult.CONSUME;
      }

      if (level instanceof ServerLevel serverLevel && ctx.getPlayer() instanceof ServerPlayer) {
         Direction face = ctx.getClickedFace();
         Vector3d lookInShip = s.getWorldToShip().transformDirection(VectorConversionsMCKt.toJOML(player.getLookAngle()));
         MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(this.id);
         if (entry == null) {
            return InteractionResult.FAIL;
         }

         Vec3 spawnPos = ctx.getClickLocation();
         if (face == Direction.DOWN) {
            spawnPos = spawnPos.subtract(0.0, entry.getHitboxScale(), 0.0);
         }

         spawnPos = gridify(spawnPos, entry.getHitboxScale()).subtract(0.0, 0.25, 0.0);
         entry.spawn(serverLevel, spawnPos, Direction.getNearest(lookInShip.x, 0.0, lookInShip.z).toYRot());
         if (!ctx.getPlayer().isCreative()) {
            ctx.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.SUCCESS;
   }

   public static Vec3 gridify(Vec3 original, float scale) {
      scale = 1.0F / Math.max(scale, 0.0625F) + 1.0F;
      Vec3 t = new Vec3(Math.floor(original.x), Math.floor(original.y), Math.floor(original.z));
      Vec3 f = original.subtract(t);
      return new Vec3(t.x + (float)Math.round(f.x * scale) / scale, t.y + (float)Math.round(f.y * scale) / scale, t.z + (float)Math.round(f.z * scale) / scale);
   }

   public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> components, TooltipFlag p_41424_) {
      super.appendHoverText(p_41421_, p_41422_, components, p_41424_);
      MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(this.id);
      if (entry != null) {
         entry.appendHoverText(components);
      }
   }
}
