package edn.stratodonut.tallyho.mixin.sticker;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import edn.stratodonut.tallyho.mixinducks.MixinAbstractContraptionEntityDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({AbstractContraptionEntity.class})
public abstract class MixinAbstractContraptionEntity implements MixinAbstractContraptionEntityDuck {
    @Shadow
    protected abstract StructureTransform makeStructureTransform();

    public StructureTransform getStructureTransform() {
      return makeStructureTransform();
    }
}
