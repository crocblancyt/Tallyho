package edn.stratodonut.tallyho.missile.tracker;

import java.util.HashMap;
import org.valkyrienskies.core.api.ships.Ship;

public class FlareTracker {
   private static final HashMap<Long, Long> flareCounter = new HashMap<>();

   public static void popFlare(Ship s) {
      flareCounter.compute(s.getId(), (k, v) -> v == null ? 1L : v + 1L);
   }

   public static long getFlareCount(Ship s) {
      return flareCounter.getOrDefault(s.getId(), 0L);
   }
}
