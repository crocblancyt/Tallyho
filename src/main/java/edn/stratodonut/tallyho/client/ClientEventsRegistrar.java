package edn.stratodonut.tallyho.client;

import edn.stratodonut.tallyho.KeypressHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "tallyho", value = {Dist.CLIENT}, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventsRegistrar {
    public static void setup() {
        MinecraftForge.EVENT_BUS.register(KeypressHandler.get());
        MinecraftForge.EVENT_BUS.register(SuppressionTracker.get());
    }
}
