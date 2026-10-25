package edn.stratodonut.tallyho.supportfire;

import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import edn.stratodonut.tallyho.missile.warhead.ClusterWarhead;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class ClusterStrike extends OffmapSupport {
   String missileId;
   ClusterWarhead clusterWarhead;
   public static final Vec3 DOWN = new Vec3(0.0, -1.0, 0.0);
   int timeOnTarget = 100;
   @Nullable
   Vec3 callPos;
   @Nullable
   Vec3 spawnPos;
   @Nullable
   String code;
   Queue<Vec3> munitionsInAir;
   int munitionIndex = 0;

   public ClusterStrike(String id) {
      this.missileId = id;
   }

   public static ClusterStrike instant(ClusterWarhead w, Vec3 spawnPos) {
      ClusterStrike strike = new ClusterStrike("");
      strike.spawnPos = spawnPos;
      strike.timeOnTarget = 0;
      strike.clusterWarhead = w;
      return strike;
   }

   @Override
   public boolean strike(@Nullable LivingEntity caller) {
      if (caller != null) {
         this.callPos = caller.position();
         this.code = caller.getName().getString();
         SupportTracker.add(caller.level(), this);
         caller.sendSystemMessage(Component.literal("Cluster Strike requested. Please designate"));
      }

      return super.strike(caller);
   }

   public boolean strike(@Nonnull Level level) {
      SupportTracker.add(level, this);
      return true;
   }

   @Override
   public void tick(Level world) {
      super.tick(world);
      if (world instanceof ServerLevel level) {
         if (this.clusterWarhead == null) {
            MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(this.missileId);
            if (!(entry.warhead instanceof ClusterWarhead c)) {
               this.complete(world);
               return;
            }

            this.clusterWarhead = c;
         }

         if (this.callPos != null && this.spawnPos == null && this.code != null) {
            LaserPointEntity.find(level, AABB.ofSize(this.callPos, 800.0, 800.0, 800.0), this.code)
               .ifPresent(laserPointEntity -> this.spawnPos = laserPointEntity.position().add(0.0, 300.0, 0.0));
         } else if (this.spawnPos != null) {
            if (this.timeOnTarget > 0) {
               this.timeOnTarget--;
               return;
            }

            if (this.munitionsInAir == null) {
               List<Vec3> munitions = new ArrayList<>();
               float side = (float)Math.sqrt(this.clusterWarhead.count());
               float d = this.clusterWarhead.dispersion() / 2.0F / (side / 2.0F);

               for (float i = -side / 2.0F; i < side / 2.0F; i++) {
                  for (float j = -side / 2.0F; j < side / 2.0F; j++) {
                     munitions.add(
                        new Vec3(
                           this.spawnPos.x + i * d + this.random.nextFloat(-5.0F, 5.0F),
                           this.spawnPos.y + this.random.nextFloat(-5.0F, 5.0F),
                           this.spawnPos.z + j * d + this.random.nextFloat(-5.0F, 5.0F)
                        )
                     );
                  }
               }

               Collections.shuffle(munitions);
               this.munitionsInAir = new LinkedList<>(munitions);
            }

            if (this.munitionsInAir.isEmpty()) {
               this.complete(world);
            } else {
               for (int i = 0; i < 20; i++) {
                  Vec3 pos = this.munitionsInAir.poll();
                  if (pos != null && !(pos.y <= level.getMinBuildHeight())) {
                     if (pos.y >= level.getMaxBuildHeight()) {
                        this.munitionsInAir.add(pos.add(0.0, -this.random.nextDouble(15.0, 25.0), 0.0));
                     } else {
                        BlockHitResult result = level.clip(
                           new ClipContext(pos, pos.add(0.0, -this.random.nextDouble(15.0, 25.0), 0.0), Block.COLLIDER, Fluid.NONE, null)
                        );
                        pos = result.getLocation();
                        if (result.getType() != Type.MISS) {
                           this.clusterWarhead.submunition().detonate(level, pos, DOWN);
                        } else {
                           this.munitionsInAir.add(pos);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void appendHoverText(List<Component> components) {
      components.add(Component.literal("Cluster Strike").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
      components.add(Component.literal(this.missileId).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
      components.add(Component.literal("On your Laser").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
      components.add(Component.literal("Time on Target: 5s").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
   }

   @Override
   public void addToNBT(CompoundTag nbt) {
      CompoundTag tag = new CompoundTag();
      tag.putString("Type", "Cluster");
      tag.putString("MissileId", this.missileId);
      nbt.put("StrikeTag", tag);
   }
}
