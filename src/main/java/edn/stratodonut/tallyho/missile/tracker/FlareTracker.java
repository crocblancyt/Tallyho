package edn.stratodonut.tallyho.missile.tracker;

import java.util.HashMap;
import org.valkyrienskies.core.api.ships.Ship;

public class FlareTracker {
    private static final HashMap<Long, Long> flareCounter = new HashMap<>();

    public static void popFlare(Ship s) {
        flareCounter.compute(Long.valueOf(s.getId()), (k, v) -> Long.valueOf((v == null) ? 1L : (v.longValue() + 1L)));
    }

    public static long getFlareCount(Ship s) {
        return (flareCounter.getOrDefault(Long.valueOf(s.getId()), Long.valueOf(0L))).longValue();
    }
}
