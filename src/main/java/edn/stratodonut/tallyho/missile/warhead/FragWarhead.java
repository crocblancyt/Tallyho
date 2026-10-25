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

public record FragWarhead(float power, float fragPercent) implements IWarhead {
   @Override
   public boolean detonate(Level level, Vec3 hit, Vec3 dir) {
      FragExplosion explosion = new FragExplosion(level, null, null, hit.x, hit.y, hit.z, this.fragPercent, this.power, false, ExplosionInteraction.BLOCK);
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
      components.add(Component.literal("Blast Fragmentation Warhead").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
      components.add(
         Component.translatable(
               "info.tallyho.tnt",
               new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(FragExplosion.effectiveBlockDamagePower(this.power, this.fragPercent) / 4.0F)}
            )
            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
      );
      components.add(
         Component.translatable(
               "info.tallyho.lethal_radius", new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(MissileRegistry.lethalRadius(this.power))}
            )
            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
      );
   }
}
