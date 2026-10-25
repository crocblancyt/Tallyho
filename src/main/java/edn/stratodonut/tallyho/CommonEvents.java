package edn.stratodonut.tallyho;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import edn.stratodonut.tallyho.missile.tracker.RadEmissionTracker;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.LimitF5Packet;
import edn.stratodonut.tallyho.network.MovementAffectAimPacket;
import edn.stratodonut.tallyho.network.ProneClipGamerulePacket;
import edn.stratodonut.tallyho.objectives.CapturePointUtil;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@EventBusSubscriber(modid = "tallyho")
public class CommonEvents {
   @SubscribeEvent
   public static void onPlayerJoin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer p) {
         CreateTypePacketHandler.getChannel()
            .send(PacketDistributor.PLAYER.with(() -> p), new LimitF5Packet(((BooleanValue)p.level().getGameRules().getRule(AllGameRules.RULE_LIMITF5)).get()));
         CreateTypePacketHandler.getChannel()
            .send(
               PacketDistributor.PLAYER.with(() -> p),
               new MovementAffectAimPacket(((BooleanValue)p.level().getGameRules().getRule(AllGameRules.RULE_MOVE_AFFECTS_AIM)).get())
            );
         CreateTypePacketHandler.getChannel()
            .send(
               PacketDistributor.PLAYER.with(() -> p),
               new ProneClipGamerulePacket(((BooleanValue)p.level().getGameRules().getRule(AllGameRules.RULE_PRONE_CLIPPING)).get())
            );
      }
   }

   @SubscribeEvent
   public static void onTickEnd(LevelTickEvent event) {
      if (event.side == LogicalSide.SERVER && event.phase == Phase.END && event.level instanceof ServerLevel serverLevel) {
         CapturePointUtil.updateTotalScores(serverLevel);
         CapturePointUtil.updateCaptureStatusTitle(serverLevel.getServer());
         RadEmissionTracker.tick(event.level);
      }
   }

   @SubscribeEvent
   public static void onPlayerBreakBlock(BreakEvent event) {
      if (event.getPlayer().getVehicle() instanceof GunMountEntity) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onExplosion(Detonate event) {
      Level level = event.getLevel();
      if ((Boolean)ConfigRegistry.COMBUSTIBLES_BLOW_UP.get() && !level.isClientSide) {
         for (BlockPos pos : event.getAffectedBlocks()) {
            if (level.isInWorldBounds(pos)) {
               Vec3 explosion_pos = Optional.ofNullable(VSGameUtilsKt.getShipManagingPos(level, pos))
                  .map(s -> VectorConversionsMCKt.toMinecraft(s.getShipToWorld().transformPosition(VectorConversionsMCKt.toJOML(Vec3.atCenterOf(pos)))))
                  .orElse(Vec3.atCenterOf(pos));
               FluidState fluidState = level.getFluidState(pos);
               BlockEntity be = level.getBlockEntity(pos);
               Optional<Fluid> fluid = Optional.empty();
               if (FuelTypeManager.getGeneratedSpeed(fluidState.getType()) != 0.0F) {
                  fluid = Optional.of(fluidState.getType());
               } else if (be != null) {
                  fluid = be.getCapability(ForgeCapabilities.FLUID_HANDLER).map(f -> f.getFluidInTank(0).getFluid());
               }

               fluid.filter(f -> FuelTypeManager.getGeneratedSpeed(f) != 0.0F).ifPresent(x -> {
                  level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                  level.explode(null, null, null, explosion_pos.x(), explosion_pos.y(), explosion_pos.z(), 8.0F, true, ExplosionInteraction.BLOCK);
               });
            }
         }
      }
   }
}
