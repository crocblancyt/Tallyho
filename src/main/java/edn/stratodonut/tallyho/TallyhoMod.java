package edn.stratodonut.tallyho;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import edn.stratodonut.tallyho.cbc.CustomHardnessIndex;
import edn.stratodonut.tallyho.client.ClientEventsRegistrar;
import edn.stratodonut.tallyho.integration.cc.ClockworkForgePeripheralProviders;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

@Mod("tallyho")
public class TallyhoMod {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "tallyho";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create("tallyho");

    public TallyhoMod() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        REGISTRATE.registerEventListeners(modEventBus);
        NonSolidBlockstateProvider.init();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onCommonSetup);
        AllConfigs.register(modLoadingContext);
        CreateTypePacketHandler.registerPackets();
        AllItems.register();
        AllEntities.register();
        MissileRegistry.register();
        AllBlocks.register();
        AllBlockEntities.register();
        AllCreativeTabs.register(modEventBus);
        AllSounds.register(modEventBus);
        AllGameRules.init();
        if (FMLLoader.getLoadingModList().getModFileById("computercraft") != null)
            ClockworkForgePeripheralProviders.register();
    }

    private static final String[] depList = new String[] { "create", "copycats", "createbigcannons", "createdieselgenerators", "pointblank", "vs_clockwork" };

    private void onCommonSetup(FMLCommonSetupEvent event) {
        ModList list = ModList.get();
        for (String dep : depList) {
            if (list.getModFileById(dep) == null)
                throw new BadModListException(dep);
        }
        CustomHardnessIndex.init();
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientEventsRegistrar.setup();
            KeyBinds.register();
        });
    }

    public static void warn(String s) {
        LOGGER.warn(s);
    }

    public static void warn(String s, Object args) {
        LOGGER.warn(s, args);
    }

    public static void warn(String s, Object... args) {
        LOGGER.warn(s, args);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation("tallyho", path);
    }
}