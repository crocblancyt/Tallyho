package edn.stratodonut.tallyho.missile.warhead;

import edn.stratodonut.tallyho.missile.IWarhead;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.block_armor_properties.BlockArmorPropertiesHandler;

public record ShapedChargeWarhead(float power, float distance) implements IWarhead {
   @Override
   public boolean detonate(Level world, Vec3 origin, Vec3 direction) {
      float localPower = this.power;
      float localDistance = this.distance;
      int limit = 16;
      Vec3 pos = origin;

      while (localDistance > 0.0F && limit > 0 && localPower > 0.0F) {
         limit--;
         BlockHitResult hit = world.clip(new ClipContext(pos, pos.add(direction.scale(localDistance)), Block.COLLIDER, Fluid.NONE, null));
         if (hit.getType() != Type.MISS) {
            BlockPos bpos = hit.getBlockPos();
            BlockState state = world.getBlockState(bpos);
            float toughness = (float)BlockArmorPropertiesHandler.getProperties(state).toughness(world, state, bpos, true);
            if (state.getDestroySpeed(world, bpos) == -1.0F || localPower < toughness) {
               break;
            }

            world.destroyBlock(bpos, false);
            localPower -= toughness;
         }

         float delta = (float)hit.getLocation().subtract(pos).length();
         pos = pos.add(direction.scale(delta));
         localDistance -= delta;
      }

      world.explode(
         null, null, new ExplosionDamageCalculator(), pos.x, pos.y, pos.z, Mth.clamp(this.power / 10.0F, 2.0F, 6.0F), false, ExplosionInteraction.BLOCK
      );
      HighExplosion explosionAtWarhead = new HighExplosion(world, null, null, null, origin.x, origin.y, origin.z, 2.0F, false, ExplosionInteraction.NONE);
      CreateBigCannons.handleCustomExplosion(world, explosionAtWarhead);
      return true;
   }

   @Override
   public void save(CompoundTag nbt) {
   }

   @Override
   public void read(CompoundTag nbt) {
   }

   @Override
   public void appendHoverText(List<Component> components) {
      components.add(Component.literal("Shaped Charge Warhead").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      components.add(
         Component.translatable("info.tallyho.shaped_charge", new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.power)})
            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
      );
   }
}
