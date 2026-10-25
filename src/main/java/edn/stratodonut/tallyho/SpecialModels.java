package edn.stratodonut.tallyho;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "tallyho", value = Dist.CLIENT, bus = Bus.MOD)
public enum SpecialModels {
   GUN_MOUNT_RING("gun_mount_ring"),
   GUN_MOUNT_BRACKET("gun_mount_bracket"),
   TRIPOD_LEGS("tripod_mount_legs"),
   HELI_TURRET_GUN("heli_turret_gun"),
   HELI_TURRET_CAGE("heli_turret_cage"),
   CROWS_GUN("crows_gun"),
   CROWS_TURRET("crows_turret"),
   TGP_CAM("tgp_cam"),
   TGP_CRADLE("tgp_cradle"),
   TGP_BODY("tgp_body"),
   ROCKET_FLAME("rocket_flame");

   private final ResourceLocation modelLocation;
   @OnlyIn(Dist.CLIENT)
   private BakedModel cachedModel;

   SpecialModels(String modelName) {
      this.modelLocation = new ResourceLocation("tallyho", "special/" + modelName);
   }

   @OnlyIn(Dist.CLIENT)
   public BakedModel getModel() {
      if (this.cachedModel == null) {
         BakedModel model = Minecraft.getInstance().getModelManager().getModel(this.modelLocation);
         if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            return model;
         }

         this.cachedModel = model;
      }

      return this.cachedModel;
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public static void registerAdditional(RegisterAdditional event) {
      for (SpecialModels model : values()) {
         event.register(model.modelLocation);
      }
   }

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public static void register(BakingCompleted event) {
      for (SpecialModels model : values()) {
         model.cachedModel = null;
      }
   }
}
