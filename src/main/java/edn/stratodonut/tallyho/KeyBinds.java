package edn.stratodonut.tallyho;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD, modid = "tallyho")
public class KeyBinds {
   public static final KeyMapping KEY_ADJUST_SEAT_UP = new KeyMapping("key.tallyho.seat_up", 265, "key.categories.tallyho");
   public static final KeyMapping KEY_ADJUST_SEAT_DOWN = new KeyMapping("key.tallyho.seat_down", 264, "key.categories.tallyho");
   public static final KeyMapping KEY_DESIGNATE = new KeyMapping("key.tallyho.designate", 88, "key.categories.tallyho");
   public static final KeyMapping KEY_FUNCTION_1 = new KeyMapping("key.tallyho.special_one", 265, "key.categories.tallyho");
   public static final KeyMapping KEY_FUNCTION_2 = new KeyMapping("key.tallyho.special_two", 264, "key.categories.tallyho");
   public static final KeyMapping KEY_FUNCTION_3 = new KeyMapping("key.tallyho.special_three", 263, "key.categories.tallyho");
   public static final KeyMapping KEY_FUNCTION_4 = new KeyMapping("key.tallyho.special_four", 262, "key.categories.tallyho");
   public static final KeyMapping KEY_SMS_FOCUS = new KeyMapping("key.tallyho.sms_focus", 88, "key.categories.tallyho");
   public static final KeyMapping KEY_SMS_LAUNCH = new KeyMapping("key.tallyho.sms_launch", 32, "key.categories.tallyho");

   public static boolean isFuncKeyDown(byte keyMap, KeyBinds.SpecialFunctions key) {
      return (keyMap & 1 << key.value - 1) != 0;
   }

   public static void register() {
   }

   @SubscribeEvent
   public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
      event.register(KEY_ADJUST_SEAT_UP);
      event.register(KEY_ADJUST_SEAT_DOWN);
      event.register(KEY_DESIGNATE);
      event.register(KEY_FUNCTION_1);
      event.register(KEY_FUNCTION_2);
      event.register(KEY_FUNCTION_3);
      event.register(KEY_FUNCTION_4);
      event.register(KEY_SMS_FOCUS);
      event.register(KEY_SMS_LAUNCH);
   }

   public enum SpecialFunctions {
      UP(1),
      DOWN(2),
      LEFT(3),
      RIGHT(4);

      public final int value;

      SpecialFunctions(int v) {
         this.value = v;
      }
   }
}
