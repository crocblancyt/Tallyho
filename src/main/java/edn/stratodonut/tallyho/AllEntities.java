package edn.stratodonut.tallyho;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;
import edn.stratodonut.tallyho.missile.warhead.FireBlobBurst;
import edn.stratodonut.tallyho.camera.entity.*;
import edn.stratodonut.tallyho.entity.*;
import edn.stratodonut.tallyho.render.*;
import kotlin.Suppress;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import org.openjdk.nashorn.internal.ir.annotations.Ignore;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.config.MunitionPropertiesHandler;
import rbasamoyai.createbigcannons.munitions.config.PropertiesTypeHandler;
import org.valkyrienskies.mod.client.EmptyRenderer;

public class AllEntities {
    private static final CreateRegistrate REGISTRATE = TallyhoMod.REGISTRATE;

    public static final EntityEntry<ObjectiveMarshalEntity> MARSHAL_ENTITY;
    public static final EntityEntry<Entity> GUN_MOUNT_ENTITY;
    public static final EntityEntry<Entity> TRIPOD_MOUNT_ENTITY;
    public static final EntityEntry<Entity> COAX_MACHINE_GUN_ENTITY;
    public static final EntityEntry<Entity> HULL_MACHINE_GUN_ENTITY;
    public static final EntityEntry<Entity> HELI_CHIN_GUN_ENTITY;
    public static final EntityEntry<Entity> CROWS_ENTITY;
    public static final EntityEntry<Entity> TARGETING_POD_ENTITY;
    public static final EntityEntry<Entity> MISSILE_ENTITY;
    public static final EntityEntry<Entity> LASER_POINT_ENTITY;
    public static final EntityEntry<FlareEntity> FLARE_ENTITY;
    public static final EntityEntry<FireBlobBurst> FIRE_BLOB_BURST;
    public static final EntityEntry<Entity> CAMERA_SEAT_ENTITY;
    public static final EntityEntry<CannonScopeEntity> CANNON_SCOPE_ENTITY;
    public static final EntityEntry<ThermalsEntity> THERMALS_ENTITY;
    public static final EntityEntry<PeriscopeEntity> PERISCOPE_ENTITY;
    public static final EntityEntry<RemoteCameraEntity> REMOTE_CAMERA;

