package edn.stratodonut.tallyho.supportfire;

import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.missile.GuidanceComponent;
import edn.stratodonut.tallyho.missile.MissileRegistry;
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

public class CarpetBombStrike extends OffmapSupport {
   String missileId;
   int quantity;
   OffmapSupport.DropPattern pattern = OffmapSupport.DropPattern.LINE;
   Vec3 callPos;
   Vec3 startPos;
   Vec3 dropDirection;
   int timeToNext = 60;
   int DELAY_BETWEEN_DROP = 3;
   String code;

   public CarpetBombStrike(String id, int qty) {
      this.missileId = id;
      this.quantity = qty;
   }

   @Override
   public boolean strike(LivingEntity caller) {
      this.callPos = caller.position();
      this.code = caller.getName().getString();
      SupportTracker.add(caller.level(), this);
      caller.sendSystemMessage(Component.literal("Carpet bomb Strike requested. Please designate"));
      return super.strike(caller);
   }

   @Override
   public void tick(Level world) {
      super.tick(world);
      if (world instanceof ServerLevel level) {
         MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(this.missileId);
         if (entry == null) {
            this.complete(world);
         }

         String code = this.code;
         if (!this.isDropping()) {
            Optional<LaserPointEntity> targ = LaserPointEntity.find(level, AABB.ofSize(this.callPos, 800.0, 800.0, 800.0), code);
            if (targ.isPresent()) {
               if (this.startPos == null) {
                  this.startPos = targ.get().position();
               } else if (this.dropDirection == null) {
                  if (targ.get().position().distanceTo(this.startPos) < 32.0) {
                     return;
                  }

                  this.dropDirection = targ.get().position().subtract(this.startPos);
                  this.dropDirection = this.dropDirection.subtract(0.0, this.dropDirection.y, 0.0).normalize();
                  this.startDrop(world);
               }
            }
         } else {
            if (this.timeToNext > 0) {
               this.timeToNext--;
               return;
            }

            this.timeToNext = this.DELAY_BETWEEN_DROP;
            Vec3 spawnPos = this.startPos.add(0.0, 300.0, 0.0);
            Vec3 strikeTarget = this.startPos;
            Vec3 strikeDirection = strikeTarget.subtract(spawnPos).normalize();
            MountedMissileEntity m = entry.spawn(level, spawnPos, 0.0F);
            GuidanceComponent g = m.getGuidance();
            if (g != null) {
               g.setCode(this.code);
            }

            m.launch(this.spread(strikeDirection, 1.0F, 3.0F), 2.0F);
            this.startPos = this.startPos.add(this.dropDirection.scale(16.0));
            this.quantity--;
            if (this.quantity == 0) {
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
      components.add(Component.literal("Carpet Strike").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
      components.add(Component.literal(this.missileId + " x" + this.quantity).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
      components.add(Component.literal("On Laze point 1 towards 2, Code is your name").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
      components.add(Component.literal("Time on Target: 3s").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
   }

   @Override
   public void addToNBT(CompoundTag nbt) {
      CompoundTag tag = new CompoundTag();
      tag.putString("Type", "Carpet");
      tag.putString("MissileId", this.missileId);
      tag.putInt("Qty", this.quantity);
      nbt.put("StrikeTag", tag);
   }

   public Vec3 spread(Vec3 dir, float vel, float spread) {
      return dir.normalize()
         .add(this.random.nextGaussian() * 0.0075F * spread, this.random.nextGaussian() * 0.0075F * spread, this.random.nextGaussian() * 0.0075F * spread)
         .scale(vel);
   }
}
