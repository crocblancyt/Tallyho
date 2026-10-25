package edn.stratodonut.tallyho;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import edn.stratodonut.tallyho.camera.entity.CannonScopeEntity;
import edn.stratodonut.tallyho.camera.entity.CoaxMachineGunEntity;
import edn.stratodonut.tallyho.camera.entity.FlexibleSeatEntity;
import edn.stratodonut.tallyho.camera.entity.HeliChinTurretEntity;
import edn.stratodonut.tallyho.camera.entity.HullMachineGunEntity;
import edn.stratodonut.tallyho.camera.entity.PeriscopeEntity;
import edn.stratodonut.tallyho.camera.entity.RemoteCameraEntity;
import edn.stratodonut.tallyho.camera.entity.RemoteStationEntity;
import edn.stratodonut.tallyho.camera.entity.TargetingPodEntity;
import edn.stratodonut.tallyho.camera.entity.ThermalsEntity;
import edn.stratodonut.tallyho.camera.model.PeriscopeEntityRenderer;
import edn.stratodonut.tallyho.entity.FlareEntity;
import edn.stratodonut.tallyho.entity.GunMountEntity;
import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.entity.MountedMissileEntity;
import edn.stratodonut.tallyho.entity.ObjectiveMarshalEntity;
import edn.stratodonut.tallyho.entity.TripodEntity;
import edn.stratodonut.tallyho.missile.warhead.FireBlobBurst;
import edn.stratodonut.tallyho.render.GunMountRenderer;
import edn.stratodonut.tallyho.render.HeliTurretRenderer;
import edn.stratodonut.tallyho.render.MGEntityRenderer;
import edn.stratodonut.tallyho.render.MissileEntityRenderer;
import edn.stratodonut.tallyho.render.RemoteStationRenderer;
import edn.stratodonut.tallyho.render.TargetPodRenderer;
import edn.stratodonut.tallyho.render.TripodMountRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.valkyrienskies.mod.client.EmptyRenderer;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.config.MunitionPropertiesHandler;

public class AllEntities {
   private static final CreateRegistrate REGISTRATE = TallyhoMod.REGISTRATE;

   public static final EntityEntry<ObjectiveMarshalEntity> MARSHAL_ENTITY = REGISTRATE.entity("marshal", ObjectiveMarshalEntity::new, MobCategory.MISC)
      .renderer(() -> EmptyRenderer::new)
      .properties(b -> ((EntityType.Builder<ObjectiveMarshalEntity>) b).setTrackingRange(32).setUpdateInterval(3).setShouldReceiveVelocityUpdates(false).fireImmune().sized(1.0F, 1.0F))
      .register();

   public static final EntityEntry GUN_MOUNT_ENTITY = REGISTRATE.entity("gun_mount", GunMountEntity::new, MobCategory.MISC)
       .renderer(() -> ((EntityRendererProvider.Context ctx) -> new GunMountRenderer(ctx)))
      .properties(b -> ((EntityType.Builder<Entity>) b).setTrackingRange(8).setUpdateInterval(6).setShouldReceiveVelocityUpdates(true).fireImmune().sized(0.5F, 0.1F))
      .register();

   public static final EntityEntry TRIPOD_MOUNT_ENTITY = REGISTRATE.entity("tripod_mount", TripodEntity::new, MobCategory.MISC)
      .renderer(() -> ((EntityRendererProvider.Context ctx) -> new TripodMountRenderer(ctx)))
      .properties(b -> ((EntityType.Builder<Entity>) b).setTrackingRange(8).setUpdateInterval(6).setShouldReceiveVelocityUpdates(true).fireImmune().sized(1.0F, 0.5F))
      .register();

