package edn.stratodonut.tallyho.client;

import edn.stratodonut.tallyho.KeypressHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "tallyho", value = Dist.CLIENT, bus = Bus.MOD)
public class ClientEventsRegistrar {
   public static void setup() {
      MinecraftForge.EVENT_BUS.register(KeypressHandler.get());
      MinecraftForge.EVENT_BUS.register(SuppressionTracker.get());
   }
}
