package edn.stratodonut.tallyho.supportfire;

import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.GuidanceComponent;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import edn.stratodonut.tallyho.missile.fuze.AltitudeFuze;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LazedMissileStrike extends OffmapSupport {
   String missileId;
   int dropDelay = 60;
   Vec3 callPos;
   String code;

   public LazedMissileStrike(String id) {
      this.missileId = id;
   }

   @Override
   public boolean strike(LivingEntity caller) {
      this.callPos = caller.position();
      this.code = caller.getName().getString();
      SupportTracker.add(caller.level(), this);
      caller.sendSystemMessage(Component.literal("Precision Strike requested. Please designate"));
      return super.strike(caller);
   }

   @Override
   public void tick(Level world) {
      super.tick(world);
      if (world instanceof ServerLevel level) {
         MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(this.missileId);
         if (entry == null) {
            this.complete(world);
         } else {
            String code = this.code;
            Optional<LaserPointEntity> targ = LaserPointEntity.find(level, AABB.ofSize(this.callPos, 800.0, 800.0, 800.0), code);
            if (targ.isPresent()) {
               this.startDrop(world);
               if (this.dropDelay > 0) {
                  this.dropDelay--;
                  return;
               }

               Vec3 spawnPos = this.callPos.add(0.0, 400.0, 0.0);
               Vec3 strikeTarget = targ.get().position();
               if (MissileRegistry.getEntry(this.missileId).getFuze() instanceof AltitudeFuze af) {
                  strikeTarget = strikeTarget.add(0.0, af.alt - strikeTarget.y, 0.0);
               }

               Vec3 strikeDirection = strikeTarget.subtract(spawnPos).normalize();
               Vec3 displacement = strikeDirection.subtract(0.0, strikeDirection.y, 0.0);
               MountedMissileEntity m = entry.spawn(level, spawnPos, 0.0F);
               GuidanceComponent g = m.getGuidance();
               if (g != null) {
                  g.setCode(this.code);
               }

               m.launch(this.spread(strikeDirection, 1.0F, 3.0F), 4.0F);
               spawnPos = spawnPos.add(displacement.scale(8.0));
               strikeTarget = strikeTarget.add(displacement.scale(16.0));
               this.complete(world);
            }
         }
      }
   }

   @Override
   public void startDrop(@Nullable Level level) {
      if (level != null && !this.isDropping() && this.callerUUID != null) {
         Player p = level.getPlayerByUUID(this.callerUUID);
         if (p != null) {
            p.sendSystemMessage(Component.literal("I see your lazer. Bombs away."));
         }
      }

      super.startDrop(level);
   }

   @Override
   public void complete(@Nullable Level level) {
      super.complete(level);
   }

   @Override
   public void appendHoverText(List<Component> components) {
      components.add(Component.literal("Precision Strike").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
      components.add(Component.literal(this.missileId + " x1").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
      components.add(Component.literal("On your Laser hold 3s, Code is your name").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
   }

   @Override
   public void addToNBT(CompoundTag nbt) {
      CompoundTag tag = new CompoundTag();
      tag.putString("Type", "Precision");
      tag.putString("MissileId", this.missileId);
      nbt.put("StrikeTag", tag);
   }

   public Vec3 spread(Vec3 dir, float vel, float spread) {
      return dir.normalize()
         .add(this.random.nextGaussian() * 0.0075F * spread, this.random.nextGaussian() * 0.0075F * spread, this.random.nextGaussian() * 0.0075F * spread)
         .scale(vel);
   }
}