    static {
        MARSHAL_ENTITY = REGISTRATE.entity("marshal", ObjectiveMarshalEntity::new, MobCategory.MISC)
            .renderer(() -> EmptyRenderer::new)
            .properties(b -> b
                    .setTrackingRange(32)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .sized(1.0F, 1.0F))
            .register();

        GUN_MOUNT_ENTITY = REGISTRATE.entity("gun_mount", GunMountEntity::new, MobCategory.MISC)
            .renderer(() -> GunMountRenderer::new)
            .properties(b -> b
                    .setTrackingRange(8)
                    .updateInterval(6)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .sized(0.5F, 0.1F))
            .register();

        TRIPOD_MOUNT_ENTITY = REGISTRATE.entity("tripod_mount", TripodEntity::new, MobCategory.MISC)
                .renderer(() -> TripodMountRenderer::new)
                .properties(b -> b
                    .setTrackingRange(8)
                    .updateInterval(6)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .sized(1.0F, 0.5F))
            .register();

        COAX_MACHINE_GUN_ENTITY = REGISTRATE.entity("coax_mg", CoaxMachineGunEntity::new, MobCategory.MISC)
                .renderer(() -> MGEntityRenderer::new)
                .properties(b -> b
                    .setTrackingRange(4)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .sized(0.5F, 0.5F))
            .register();

        HULL_MACHINE_GUN_ENTITY = REGISTRATE.entity("hull_mg", HullMachineGunEntity::new, MobCategory.MISC)
            .renderer(() -> MGEntityRenderer::new)
            .properties(b -> b
                    .setTrackingRange(32)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .sized(1.2F, 0.5F))
            .register();

        HELI_CHIN_GUN_ENTITY = REGISTRATE.entity("chin_turret", HeliChinTurretEntity::new, MobCategory.MISC)
            .renderer(() -> HeliTurretRenderer::new)
            .properties(b -> b
                    .setTrackingRange(32)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .sized(1.0F, 1.0F))
            .register();

        CROWS_ENTITY = REGISTRATE.entity("crows_turret", RemoteStationEntity::new, MobCategory.MISC)
            .renderer(() -> RemoteStationRenderer::new)
            .properties(b -> b
                    .setTrackingRange(32)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .noSave()
                    .sized(1.0F, 1.0F))
            .register();

        TARGETING_POD_ENTITY = REGISTRATE.entity("targeting_pod", TargetingPodEntity::new, MobCategory.MISC)
                .renderer(() -> TargetPodRenderer::new)
                .properties(b -> b
                    .setTrackingRange(32)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .sized(1.0F, 1.0F))
                .register();

        MISSILE_ENTITY = REGISTRATE.entity("missile", MountedMissileEntity::new, MobCategory.MISC)
            .renderer(() -> MissileEntityRenderer::new)
            .properties(b -> b
                    .clientTrackingRange(32)
                    .setTrackingRange(32)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .noSave()
                    .sized(1.0F, 1.0F))
            .register();

        LASER_POINT_ENTITY = REGISTRATE.entity("laser_point", LaserPointEntity::new, MobCategory.MISC)
            .renderer(() -> EmptyRenderer::new)
            .properties(b -> b
                    .setTrackingRange(0)
                    .noSave()
                    .noSummon()
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .setCustomClientFactory((packet, level) -> new LaserPointEntity(level))
                    .sized(0.2F, 0.2F))
            .register();

        FLARE_ENTITY = REGISTRATE.entity("flare", FlareEntity::new, MobCategory.MISC)
            .renderer(() -> EmptyRenderer::new)
            .properties(b -> b
                    .setTrackingRange(128)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .sized(0.2F, 0.2F))
            .register();

        FIRE_BLOB_BURST = CreateBigCannons.REGISTRATE.entity("fire_blob_burst", FireBlobBurst::new, MobCategory.MISC)
            .properties(b -> b
                    .sized(0.8F, 0.8F)
                    .noSummon()
                    .noSave()
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(128))
            .renderer(() -> NoopRenderer::new)
            .onRegister(type -> MunitionPropertiesHandler.registerProjectileHandler(type, (PropertiesTypeHandler)CBCMunitionPropertiesHandlers.PROJECTILE_BURST))
            .register();

        CAMERA_SEAT_ENTITY = REGISTRATE.entity("camera_seat", FlexibleSeatEntity::new, MobCategory.MISC)
            .renderer(() -> FlexibleSeatEntity.Render::new)
            .properties(b -> b
                    .setTrackingRange(128)
                    .updateInterval(Integer.MAX_VALUE)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .noSave())
            .properties(FlexibleSeatEntity::build)
            .register();

        CANNON_SCOPE_ENTITY = (EntityEntry<CannonScopeEntity>) REGISTRATE.entity("cannon_scope", (EntityType.EntityFactory<? extends Entity>) CannonScopeEntity::new, MobCategory.MISC)
            .renderer(() -> org.valkyrienskies.mod.client.EmptyRenderer::new)
                .properties(b -> b
                    .setTrackingRange(32)
                    .updateInterval(Integer.MAX_VALUE)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .noSave()
                    .sized(0.5F, 0.5F))
            .register();

        THERMALS_ENTITY = REGISTRATE.entity("thermal_camera", ThermalsEntity::new, MobCategory.MISC)
            .renderer(() -> org.valkyrienskies.mod.client.EmptyRenderer::new)
            .properties(b -> b
                    .setTrackingRange(32)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .noSave()
                    .sized(0.5F, 0.5F))
            .register();

        PERISCOPE_ENTITY = REGISTRATE.entity("periscope_arc", PeriscopeEntity::new, MobCategory.MISC)
            .renderer(() -> edn.stratodonut.tallyho.camera.model.PeriscopeEntityRenderer::new)
            .properties(b -> b
                    .setTrackingRange(32)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .noSave()
                    .sized(0.5F, 0.5F))
            .register();

        REMOTE_CAMERA = REGISTRATE.entity("remote_camera", RemoteCameraEntity::new, MobCategory.MISC)
            .renderer(() -> org.valkyrienskies.mod.client.EmptyRenderer::new)
            .properties(b -> b
                    .setTrackingRange(128)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .noSave()
                    .sized(0.5F, 0.5F))
            .register();
    }

    public static void register() {}
}