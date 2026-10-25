package edn.stratodonut.tallyho;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AllSounds {
   public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "tallyho");
   public static final RegistryObject<SoundEvent> HELI_LOOP = registerSoundEvents("heli_loop");
   public static final RegistryObject<SoundEvent> BULLET_WHIZZ = registerSoundEvents("bullet_whizz");
   public static final RegistryObject<SoundEvent> BULLET_IMPACT = registerSoundEvents("bullet_impact");
   public static final RegistryObject<SoundEvent> AIM9_SEEK = registerSoundEvents("aim9_seek");
   public static final RegistryObject<SoundEvent> AIM9_LOCK = registerSoundEvents("aim9_lock");
   public static final RegistryObject<SoundEvent> MISSILE_LAUNCH = registerSoundEvents("missile_launch");

   private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
      return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("tallyho", name)));
   }

   public static void register(IEventBus bus) {
      SOUND_EVENTS.register(bus);
   }
}
