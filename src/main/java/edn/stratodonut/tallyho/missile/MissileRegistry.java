package edn.stratodonut.tallyho.missile;

import com.jozufozu.flywheel.core.PartialModel;
import com.tterrag.registrate.util.entry.ItemEntry;
import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.TallyhoMod;
import edn.stratodonut.tallyho.camera.entity.FlexibleSeatEntity;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.item.MissileItem;
import edn.stratodonut.tallyho.missile.fuze.AltitudeFuze;
import edn.stratodonut.tallyho.missile.fuze.ImpactFuze;
import edn.stratodonut.tallyho.missile.fuze.ProximityFuze;
import edn.stratodonut.tallyho.missile.fuze.TopAttackFuze;
import edn.stratodonut.tallyho.missile.guid.AntiRadGroundSeeker;
import edn.stratodonut.tallyho.missile.guid.IRSeeker;
import edn.stratodonut.tallyho.missile.guid.LaserSeeker;
import edn.stratodonut.tallyho.missile.guid.SACLOS;
import edn.stratodonut.tallyho.missile.motor.RocketMotor;
import edn.stratodonut.tallyho.missile.motor.RocketMotorNoLift;
import edn.stratodonut.tallyho.missile.motor.TailRetarder;
import edn.stratodonut.tallyho.missile.warhead.ClusterWarhead;
import edn.stratodonut.tallyho.missile.warhead.DualPurposeWarhead;
import edn.stratodonut.tallyho.missile.warhead.FireWarhead;
import edn.stratodonut.tallyho.missile.warhead.FragWarhead;
import edn.stratodonut.tallyho.missile.warhead.HighExplosiveWarhead;
import edn.stratodonut.tallyho.missile.warhead.ShapedChargeWarhead;
import edn.stratodonut.tallyho.missile.warhead.TopAttackWarhead;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class MissileRegistry {
   private static final HashMap<String, MissileRegistry.MissileRegistryEntry> entries = new HashMap<>();
   public static MissileRegistry.MissileRegistryEntry AIM_9L = register(
      new MissileRegistry.MissileRegistryEntry("aim9lima", new FragWarhead(8.0F, 0.1F))
         .withGuidance(new IRSeeker.Factory(5, 43, 0.0192, 4, 0.65F, 640, 0.9F))
         .withMotor(new RocketMotor.Factory(1.0F, 5, false).offsetRender(1.5F))
         .withFuze(new ProximityFuze.Factory(9.0F, 2))
         .withSize(0.2F)
   );
   public static MissileRegistry.MissileRegistryEntry FIM_92E = register(
      new MissileRegistry.MissileRegistryEntry("fim92e", new FragWarhead(4.0F, 0.3F))
         .withGuidance(new IRSeeker.Factory(3, 40, 0.01047, 4, 0.35F, 310, 0.22F))
         .withMotor(new RocketMotor.Factory(0.87125F, 2, false).offsetRender(1.0F))
         .withSize(0.2F)
   );
   public static MissileRegistry.MissileRegistryEntry AGM_114B = register(
      new MissileRegistry.MissileRegistryEntry("agm114b", new ShapedChargeWarhead(110.0F, 5.0F))
         .withGuidance(new LaserSeeker.Factory(30, 45, 0.02, 0.14715F, 420))
         .withMotor(new RocketMotor.Factory(0.13333334F, 30, true))
         .withSize(0.5F)
   );
   public static MissileRegistry.MissileRegistryEntry AGM_114M = register(
      new MissileRegistry.MissileRegistryEntry("agm114m", new FragWarhead(18.0F, 0.8F))
         .withGuidance(new LaserSeeker.Factory(30, 45, 0.02, 0.14715F, 420))
         .withMotor(new RocketMotor.Factory(0.13333334F, 30, true))
         .withSize(0.5F)
   );
   public static MissileRegistry.MissileRegistryEntry APKWS = register(
      new MissileRegistry.MissileRegistryEntry("apkws", new ShapedChargeWarhead(36.0F, 3.0F))
         .withGuidance(new LaserSeeker.Factory(40, 0, 1.0, 0.04905F, 400))
         .withMotor(new RocketMotor.Factory(0.47142857F, 7, true, 100.0).offsetRender(0.5F))
         .withDeployModel()
         .withSize(0.2F)
   );
   public static MissileRegistry.MissileRegistryEntry KATYUSHA = register(
      new MissileRegistry.MissileRegistryEntry("katyusha", new HighExplosiveWarhead(8.0F))
         .withMotor(new RocketMotorNoLift.Factory(0.23333333F, 30, 0.05F, false, 200.0F))
         .withLongRange()
         .withSize(0.2F)
   );
   public static MissileRegistry.MissileRegistryEntry SNAKEYE = register(
      new MissileRegistry.MissileRegistryEntry("snakeye", new HighExplosiveWarhead(16.0F))
         .withMotor(new TailRetarder.Factory())
         .withDeployModel()
         .withSize(0.5F)
   );
   public static MissileRegistry.MissileRegistryEntry ROCKEYE = register(
      new MissileRegistry.MissileRegistryEntry("rockeye", new ClusterWarhead(new ShapedChargeWarhead(24.0F, 3.0F), 247, 160.0F))
         .withFuze(new AltitudeFuze.Factory(200.0F, 0))
         .withSize(0.5F)
         .withDeployModel()
   );
   public static MissileRegistry.MissileRegistryEntry CBU_87 = register(
      new MissileRegistry.MissileRegistryEntry(
            "cbu87", new ClusterWarhead(new DualPurposeWarhead(new ShapedChargeWarhead(16.0F, 2.0F), new FragWarhead(8.0F, 0.9F)), 202, 120.0F)
         )
         .withFuze(new AltitudeFuze.Factory(200.0F, 0))
         .withDeployModel()
         .withSize(0.5F)
   );
   public static MissileRegistry.MissileRegistryEntry MK_77 = register(new MissileRegistry.MissileRegistryEntry("mk77", new FireWarhead(24.0F)).withSize(0.5F));
   public static MissileRegistry.MissileRegistryEntry AN_M64 = register(
      new MissileRegistry.MissileRegistryEntry("m64", new HighExplosiveWarhead(16.0F)).withSize(0.5F)
   );
   public static MissileRegistry.MissileRegistryEntry GBU_12 = register(
      new MissileRegistry.MissileRegistryEntry("gbu12", new HighExplosiveWarhead(16.0F))
         .withGuidance(new LaserSeeker.Factory(20, 0, 8.72, 0.1F, 400))
         .withDeployModel()
         .withSize(0.5F)
   );
   public static MissileRegistry.MissileRegistryEntry GBU_24 = register(
      new MissileRegistry.MissileRegistryEntry("gbu24", new HighExplosiveWarhead(36.0F))
         .withGuidance(new LaserSeeker.Factory(20, 0, 8.72, 0.1F, 400))
         .withDeployModel()
   );
   public static MissileRegistry.MissileRegistryEntry AGM_88 = register(
      new MissileRegistry.MissileRegistryEntry("agm88", new HighExplosiveWarhead(14.0F))
         .withGuidance(new AntiRadGroundSeeker.Factory(20, 3, 0.65F, 2.0F))
         .withMotor(new RocketMotor.Factory(0.13333334F, 120, true).offsetRender(2.0F))
         .withLongRange()
   );
   public static MissileRegistry.MissileRegistryEntry TOW_2A = register(
      new MissileRegistry.MissileRegistryEntry("tow2a", new ShapedChargeWarhead(81.0F, 4.0F))
         .withGuidance(new SACLOS.Factory(SACLOS.ControlMedium.WIRE, 0.049F, 500))
         .withMotor(new RocketMotor.Factory(0.1F, 20, true))
         .withDeployModel()
         .withSize(0.25F)
   );
   public static MissileRegistry.MissileRegistryEntry TOW_2B = register(
      new MissileRegistry.MissileRegistryEntry("tow2b", new TopAttackWarhead(24.0F, 5.0F))
         .withGuidance(new SACLOS.Factory(SACLOS.ControlMedium.WIRE, 0.049F, 500, new Vec3(0.0, 2.0, 0.0)))
         .withMotor(new RocketMotor.Factory(0.1F, 20, true))
         .withFuze(new TopAttackFuze.Factory(3.0F, 3.0F))
         .withDeployModel()
         .withSize(0.25F)
   );
   public static MissileRegistry.MissileRegistryEntry TOW_BB = register(
      new MissileRegistry.MissileRegistryEntry("towbb", new HighExplosiveWarhead(8.0F))
         .withGuidance(new SACLOS.Factory(SACLOS.ControlMedium.WIRE, 0.049F, 500))
         .withMotor(new RocketMotor.Factory(0.1F, 20, true))
         .withDeployModel()
         .withSize(0.25F)
   );

   public static void register() {
   }

   public static <T extends MountedMissileEntity> MissileRegistry.MissileRegistryEntry register(MissileRegistry.MissileRegistryEntry entry) {
      entries.put(entry.id, entry);
      return entry;
   }

   @Nullable
   public static MissileRegistry.MissileRegistryEntry getEntry(String id) {
      return entries.containsKey(id) ? entries.get(id) : null;
   }

   @Nullable
   public static MissileRegistry.MissileRegistryEntry getEntry(MountedMissileEntity mme) {
      return mme.getMissileId() != null && getEntry(mme.getMissileId()) != null ? getEntry(mme.getMissileId()) : null;
   }

   @OnlyIn(Dist.CLIENT)
   @Nonnull
   public static Optional<BakedModel> getDeployModel(MountedMissileEntity mme) {
      MissileRegistry.MissileRegistryEntry e = getEntry(mme);
      return e != null ? e.getModel() : Optional.empty();
   }

   public static float lethalRadius(float power) {
      return (float)(power * (Math.sqrt(9.0F - 4.0F * (20.0F / (7.0F * power) - 2.0F)) - 3.0));
   }

   public static class MissileRegistryEntry {
      public final String id;
      public final IWarhead warhead;
      private IGuidanceFactory<?> guidanceFactory;
      private IMotorFactory<?> motorFactory;
      private IFuzeFactory<?> fuzeFactory = new ImpactFuze.Factory();
      public final MissileRegistry.MissileRegistryEntry.MissileFactory<MountedMissileEntity> factory;
      private PartialModel deployModel = null;
      private float hitboxScale = 1.0F;
      private boolean longRange = false;
      private final Supplier<EntityType<MountedMissileEntity>> entityType;
      private final ItemEntry<MissileItem> itemEntry;

      public MissileRegistryEntry(String id, IWarhead warhead) {
         if (id.contains(" ")) {
            throw new IllegalArgumentException("Spaces not allowed in missile id!");
         }

         this.id = id;
         this.warhead = warhead;
         this.factory = MountedMissileEntity::new;
         this.entityType = AllEntities.MISSILE_ENTITY;
         this.itemEntry = TallyhoMod.REGISTRATE.item(id, p -> new MissileItem(p, id)).properties(p -> p.stacksTo(1)).register();
      }

      protected MissileRegistry.MissileRegistryEntry withGuidance(IGuidanceFactory<?> g) {
         this.guidanceFactory = g;
         return this;
      }

      protected MissileRegistry.MissileRegistryEntry withMotor(IMotorFactory<?> m) {
         this.motorFactory = m;
         return this;
      }

      protected MissileRegistry.MissileRegistryEntry withFuze(IFuzeFactory<?> f) {
         this.fuzeFactory = f;
         return this;
      }

      protected MissileRegistry.MissileRegistryEntry withLongRange() {
         this.longRange = true;
         return this;
      }

      protected MissileRegistry.MissileRegistryEntry withSize(float s) {
         this.hitboxScale = s;
         return this;
      }

      protected MissileRegistry.MissileRegistryEntry withDeployModel() {
         DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.deployModel = new PartialModel(TallyhoMod.asResource("item/missile/deploy/" + this.id)));
         return this;
      }

      public ItemEntry<MissileItem> getItemEntry() {
         return this.itemEntry;
      }

      public boolean isLongRange() {
         return this.longRange;
      }

      @Nullable
      public GuidanceComponent getGuidance() {
         return this.guidanceFactory != null ? this.guidanceFactory.create() : null;
      }

      @Nullable
      public MotorComponent getMotor() {
         return this.motorFactory != null ? this.motorFactory.create() : null;
      }

      @Nullable
      public FuzeComponent getFuze() {
         return this.fuzeFactory != null ? this.fuzeFactory.create() : null;
      }

      public float getHitboxScale() {
         return this.hitboxScale;
      }

      @OnlyIn(Dist.CLIENT)
      @Nonnull
      public Optional<BakedModel> getModel() {
         return Optional.ofNullable(this.deployModel).map(PartialModel::get);
      }

      public void appendHoverText(List<Component> components) {
         if (this.guidanceFactory != null) {
            this.guidanceFactory.appendHoverText(components);
         }

         if (this.fuzeFactory != null) {
            this.fuzeFactory.appendHoverText(components);
         }

         this.warhead.appendHoverText(components);
         if (this.motorFactory != null) {
            this.motorFactory.appendHoverText(components);
         }
      }

      @Nonnull
      public MountedMissileEntity spawn(ServerLevel serverLevel, Vec3 pos, float base_yaw) {
         MountedMissileEntity missile = this.factory.create(this.entityType.get(), serverLevel, this.id, (Item)this.itemEntry.get());
         missile.setPos(pos);
         serverLevel.addFreshEntity(missile);
         FlexibleSeatEntity.sitDown(serverLevel, pos, missile);
         missile.setYRot(base_yaw);
         missile.setXRot(0.0F);
         return missile;
      }

      public interface MissileFactory<T extends MountedMissileEntity> {
         T create(EntityType<T> var1, Level var2, String var3, Item var4);
      }
   }
}
