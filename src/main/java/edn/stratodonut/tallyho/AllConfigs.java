package edn.stratodonut.tallyho;

import com.simibubi.create.foundation.config.ConfigBase;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

public class AllConfigs {
    private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    private static TServer server;
    private static TClient client;

    public static class TServer extends ConfigBase {
        public final ConfigBase.ConfigFloat recoilMultiplier = f(1.0F, 0.0F, "recoilForceMultiplier", new String[] { "Placeholder, does nothing." });

        public String getName() {
            return "server";
        }
    }

    public static class TClient extends ConfigBase {
        public final ConfigBase.ConfigBool disableQuickExit = b(false, "disableQuickExit", new String[] { "Disable Controller Exit on Right-click" });

        public String getName() {
            return "client";
        }
    }

    public static TClient client() {
        return client;
    }

    public static TServer server() {
        return server;
    }

    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        Pair<ConfigBase, ForgeConfigSpec> specPair = (new ForgeConfigSpec.Builder()).configure(builder -> {
            ConfigBase configBase = factory.get();
            configBase.registerAll(builder);
            return configBase;
        });

        ConfigBase configBase = (ConfigBase)specPair.getLeft();
        configBase.specification = (ForgeConfigSpec)specPair.getRight();
        CONFIGS.put(side, configBase);
        return (T)configBase;
    }

    public static void register(ModLoadingContext context) {
        server = register(TServer::new, ModConfig.Type.SERVER);
        client = register(TClient::new, ModConfig.Type.CLIENT);
        for (Map.Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
            context.registerConfig(pair.getKey(), (IConfigSpec)((ConfigBase)pair.getValue()).specification);
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        for (ConfigBase config : CONFIGS.values()) {
            if (config.specification == event.getConfig().getSpec())
                config.onLoad();
        }
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        for (ConfigBase config : CONFIGS.values()) {
            if (config.specification == event.getConfig().getSpec())
                config.onReload();
        }
    }
}