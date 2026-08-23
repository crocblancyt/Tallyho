package edn.stratodonut.tallyho.missile;

import java.util.List;
import net.minecraft.network.chat.Component;

public interface IFuzeFactory<T extends FuzeComponent> {
    T create();

    void appendHoverText(List<Component> paramList);
}
