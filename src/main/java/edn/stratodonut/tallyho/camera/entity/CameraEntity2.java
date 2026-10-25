package edn.stratodonut.tallyho.camera.entity;

import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.CameraClientHandler;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.SetCameraViewPacket;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public abstract class CameraEntity2 extends Entity implements IEntityAdditionalSpawnData {
   private float BASE_YAW;
   private AngleLimits ANGLE_LIMIT = new AngleLimits(0.0F, 0.0F, 0.0F);
   private int FOV;
   public static final List<Player> RECENTLY_DISMOUNTED_PLAYERS = new ArrayList<>();
   @Nonnull
   protected WeakReference<ServerPlayer> currentlyViewing = new WeakReference<>(null);
   public SectionPos oldSectionPos = null;
   public double firstGoodX;
   public double firstGoodY;
   public double firstGoodZ;
   public double lastGoodX;
   public double lastGoodY;
   public double lastGoodZ;
   public int receivedMovePacketCount;
   public int knownMovePacketCount;
   public double xLast;
   public double yLast;
   public double zLast;
   public double xRotLast;
   public double yRotLast;
   int timeout = 0;

   public CameraEntity2(EntityType<? extends Entity> p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
   }

   public static boolean hasRecentlyDismounted(Player player) {
      return RECENTLY_DISMOUNTED_PLAYERS.remove(player);
   }

   protected void setParams(float base_yaw, @Deprecated CameraEntity2.PostFXType optic_type, AngleLimits angle_limit, int fov) {
      this.ANGLE_LIMIT = angle_limit;
      this.BASE_YAW = base_yaw;
      this.FOV = fov;
   }

   public boolean startViewing(ServerPlayer player) {
      ServerPlayer current = this.currentlyViewing.get();
      if (current != null && current.getCamera() == this && current.isAlive() && current != player) {
         return false;
      }

      ServerLevel serverLevel = player.serverLevel();
      if (serverLevel != this.level()) {
         return false;
      }

      this.currentlyViewing = new WeakReference<>(player);
      this.oldSectionPos = null;
      SectionPos chunkPos = SectionPos.of(this.blockPosition());
      int viewDistance = player.server.getPlayerList().getViewDistance();
      if (player.getCamera() instanceof CameraEntity2 conductor) {
         conductor.stopViewing(player);
      }

      player.camera = this;
      CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> player), new SetCameraViewPacket(this));
      this.resetPosition();
      return true;
   }

   protected float getEyeHeight(@NotNull Pose p, EntityDimensions dim) {
      return dim.height;
   }

   public double getMyRidingOffset() {
      return -0.25;
   }

   public void stopViewing(ServerPlayer player) {
      if (!this.level().isClientSide) {
         this.currentlyViewing.clear();
         player.camera = player;
         CreateTypePacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> player), new SetCameraViewPacket(player));
         if (this.isShortLived()) {
            this.discard();
         }
      }
   }

   protected boolean isShortLived() {
      return true;
   }

   private void resetPosition() {
      this.firstGoodX = this.lastGoodX = this.getX();
      this.firstGoodY = this.lastGoodY = this.getY();
      this.firstGoodZ = this.lastGoodZ = this.getZ();
      this.knownMovePacketCount = this.receivedMovePacketCount;
   }

   public void tick() {
      this.resetPosition();
      if (this.level().isClientSide) {
         CameraPossessionController.tryUpdatePossession(this);
         this.updatePossessionInputs();
      }

      super.tick();
      if (!this.level().isClientSide) {
         if (this.isShortLived() && this.currentlyViewing.get() == null) {
            this.timeout++;
         }

         if (this.getVehicle() == null) {
            this.timeout++;
         }

         if (this.timeout > 100) {
            this.discard();
         }
      }
   }

   protected void onBelowWorld() {
      if (!this.isPassenger()) {
         super.onBelowWorld();
      }
   }

   public void turn(double p_19885_, double p_19886_) {
      super.turn(p_19885_, p_19886_);
   }

   public void turnView(double yRot, double xRot) {
      this.turnView(yRot, xRot, false);
   }

   public void turnView(double yRot, double xRot, boolean raw) {
      AngleLimits limit = this.getRotationLimit();
      float base_yaw = this.getBaseYaw();
      float sense = !raw && this.getFOV() > 0 ? this.getFOV() / 70.0F : 1.0F;
      float f = (float)xRot * sense;
      float g = (float)yRot * sense;
      this.setXRot(Mth.clamp(this.getXRot() + f, -limit.upperX(), limit.lowerX()));
      if (limit.Y() >= 180.0F) {
         this.setYRot(this.getYRot() + g);
      } else {
         this.setYRot(Mth.rotateIfNecessary(this.getYRot() + g, base_yaw, limit.Y()));
      }

      this.xRotO += f;
      this.yRotO += g;
      this.xRotO = Mth.clamp(this.xRotO, -limit.upperX(), limit.lowerX());
      if (limit.Y() < 180.0F) {
         this.yRotO = Mth.clamp(this.yRotO, base_yaw - limit.Y(), base_yaw + limit.Y());
      }

      if (this.getVehicle() != null) {
         this.getVehicle().onPassengerTurned(this);
      }
   }

   @Nonnull
   public CameraEntity2.PostFXType getOverlayType() {
      return CameraEntity2.PostFXType.NONE;
   }

   public int getFOV() {
      return this.FOV;
   }

   public AngleLimits getRotationLimit() {
      return this.ANGLE_LIMIT;
   }

   public float getBaseYaw() {
      return this.BASE_YAW;
   }

   public void updatePossessionInputs() {
      if (this.isPossessedAndClient()) {
         this._updatePossessionInputs();
      }
   }

   public boolean isFixedByPlayer() {
      return true;
   }

   public boolean isControlledByLocalInstance() {
      return super.isControlledByLocalInstance() || this.isFixedByPlayer() && this.isPossessedAndClient();
   }

   public boolean isPossessed() {
      return this.level().isClientSide ? CameraClientHandler.isPossessed(this) : this.currentlyViewing.get() != null;
   }

   public boolean isPossessedAndClient() {
      return this.level().isClientSide && this.isPossessed();
   }

   @OnlyIn(Dist.CLIENT)
   private void _updatePossessionInputs() {
   }

   public void setDeltaMovement(@Nonnull Vec3 v) {
   }

   protected void defineSynchedData() {
   }

   protected Vec2 getRotationFromDirection(Vec3 dir) {
      Vector3d relPos = VectorConversionsMCKt.toJOML(dir);
      if (VSGameUtilsKt.getShipMountedTo(this) != null) {
         double length = relPos.length();
         Ship s = VSGameUtilsKt.getShipMountedTo(this);
         if (s instanceof ClientShip cs) {
            relPos = cs.getRenderTransform().getWorldToShip().transformDirection(relPos.normalize());
         } else {
            relPos = s.getWorldToShip().transformDirection(relPos.normalize());
         }

         relPos.mul(length);
      }

      double d0 = relPos.x;
      double d1 = relPos.y;
      double d2 = relPos.z;
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      float x = Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * 180.0F / (float)Math.PI)));
      float y = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * 180.0F / (float)Math.PI) - 90.0F);
      return new Vec2(x, y);
   }

   protected Vec2 getLimitedRotationFromDirection(Vec3 dir) {
      Vec2 rawRotation = this.getRotationFromDirection(dir);
      AngleLimits limit = this.getRotationLimit();
      float x = rawRotation.x;
      float y = rawRotation.y;
      x = Mth.clamp(x, -limit.upperX(), limit.lowerX());
      if (limit.Y() < 180.0F) {
         y = Mth.rotateIfNecessary(y, this.getBaseYaw(), limit.Y());
      }

      return !Double.isNaN(x) && !Double.isNaN(y) ? new Vec2(x, y) : new Vec2(0.0F, 0.0F);
   }

   protected Vec2 getLimitedRotationFromTarget(Vec3 target, float partial_tick) {
      return this.getLimitedRotationFromDirection(target.subtract(this.getEyePosition(partial_tick)));
   }

   protected void readAdditionalSaveData(CompoundTag nbt) {
      this.ANGLE_LIMIT = new AngleLimits(nbt.getFloat("ANGLE_LIMIT_LOWER_X"), nbt.getFloat("ANGLE_LIMIT_UPPER_X"), nbt.getFloat("ANGLE_LIMIT_Y"));
      this.BASE_YAW = nbt.getFloat("BASE_YAW");
      this.FOV = nbt.getInt("FOV");
   }

   protected void addAdditionalSaveData(CompoundTag nbt) {
      nbt.putInt("OPTIC_TYPE", this.getOverlayType().id);
      AngleLimits r = this.getRotationLimit();
      nbt.putFloat("ANGLE_LIMIT_LOWER_X", r.lowerX());
      nbt.putFloat("ANGLE_LIMIT_UPPER_X", r.upperX());
      nbt.putFloat("ANGLE_LIMIT_Y", r.Y());
      nbt.putFloat("BASE_YAW", this.getBaseYaw());
      nbt.putInt("FOV", this.getFOV());
   }

   @Nonnull
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public void writeSpawnData(FriendlyByteBuf buffer) {
      CompoundTag nbt = new CompoundTag();
      this.addAdditionalSaveData(nbt);
      buffer.writeNbt(nbt);
   }

   public void readSpawnData(FriendlyByteBuf additionalData) {
      CompoundTag nbt = additionalData.readNbt();
      if (nbt != null) {
         this.readAdditionalSaveData(nbt);
      }
   }

   public enum PostFXType {
      SCOPE(1),
      PERISCOPE(2),
      THERMALS_GREEN(3),
      CAMERA(4),
      THERMALS_WHITE(5),
      VISIBLE(6),
      NONE(0);

      public final int id;
      private static final CameraEntity2.PostFXType[] BY_ID = Arrays.stream(values())
         .sorted(Comparator.comparingInt(o -> o.id))
         .toArray(CameraEntity2.PostFXType[]::new);

      PostFXType(int i) {
         this.id = i;
      }

      public static CameraEntity2.PostFXType get(int i) {
         return 0 < i && i < BY_ID.length ? BY_ID[i] : NONE;
      }
   }
}
