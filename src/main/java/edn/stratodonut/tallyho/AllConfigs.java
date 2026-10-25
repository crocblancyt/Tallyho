package edn.stratodonut.tallyho;

import com.simibubi.create.foundation.config.ConfigBase;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;
import com.simibubi.create.foundation.config.ConfigBase.ConfigFloat;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent.Loading;
import net.minecraftforge.fml.event.config.ModConfigEvent.Reloading;
import org.apache.commons.lang3.tuple.Pair;

public class AllConfigs {
   private static final Map<Type, ConfigBase> CONFIGS = new EnumMap<>(Type.class);
   private static AllConfigs.TServer server;
   private static AllConfigs.TClient client;

   public static AllConfigs.TClient client() {
      return client;
   }

   public static AllConfigs.TServer server() {
      return server;
   }

   private static <T extends ConfigBase> T register(Supplier<T> factory, Type side) {
      Pair<T, ForgeConfigSpec> specPair = new Builder().configure(builder -> {
         T configx = factory.get();
         configx.registerAll(builder);
         return configx;
      });
      T config = (T)specPair.getLeft();
      config.specification = (ForgeConfigSpec)specPair.getRight();
      CONFIGS.put(side, config);
      return config;
   }

   public static void register(ModLoadingContext context) {
      server = register(AllConfigs.TServer::new, Type.SERVER);
      client = register(AllConfigs.TClient::new, Type.CLIENT);

      for (Entry<Type, ConfigBase> pair : CONFIGS.entrySet()) {
         context.registerConfig(pair.getKey(), pair.getValue().specification);
      }
   }

   @SubscribeEvent
   public static void onLoad(Loading event) {
      for (ConfigBase config : CONFIGS.values()) {
         if (config.specification == event.getConfig().getSpec()) {
            config.onLoad();
         }
      }
   }

   @SubscribeEvent
   public static void onReload(Reloading event) {
      for (ConfigBase config : CONFIGS.values()) {
         if (config.specification == event.getConfig().getSpec()) {
            config.onReload();
         }
      }
   }

   public static class TClient extends ConfigBase {
      public final ConfigBool disableQuickExit = this.b(false, "disableQuickExit", new String[]{"Disable Controller Exit on Right-click"});

      public String getName() {
         return "client";
      }
   }

   public static class TServer extends ConfigBase {
      public final ConfigFloat recoilMultiplier = this.f(1.0F, 0.0F, "recoilForceMultiplier", new String[]{"Placeholder, does nothing."});

      public String getName() {
         return "server";
      }
   }
}
