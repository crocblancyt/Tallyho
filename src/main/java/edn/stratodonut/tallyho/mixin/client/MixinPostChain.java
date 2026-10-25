package edn.stratodonut.tallyho.mixin.client;

import com.mojang.blaze3d.shaders.Uniform;
import edn.stratodonut.tallyho.mixinducks.MixinPostChainDuck;
import java.util.List;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PostChain.class)
public abstract class MixinPostChain implements MixinPostChainDuck {
   @Shadow
   @Final
   private List<PostPass> passes;

   @Override
   public void tyh$setUniformFloat(int passIndex, String name, float value) {
      if (this.passes.size() > passIndex && this.passes.get(passIndex) != null) {
         PostPass pass = this.passes.get(passIndex);
         Uniform u = pass.getEffect().getUniform(name);
         if (u != null) {
            u.set(value);
         }
      }
   }
}
