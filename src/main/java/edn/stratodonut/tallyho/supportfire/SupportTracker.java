package edn.stratodonut.tallyho.supportfire;

import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "tallyho")
public final class SupportTracker {
   private static final Queue<SupportTracker.TrackedSupportCall> TRACKED_SUPPORT_CALL_QUEUE = new LinkedList<>();

   public static void add(Level level, OffmapSupport call) {
      TRACKED_SUPPORT_CALL_QUEUE.add(new SupportTracker.TrackedSupportCall(level, call));
   }

   @SubscribeEvent
   public static void tick(LevelTickEvent event) {
      if (event.phase == Phase.START) {
         Level level = event.level;

         for (SupportTracker.TrackedSupportCall call : TRACKED_SUPPORT_CALL_QUEUE) {
            if (call.world.equals(level) && !call.support.isComplete()) {
               call.support.tick(level);
            }
         }

         TRACKED_SUPPORT_CALL_QUEUE.removeIf(callx -> callx.support.isComplete());
      }
   }

   public record TrackedSupportCall(Level world, OffmapSupport support) {
   }
}