   public static final EntityEntry COAX_MACHINE_GUN_ENTITY = REGISTRATE.entity("coax_mg", CoaxMachineGunEntity::new, MobCategory.MISC)
      .renderer(() -> MGEntityRenderer::new)
      .properties(b -> ((EntityType.Builder<Entity>) b).setTrackingRange(4).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true).fireImmune().sized(0.5F, 0.5F))
           .register();

   public static final EntityEntry HULL_MACHINE_GUN_ENTITY = REGISTRATE.entity("hull_mg", HullMachineGunEntity::new, MobCategory.MISC)
      .renderer(() -> MGEntityRenderer::new)
      .properties(b -> ((EntityType.Builder<Entity>) b).setTrackingRange(32).setUpdateInterval(3).setShouldReceiveVelocityUpdates(false).fireImmune().sized(1.2F, 0.5F))
      .register();

   public static final EntityEntry HELI_CHIN_GUN_ENTITY = REGISTRATE.entity("chin_turret", HeliChinTurretEntity::new, MobCategory.MISC)
      .renderer(() -> HeliTurretRenderer::new)
      .properties(b -> ((EntityType.Builder<Entity>) b).setTrackingRange(32).setUpdateInterval(3).setShouldReceiveVelocityUpdates(false).fireImmune().sized(1.0F, 1.0F))
      .register();

   public static final EntityEntry CROWS_ENTITY = REGISTRATE.entity("crows_turret", RemoteStationEntity::new, MobCategory.MISC)
      .renderer(() -> RemoteStationRenderer::new)
      .properties(b -> ((EntityType.Builder<Entity>) b).setTrackingRange(32).setUpdateInterval(3).setShouldReceiveVelocityUpdates(false).fireImmune().noSummon().sized(1.0F, 1.0F))
      .register();

   public static final EntityEntry TARGETING_POD_ENTITY = REGISTRATE.entity("targeting_pod", TargetingPodEntity::new, MobCategory.MISC)
      .renderer(() -> ((EntityRendererProvider.Context ctx) -> new TargetPodRenderer(ctx)))
      .properties(b -> ((EntityType.Builder<Entity>) b).setTrackingRange(32).setUpdateInterval(3).setShouldReceiveVelocityUpdates(false).fireImmune().sized(1.0F, 1.0F))
      .register();

   public static final EntityEntry MISSILE_ENTITY = REGISTRATE.entity("missile", MountedMissileEntity::new, MobCategory.MISC)
      .renderer(() -> ((EntityRendererProvider.Context ctx) -> new MissileEntityRenderer(ctx)))
      .properties(
              b -> ((EntityType.Builder<Entity>) b).setTrackingRange(32)
            .clientTrackingRange(32)
            .setUpdateInterval(3)
            .setShouldReceiveVelocityUpdates(false)
            .fireImmune()
            .noSummon()
            .sized(1.0F, 1.0F)
      )
      .register();
   public static final EntityEntry<LaserPointEntity> LASER_POINT_ENTITY = REGISTRATE.entity("laser_point", LaserPointEntity::new, MobCategory.MISC)
      .renderer(() -> EmptyRenderer::new)
      .properties(
         b -> b.setTrackingRange(0)
            .noSummon()
            .noSave()
            .setShouldReceiveVelocityUpdates(false)
            .fireImmune()
            .setCustomClientFactory((spawnEntity, world) -> null)
            .sized(0.2F, 0.2F)
      )
      .register();
   public static final EntityEntry<FlareEntity> FLARE_ENTITY = REGISTRATE.entity("flare", FlareEntity::new, MobCategory.MISC)
      .renderer(() -> EmptyRenderer::new)
      .properties(b -> b.setTrackingRange(128).setShouldReceiveVelocityUpdates(false).fireImmune().sized(0.2F, 0.2F))
      .register();

   public static final EntityEntry<FireBlobBurst> FIRE_BLOB_BURST = ((EntityBuilder)CreateBigCannons.REGISTRATE
         .entity("fire_blob_burst", FireBlobBurst::new, MobCategory.MISC)
         .properties(b -> b.sized(0.8F, 0.8F).fireImmune().updateInterval(1).setShouldReceiveVelocityUpdates(true).setTrackingRange(128))
         .renderer(() -> NoopRenderer::new)
         .onRegister(type -> MunitionPropertiesHandler.registerProjectileHandler(type, CBCMunitionPropertiesHandlers.PROJECTILE_BURST)))
      .register();



   public static final EntityEntry CAMERA_SEAT_ENTITY = REGISTRATE.entity("camera_seat", FlexibleSeatEntity::new, MobCategory.MISC)
      .renderer(() -> ((EntityRendererProvider.Context ctx) -> (EntityRenderer) new FlexibleSeatEntity.Render(ctx)))
      .properties(b -> FlexibleSeatEntity.build((EntityType.Builder<Entity>) b).setTrackingRange(128).setUpdateInterval(Integer.MAX_VALUE).setShouldReceiveVelocityUpdates(false).fireImmune().noSummon())
      .register();

   public static final EntityEntry CANNON_SCOPE_ENTITY = REGISTRATE.entity("cannon_scope", CannonScopeEntity::new, MobCategory.MISC)
      .renderer(() -> EmptyRenderer::new)
      .properties(
         b -> ((EntityType.Builder<Entity>) b).setTrackingRange(32).setUpdateInterval(Integer.MAX_VALUE).setShouldReceiveVelocityUpdates(false).fireImmune().noSummon().sized(0.5F, 0.5F)
      )
      .register();

   public static final EntityEntry<ThermalsEntity> THERMALS_ENTITY = REGISTRATE.entity("thermal_camera", ThermalsEntity::new, MobCategory.MISC)
      .renderer(() -> EmptyRenderer::new)
      .properties(b -> b.setTrackingRange(32).setUpdateInterval(3).setShouldReceiveVelocityUpdates(false).fireImmune().noSummon().sized(0.5F, 0.5F))
      .register();

   public static final EntityEntry<PeriscopeEntity> PERISCOPE_ENTITY = REGISTRATE.entity("periscope_arc", PeriscopeEntity::new, MobCategory.MISC)
      .renderer(() -> PeriscopeEntityRenderer::new)
      .properties(b -> b.setTrackingRange(32).setUpdateInterval(3).setShouldReceiveVelocityUpdates(false).fireImmune().noSummon().sized(0.5F, 0.5F))
      .register();

   public static final EntityEntry<RemoteCameraEntity> REMOTE_CAMERA = REGISTRATE.entity("remote_camera", RemoteCameraEntity::new, MobCategory.MISC)
      .renderer(() -> EmptyRenderer::new)
      .properties(b -> b.setTrackingRange(128).setUpdateInterval(3).setShouldReceiveVelocityUpdates(false).fireImmune().noSummon().sized(0.5F, 0.5F))
      .register();

   public static void register() {
   }
}
