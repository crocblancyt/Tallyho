package edn.stratodonut.tallyho;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import edn.stratodonut.tallyho.missile.tracker.RadEmissionTracker;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.LimitF5Packet;
import edn.stratodonut.tallyho.network.MovementAffectAimPacket;
import edn.stratodonut.tallyho.network.ProneClipGamerulePacket;
import edn.stratodonut.tallyho.objectives.CapturePointUtil;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@EventBusSubscriber(modid = "tallyho")
public class CommonEvents {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)player;
            CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new LimitF5Packet(p
                    .level().getGameRules().getRule(AllGameRules.RULE_LIMITF5).get()));
            CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new MovementAffectAimPacket(p
                    .level().getGameRules().getRule(AllGameRules.RULE_MOVE_AFFECTS_AIM).get()));
            CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> p), new ProneClipGamerulePacket(p
                    .level().getGameRules().getRule(AllGameRules.RULE_PRONE_CLIPPING).get()));
        }
    }

    @SubscribeEvent
    public static void onTickEnd(TickEvent.LevelTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            Level level = event.level;
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                CapturePointUtil.updateTotalScores(serverLevel);
                CapturePointUtil.updateCaptureStatusTitle(serverLevel.getServer());
                RadEmissionTracker.tick(event.level);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
        if (event.getPlayer().getVehicle() instanceof edn.stratodonut.tallyho.entity.GunMountEntity)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (ConfigRegistry.COMBUSTIBLES_BLOW_UP.get() && !level.isClientSide)
            for (Iterator<BlockPos> iterator = event.getAffectedBlocks().iterator(); iterator.hasNext(); ) {
                BlockPos pos = iterator.next();
                if (level.hasChunkAt(pos)) {
                    Vec3 explosion_pos = Optional.<Ship>ofNullable(VSGameUtilsKt.getShipManagingPos(level, pos)).map(s -> VectorConversionsMCKt.toMinecraft((Vector3dc)s.getShipToWorld().transformPosition(VectorConversionsMCKt.toJOML(Vec3.atCenterOf((Vec3i)pos))))).orElse(Vec3.atCenterOf((Vec3i)pos));
                    FluidState fluidState = level.getFluidState(pos);
                    BlockEntity be = level.getBlockEntity(pos);
                    Optional<Fluid> fluid = Optional.empty();
                    if (FuelTypeManager.getGeneratedSpeed(fluidState.getType()) != 0.0F) {
                        fluid = Optional.of(fluidState.getType());
                    } else if (be != null) {
                        fluid = be.getCapability(ForgeCapabilities.FLUID_HANDLER).map(f -> f.getFluidInTank(0).getFluid());
                    }
                    fluid.filter(f -> (FuelTypeManager.getGeneratedSpeed(f) != 0.0F)).ifPresent(x -> {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        level.explode(null, null, null, explosion_pos.x(), explosion_pos.y(), explosion_pos.z(), 8.0F, true, Level.ExplosionInteraction.BLOCK);
                    });
                }
            }
    }
}