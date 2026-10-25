package edn.stratodonut.tallyho.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;

public enum CreateTypePacketHandler {
   SEAT_ADJUST(SeatAdjustPacket.class, SeatAdjustPacket::new, NetworkDirection.PLAY_TO_SERVER),
   SEAT_SYNC(SeatSyncPacket.class, SeatSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   MISSILE_TONE(MissileTonePacket.class, MissileTonePacket::new, NetworkDirection.PLAY_TO_CLIENT),
   MISSILE_VELOCITY(MissileVelocityPacket.class, MissileVelocityPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   LIMIT_F5(LimitF5Packet.class, LimitF5Packet::new, NetworkDirection.PLAY_TO_CLIENT),
   MOVEMENT_AFFECT_AIM(MovementAffectAimPacket.class, MovementAffectAimPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   PRONE_CLIPPING_GAMERULE(ProneClipGamerulePacket.class, ProneClipGamerulePacket::new, NetworkDirection.PLAY_TO_CLIENT),
   SUPPRESS(SuppressionPacket.class, SuppressionPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   EXP_SUPPRESS(ExplosionSuppressPacket.class, ExplosionSuppressPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   DESIGNATE(DesignatePacket.class, DesignatePacket::new, NetworkDirection.PLAY_TO_SERVER),
   GRAVITRON_STATIC(GravitronStaticPacket.class, GravitronStaticPacket::new, NetworkDirection.PLAY_TO_SERVER),
   RECOIL_CANNON(RecoilingCannonUpdatePacket.class, RecoilingCannonUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT),
   SMS_LAUNCH(SMSCommandPacket.class, SMSCommandPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CAMERA_DISMOUNT(DismountCameraPacket.class, DismountCameraPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CAMERA_MOVE_S(CameraMovePacket.class, CameraMovePacket::new, NetworkDirection.PLAY_TO_SERVER),
   CAMERA_MOVE_C(CameraMovePacketClient.class, CameraMovePacketClient::new, NetworkDirection.PLAY_TO_CLIENT),
   CAMERA_SET_VIEW(SetCameraViewPacket.class, SetCameraViewPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   GUNFIRE(ServerboundGunFirePacket.class, ServerboundGunFirePacket::new, NetworkDirection.PLAY_TO_SERVER),
   MOUNT_AMMO_SYNC(GunMountAmmoSyncPacket.class, GunMountAmmoSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   STABSYNC_S(StabiliserSyncPacket.class, StabiliserSyncPacket::new, NetworkDirection.PLAY_TO_SERVER),
   STABSYNC_C(StabiliserSyncPacketClient.class, StabiliserSyncPacketClient::new, NetworkDirection.PLAY_TO_CLIENT),
   TGPCHANGE_S(TGPChangeModePacket.class, TGPChangeModePacket::new, NetworkDirection.PLAY_TO_SERVER),
   TGPCHANGE_C(TGPChangeModePacketClient.class, TGPChangeModePacketClient::new, NetworkDirection.PLAY_TO_CLIENT),
   TGPLAZE(TGPLazeTogglePacket.class, TGPLazeTogglePacket::new, NetworkDirection.PLAY_TO_SERVER),
   RANGE(RangePacket.class, RangePacket::new, NetworkDirection.PLAY_TO_SERVER);

   public static final ResourceLocation CHANNEL_NAME = new ResourceLocation("tallyho", "main");
   public static final int NETWORK_VERSION = 3;
   public static final String NETWORK_VERSION_STR = String.valueOf(3);
   private static SimpleChannel channel;
   private final CreateTypePacketHandler.PacketType<?> packetType;

   <T extends SimplePacketBase> CreateTypePacketHandler(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
      this.packetType = new CreateTypePacketHandler.PacketType<>(type, factory, direction);
   }

   public static void registerPackets() {
      channel = ChannelBuilder.named(CHANNEL_NAME)
         .serverAcceptedVersions(NETWORK_VERSION_STR::equals)
         .clientAcceptedVersions(NETWORK_VERSION_STR::equals)
         .networkProtocolVersion(() -> NETWORK_VERSION_STR)
         .simpleChannel();

      for (CreateTypePacketHandler packet : values()) {
         packet.packetType.register();
      }
   }

   public static SimpleChannel getChannel() {
      return channel;
   }

   private static class PacketType<T extends SimplePacketBase> {
      private static int index = 0;
      private BiConsumer<T, FriendlyByteBuf> encoder = SimplePacketBase::write;
      private Function<FriendlyByteBuf, T> decoder;
      private BiConsumer<T, Supplier<Context>> handler;
      private Class<T> type;
      private NetworkDirection direction;

      private PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
         this.decoder = factory;
         this.handler = (packet, contextSupplier) -> {
            Context context = contextSupplier.get();
            if (packet.handle(context)) {
               context.setPacketHandled(true);
            }
         };
         this.type = type;
         this.direction = direction;
      }

      private void register() {
         CreateTypePacketHandler.getChannel()
            .messageBuilder(this.type, index++, this.direction)
            .encoder(this.encoder)
            .decoder(this.decoder)
            .consumerNetworkThread(this.handler)
            .add();
      }
   }
}
