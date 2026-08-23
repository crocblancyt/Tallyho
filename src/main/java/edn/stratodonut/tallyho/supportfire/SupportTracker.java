package edn.stratodonut.tallyho.supportfire;

import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "tallyho")
public final class SupportTracker {
    private static final Queue<TrackedSupportCall> TRACKED_SUPPORT_CALL_QUEUE = new LinkedList<>();

    public static void add(Level level, OffmapSupport call) {
        TRACKED_SUPPORT_CALL_QUEUE.add(new TrackedSupportCall(level, call));
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Level level = event.level;
            for (TrackedSupportCall call : TRACKED_SUPPORT_CALL_QUEUE) {
                if (call.world.equals(level) && !call.support.isComplete())
                    call.support.tick(level);
            }
            TRACKED_SUPPORT_CALL_QUEUE.removeIf(call -> call.support.isComplete());
        }
    }

    public record TrackedSupportCall(Level world, OffmapSupport support) {
    }
}