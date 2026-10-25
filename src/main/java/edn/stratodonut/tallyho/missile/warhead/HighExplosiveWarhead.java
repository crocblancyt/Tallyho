package edn.stratodonut.tallyho.missile.warhead;

import edn.stratodonut.tallyho.missile.IWarhead;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.CreateBigCannons;

public record HighExplosiveWarhead(float power) implements IWarhead {
   @Override
   public boolean detonate(Level level, Vec3 hit, Vec3 dir) {
      HighExplosion explosion = new HighExplosion(
         level, null, null, new FragExplosion.FragmentationCalculator(0.5F), hit.x, hit.y, hit.z, this.power, false, ExplosionInteraction.BLOCK
      );
      CreateBigCannons.handleCustomExplosion(level, explosion);
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
      components.add(Component.literal("High Explosive Warhead").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      components.add(
         Component.translatable("info.tallyho.tnt", new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.power / 8.0F)})
            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
      );
      components.add(
         Component.translatable(
               "info.tallyho.lethal_radius", new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(MissileRegistry.lethalRadius(this.power))}
            )
            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
      );
   }

   @Override
   public String toString() {
      return "";
   }
}
