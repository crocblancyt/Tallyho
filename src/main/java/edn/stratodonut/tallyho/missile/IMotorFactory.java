package edn.stratodonut.tallyho.missile;

import java.util.List;
import net.minecraft.network.chat.Component;

public interface IMotorFactory<T extends MotorComponent> {
   T create();

   void appendHoverText(List<Component> var1);
}
