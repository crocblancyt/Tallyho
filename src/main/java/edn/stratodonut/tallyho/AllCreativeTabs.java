package edn.stratodonut.tallyho;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.utility.Components;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import edn.stratodonut.tallyho.supportfire.CarpetBombStrike;
import edn.stratodonut.tallyho.supportfire.LazedMissileStrike;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(bus = Bus.MOD)
public class AllCreativeTabs {
   private static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "tallyho");
   public static final RegistryObject<CreativeModeTab> BASE_CREATIVE_TAB = REGISTER.register(
      "base",
      () -> CreativeModeTab.builder()
         .title(Components.translatable("itemGroup.tallyho.base"))
         .icon(Items.EGG::getDefaultInstance)
         .displayItems(new AllCreativeTabs.RegistrateDisplayItemsGenerator(true, AllCreativeModeTabs.BASE_CREATIVE_TAB))
         .build()
   );

   public static void register(IEventBus modEventBus) {
      REGISTER.register(modEventBus);
   }

   private static class RegistrateDisplayItemsGenerator implements DisplayItemsGenerator {
      private final boolean addItems;
      private final RegistryObject<CreativeModeTab> tabFilter;

      public RegistrateDisplayItemsGenerator(boolean addItems, RegistryObject<CreativeModeTab> tabFilter) {
         this.addItems = addItems;
         this.tabFilter = tabFilter;
      }

      public void accept(ItemDisplayParameters p_270258_, Output output) {
         for (ItemStack stack : this.fillItemList(new LinkedList<>())) {
            output.accept(stack);
         }
      }

      private static Predicate<Item> makeExclusionPredicate() {
         Set<Item> exclusions = new ReferenceOpenHashSet();

         for (ItemProviderEntry<?> entry : List.of(
            AllItems.SUPPORT_RADIO_ITEM,
            AllItems.REMOTE_CAMERA_ITEM,
            AllItems.TURRET_CAMERA_ITEM,
            MissileRegistry.ROCKEYE.getItemEntry(),
            MissileRegistry.CBU_87.getItemEntry()
         )) {
            exclusions.add(entry.asItem());
         }

         return exclusions::contains;
      }

      public List<ItemStack> fillItemList(@NotNull List<ItemStack> items) {
         this.addBlocks(items);
         this.addItems(items, true);
         this.addItems(items, false);
         ItemStack radio = new ItemStack((ItemLike)AllItems.SUPPORT_RADIO_ITEM.get());
         new LazedMissileStrike(MissileRegistry.GBU_24.id).addToNBT(radio.getOrCreateTag());
         items.add(radio);
         ItemStack radio2 = new ItemStack((ItemLike)AllItems.SUPPORT_RADIO_ITEM.get());
         new LazedMissileStrike(MissileRegistry.AGM_114B.id).addToNBT(radio2.getOrCreateTag());
         items.add(radio2);
         ItemStack radio3 = new ItemStack((ItemLike)AllItems.SUPPORT_RADIO_ITEM.get());
         new CarpetBombStrike(MissileRegistry.SNAKEYE.id, 8).addToNBT(radio3.getOrCreateTag());
         items.add(radio3);
         return items;
      }

      protected Collection<RegistryEntry<Item>> registeredItems() {
         return TallyhoMod.REGISTRATE.getAll(ForgeRegistries.ITEMS.getRegistryKey());
      }

      public void addBlocks(List<ItemStack> items) {
         for (RegistryEntry<Item> entry : this.registeredItems()) {
            if (entry.get() instanceof BlockItem blockItem) {
               items.add(new ItemStack(blockItem));
            }
         }
      }

      public void addItems(List<ItemStack> items, boolean specialItems) {
         ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
         Predicate<Item> exclusionFilter = makeExclusionPredicate();

         for (RegistryEntry<Item> entry : this.registeredItems()) {
            Item item = (Item)entry.get();
            if (!(item instanceof BlockItem)) {
               ItemStack stack = new ItemStack(item);
               BakedModel model = itemRenderer.getModel(stack, null, null, 0);
               if (model.isGui3d() == specialItems && !exclusionFilter.test(item)) {
                  items.add(new ItemStack(item));
               }
            }
         }
      }

      private static void outputAll(Output output, List<Item> items, Function<Item, ItemStack> stackFunc, Function<Item, TabVisibility> visibilityFunc) {
         for (Item item : items) {
            output.accept(stackFunc.apply(item), visibilityFunc.apply(item));
         }
      }
   }
}
