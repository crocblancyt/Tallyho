package edn.stratodonut.tallyho.missile.warhead;

import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.missile.IWarhead;
import edn.stratodonut.tallyho.supportfire.ClusterStrike;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record ClusterWarhead(IWarhead submunition, int count, float dispersion) implements IWarhead {
   @Override
   public boolean detonate(Level level, Vec3 hit, Vec3 dir) {
      ClusterStrike strike = ClusterStrike.instant(this, hit);
      strike.strike(level);
      level.getNearbyPlayers(TargetingConditions.forNonCombat(), null, AABB.ofSize(hit, this.dispersion, 400.0, this.dispersion))
         .forEach(
            p -> p.displayClientMessage(
               Components.literal("You feel a sudden chill in the wind.").withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.DARK_GRAY)), true
            )
         );
      level.explode(null, null, new ExplosionDamageCalculator(), hit.x, hit.y, hit.z, 2.0F, false, ExplosionInteraction.NONE);
      return true;
   }

   @Override
   public void save(CompoundTag nbt) {
      this.submunition.save(nbt);
   }

   @Override
   public void read(CompoundTag nbt) {
      this.submunition.read(nbt);
   }

   @Override
   public void appendHoverText(List<Component> components) {
      components.add(Component.literal(String.format("Cluster x%d:", this.count)).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
      this.submunition.appendHoverText(components);
   }
}
