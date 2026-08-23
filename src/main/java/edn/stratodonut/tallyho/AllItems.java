package edn.stratodonut.tallyho;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import edn.stratodonut.tallyho.camera.item.Periscope360Item;
import edn.stratodonut.tallyho.camera.item.PeriscopeArcItem;
import edn.stratodonut.tallyho.camera.item.RemoteCameraItem;
import edn.stratodonut.tallyho.camera.item.RemoteStationItem;
import edn.stratodonut.tallyho.camera.item.TargetPodRemote;
import edn.stratodonut.tallyho.camera.item.TurretCameraItem;
import edn.stratodonut.tallyho.item.BearingJackItem;
import edn.stratodonut.tallyho.item.CoaxMGItem;
import edn.stratodonut.tallyho.item.GunMountItem;
import edn.stratodonut.tallyho.item.HullMGItem;
import edn.stratodonut.tallyho.item.LaserDesignatorItem;
import edn.stratodonut.tallyho.item.MPCDProgrammerItem;
import edn.stratodonut.tallyho.item.SupportCallerItem;
import edn.stratodonut.tallyho.item.TripodMountItem;
import net.minecraft.world.item.Item;

public class AllItems {
    public static final ItemEntry<MPCDProgrammerItem> MPCD_PROGRAMMER_ITEM;

    public static final ItemEntry<BearingJackItem> BEARING_JACK;

    public static final ItemEntry<TripodMountItem> TRIPOD_ITEM;

    public static final ItemEntry<GunMountItem> GUN_MOUNT_ITEM;

    public static final ItemEntry<LaserDesignatorItem> LASER_DESIGNATOR_ITEM;

    public static final ItemEntry<CoaxMGItem> COAX_MG_ITEM;

    public static final ItemEntry<SupportCallerItem> SUPPORT_RADIO_ITEM;

    public static final ItemEntry<HullMGItem> HULL_MG_ITEM;

    public static final ItemEntry<PeriscopeArcItem> PERISCOPE_ARC_ITEM;

    public static final ItemEntry<Periscope360Item> PERISCOPE_360_ITEM;

    public static final ItemEntry<RemoteCameraItem> REMOTE_CAMERA_ITEM;

    public static final ItemEntry<TurretCameraItem> TURRET_CAMERA_ITEM;

    public static final ItemEntry<RemoteStationItem> REMOTE_STATION_ITEM;

    public static final ItemEntry<TargetPodRemote> TARGET_POD_REMOTE;

    public static void register() {}

    static {
        MPCD_PROGRAMMER_ITEM = TallyhoMod.REGISTRATE.item("mpcd_programmer", MPCDProgrammerItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        BEARING_JACK = TallyhoMod.REGISTRATE.item("bearing_jack", BearingJackItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        TRIPOD_ITEM = TallyhoMod.REGISTRATE.item("tripod_mount", TripodMountItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        GUN_MOUNT_ITEM = TallyhoMod.REGISTRATE.item("gun_mount", GunMountItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        LASER_DESIGNATOR_ITEM = TallyhoMod.REGISTRATE.item("laser_designator", LaserDesignatorItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        COAX_MG_ITEM = TallyhoMod.REGISTRATE.item("coax_mg", CoaxMGItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        SUPPORT_RADIO_ITEM = TallyhoMod.REGISTRATE.item("support_radio", SupportCallerItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        HULL_MG_ITEM = TallyhoMod.REGISTRATE.item("hull_mg", HullMGItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        PERISCOPE_ARC_ITEM = TallyhoMod.REGISTRATE.item("periscope_arc_item", PeriscopeArcItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        PERISCOPE_360_ITEM = TallyhoMod.REGISTRATE.item("periscope_360_item", Periscope360Item::new)
                .properties(p -> p.stacksTo(1))
                .register();

        REMOTE_CAMERA_ITEM = TallyhoMod.REGISTRATE.item("remote_camera", RemoteCameraItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        TURRET_CAMERA_ITEM = TallyhoMod.REGISTRATE.item("turret_remote", TurretCameraItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        REMOTE_STATION_ITEM = TallyhoMod.REGISTRATE.item("crows_item", RemoteStationItem::new)
                .properties(p -> p.stacksTo(1))
                .register();

        TARGET_POD_REMOTE = TallyhoMod.REGISTRATE.item("tgp_remote", TargetPodRemote::new)
                .properties(p -> p.stacksTo(1))
                .register();
    }
}