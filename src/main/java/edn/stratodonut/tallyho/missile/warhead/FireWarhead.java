package edn.stratodonut.tallyho.missile.warhead;

import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.missile.IWarhead;
import edn.stratodonut.tallyho.missile.MissileRegistry;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.multiloader.IndexPlatform;
import rbasamoyai.createbigcannons.munitions.GasCloudEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.EndFluidStack;
import rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.FluidBlobBurst;
import rbasamoyai.createbigcannons.munitions.big_cannon.smoke_shell.SmokeExplosion;
import rbasamoyai.createbigcannons.munitions.fragment_burst.CBCProjectileBurst;

public record FireWarhead(float power) implements IWarhead {
   private static final CompoundTag FIRE_GEL_NBT = PotionUtils.setCustomEffects(
         new ItemStack(Items.POTION), List.of(new MobEffectInstance(MobEffects.WITHER, 30, 0, true, false, true))
      )
      .getTag();
   private static final EndFluidStack FIRE_GEL_STACK = new EndFluidStack(Fluids.LAVA, 2000, FIRE_GEL_NBT);
   private static final int SPREAD = 1;

   @Override
   public boolean detonate(Level level, Vec3 hit, Vec3 dir) {
      SmokeExplosion explosion = new SmokeExplosion(level, null, hit.x, hit.y, hit.z, 2.0F, ExplosionInteraction.NONE);
      CreateBigCannons.handleCustomExplosion(level, explosion);
      int mbPerBlob = 500;
      int mBPerAoeRadius = 125;
      byte blobSize = (byte)((double)FIRE_GEL_STACK.amount() / mBPerAoeRadius);
      int convertCount = IndexPlatform.convertFluid(mbPerBlob);
      int count = (int)Math.ceil((double)FIRE_GEL_STACK.amount() / convertCount);
      FireBlobBurst burst = (FireBlobBurst)CBCProjectileBurst.spawnConeBurst(
         level, (EntityType)AllEntities.FIRE_BLOB_BURST.get(), new Vec3(hit.x, hit.y, hit.z), Vec3.ZERO, count, 1.0
      );
      burst.setFluidStack(FIRE_GEL_STACK.copy(convertCount));
      burst.setBlobSize(blobSize);
      spawnFireCloud(BlockPos.containing(hit), burst, level);
      return true;
   }

   public static void spawnFireCloud(BlockPos pos, FluidBlobBurst blob, Level level) {
      CompoundTag tag = blob.getFluidStack().data();
      GasCloudEntity gasCloud = (GasCloudEntity)CBCEntityTypes.GAS_CLOUD.create(level);
      gasCloud.setPos(Vec3.atCenterOf(pos));
      gasCloud.setWaitTime(10);
      gasCloud.setSize(blob.getBlobSize());
      gasCloud.setDuration(300);
      gasCloud.setPotion(PotionUtils.getPotion(tag));
      gasCloud.setFixedColor(16750848);

      for (MobEffectInstance effect : PotionUtils.getAllEffects(tag)) {
         gasCloud.addEffect(new MobEffectInstance(effect));
      }

      level.addFreshEntity(gasCloud);
   }

   @Override
   public void save(CompoundTag nbt) {
   }

   @Override
   public void read(CompoundTag nbt) {
   }

   @Override
   public void appendHoverText(List<Component> components) {
      components.add(Component.literal("Fire Warhead").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
      components.add(
         Component.translatable(
               "info.tallyho.lethal_radius", new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(MissileRegistry.lethalRadius(this.power))}
            )
            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
      );
   }

   @Override
   public String toString() {
      return "";
   }
}
