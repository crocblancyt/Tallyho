package edn.stratodonut.tallyho.camera.entity;

import com.simibubi.create.content.equipment.wrench.WrenchItem;
import edn.stratodonut.tallyho.AllEntities;
import edn.stratodonut.tallyho.IClientKeyConsumer;
import edn.stratodonut.tallyho.KeyBinds;
import edn.stratodonut.tallyho.camera.AngleLimits;
import edn.stratodonut.tallyho.camera.CameraPossessionController;
import edn.stratodonut.tallyho.camera.ICameraStatus;
import edn.stratodonut.tallyho.camera.MultiOpticType;
import edn.stratodonut.tallyho.entity.LaserPointEntity;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.TGPChangeModePacket;
import edn.stratodonut.tallyho.network.TGPChangeModePacketClient;
import edn.stratodonut.tallyho.network.TGPLazeTogglePacket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class TargetingPodEntity extends PointStabilisedCameraEntity implements ICameraStatus, IClientKeyConsumer {
   private static final EntityDataAccessor<Boolean> DATA_ID_LAZING = SynchedEntityData.defineId(TargetingPodEntity.class, EntityDataSerializers.BOOLEAN);
   protected TargetingPodEntity.TGP_MODE mode = TargetingPodEntity.TGP_MODE.NONE;
   MultiOpticType opticMode;
   int stabDelay = 0;
   public float roll = 0.0F;
   public float pitch = 0.0F;
   private float lastRotX = 0.0F;
   private float lastRotY = 0.0F;

   public TargetingPodEntity(EntityType<? extends Entity> p_20966_, Level p_20967_) {
      super(p_20966_, p_20967_);
      this.opticMode = new MultiOpticType(CameraEntity2.PostFXType.VISIBLE, CameraEntity2.PostFXType.THERMALS_WHITE);
   }

   public TargetingPodEntity(Level p_19871_) {
      this((EntityType<? extends Entity>)AllEntities.TARGETING_POD_ENTITY.get(), p_19871_);
   }

   public static TargetingPodEntity spawn(ServerLevel level, BlockPos pos, float base_yaw) {
      TargetingPodEntity result = new TargetingPodEntity(level);
      result.setParams(base_yaw, CameraEntity2.PostFXType.THERMALS_WHITE, new AngleLimits(89.0F, 89.0F, 140.0F), -1);
      level.addFreshEntity(result);
      FlexibleSeatEntity.sitDown(level, pos, result);
      result.setYRot(base_yaw);
      return result;
   }

   @Override
   public void tick() {
      super.tick();
      TargetingPodEntity.TGP_MODE mode = this.getTGPMode();
      if (!this.level().isClientSide) {
         Vec3 stabLookVector = this.virtualStabVec.subtract(this.getEyePosition()).normalize();
         if (this.isDesignating()) {
            String code = this.getCode();
            LaserPointEntity.shoot(this.level(), this.getEyePosition(), stabLookVector, code);
         }

         if (mode != TargetingPodEntity.TGP_MODE.INR) {
            if (mode == TargetingPodEntity.TGP_MODE.LSRCH) {
               Optional<LaserPointEntity> targ = LaserPointEntity.find(
                  this.level(),
                  this.getBoundingBox().move(stabLookVector.scale(200.0)).inflate(400.0),
                  lpe -> lpe.getCode() != null && !lpe.match(this.getCode()) && lpe.getGroundTicks() > 0,
                  Comparator.comparingDouble(lpe -> lpe.position().subtract(this.position()).lengthSqr())
               );
               targ.ifPresent(lpe -> this.setTGP(TargetingPodEntity.TGP_MODE.INR, lpe.position()));
            } else if (mode == TargetingPodEntity.TGP_MODE.NONE) {
               if (this.stabDelay >= 20) {
                  Ship s = VSGameUtilsKt.getShipMountedTo(this);
                  if (s != null) {
                     LaserPointEntity.shootWithPlatformVelocity(
                        this.level(), this.getEyePosition(), this.getLookAngle(), VectorConversionsMCKt.toMinecraft(s.getVelocity()), null
                     );
                  } else {
                     LaserPointEntity.shoot(this.level(), this.getEyePosition(), this.getLookAngle(), null);
                  }

                  Optional<LaserPointEntity> targ = LaserPointEntity.findUncoded(this.level(), this.getEyePosition(), this.getLookAngle(), 800.0F, 22.5);
                  targ.ifPresent(lpe -> this.setTGP(TargetingPodEntity.TGP_MODE.INR, lpe.position()));
               } else {
                  this.stabDelay++;
               }
            }
         }
      }
   }

   @Override
   public boolean isStab() {
      return true;
   }

   @Override
   public void syncStab(Vec3 v) {
      super.syncStab(v);
      if (!this.level().isClientSide) {
         this.stabDelay = 0;
      }
   }

   @Nonnull
   public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand p_19979_) {
      if (CameraPossessionController.isPossessingConductor(player)) {
         return InteractionResult.FAIL;
      } else if (!player.level().isClientSide() && player.getItemInHand(p_19979_).getItem() instanceof WrenchItem) {
         this.discard();
         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public void turnView(double yRot, double xRot) {
      super.turnView(yRot, xRot);
      if (yRot != 0.0 || xRot != 0.0) {
         if (this.getTGPMode() == TargetingPodEntity.TGP_MODE.INR) {
            this.setTGP(TargetingPodEntity.TGP_MODE.NONE, this.getLookAngle());
         } else {
            this.setStabVector(this.getLookAngle());
         }
      }
   }

   @Override
   public void onKeyPressed(byte keyMap) {
      if (KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.UP)) {
         this.toggleLazeMode();
      } else if (KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.DOWN)) {
         TargetingPodEntity.TGP_MODE m = this.getTGPMode();
         if (m != TargetingPodEntity.TGP_MODE.LSRCH) {
            this.setTGP(TargetingPodEntity.TGP_MODE.LSRCH, this.getLookAngle());
         } else {
            this.setTGP(TargetingPodEntity.TGP_MODE.NONE, this.getLookAngle());
         }
      } else if (KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.LEFT) || KeyBinds.isFuncKeyDown(keyMap, KeyBinds.SpecialFunctions.RIGHT)) {
         this.opticMode.nextType();
         Minecraft.getInstance().gameRenderer.checkEntityPostEffect(this);
      }
   }

   public void toggleLazeMode() {
      if (!this.level().isClientSide) {
         this.entityData.set(DATA_ID_LAZING, !(Boolean)this.entityData.get(DATA_ID_LAZING));
      } else {
         CreateTypePacketHandler.getChannel().sendToServer(new TGPLazeTogglePacket(this));
      }
   }

   public void handle(TGPChangeModePacketClient p) {
      if (!this.level().isClientSide) {
         this.setTGP(TargetingPodEntity.TGP_MODE.get(p.mode), p.stab);
      } else if (this.mode.id() != p.mode) {
         this.mode = TargetingPodEntity.TGP_MODE.get(p.mode);
         this.virtualStabVec = p.stab;
      }
   }

   public void handle(TGPChangeModePacket p) {
      if (!this.level().isClientSide) {
         this.setTGP(TargetingPodEntity.TGP_MODE.get(p.mode), p.stab);
      } else if (this.mode.id() != p.mode) {
         this.mode = TargetingPodEntity.TGP_MODE.get(p.mode);
         this.virtualStabVec = p.stab;
      }
   }

   public void setTGP(TargetingPodEntity.TGP_MODE mode, @Nonnull Vec3 stab) {
      if (this.level().isClientSide) {
         CreateTypePacketHandler.getChannel().sendToServer(new TGPChangeModePacket(this, mode, stab));
      } else {
         CreateTypePacketHandler.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new TGPChangeModePacketClient(this, mode, stab));
      }

      this.mode = mode;
      this.virtualStabVec = stab;
   }

   public TargetingPodEntity.TGP_MODE getTGPMode() {
      return this.mode;
   }

   public boolean isDesignating() {
      return (Boolean)this.entityData.get(DATA_ID_LAZING);
   }

   @Nonnull
   @Override
   protected Vec2 getStabView(float partial_tick) {
      Ship s = VSGameUtilsKt.getShipMountedTo(this);
      if (s != null) {
         TargetingPodEntity.TGP_MODE m = this.getTGPMode();
         return m == TargetingPodEntity.TGP_MODE.INR
            ? this.getLimitedRotationFromTarget(this.virtualStabVec, partial_tick)
            : this.getLimitedRotationFromDirection(this.virtualStabVec);
      } else {
         return this.getRotationVector();
      }
   }

   @Override
   public String getStatus() {
      String base = "";
      TargetingPodEntity.TGP_MODE mode = this.getTGPMode();
      switch (mode) {
         case NONE:
            base = "TV       ";
            break;
         case INR:
            base = "TV  INR  ";
            break;
         case LSRCH:
            base = "TV LSRCH ";
      }

      return base + (this.isDesignating() && this.tickCount % 10 < 5 ? "L" : " ");
   }

   protected void setRot(float p_19916_, float p_19917_) {
      boolean b = Mth.degreesDifferenceAbs(p_19916_, this.lastRotY) > 1.0F || Mth.degreesDifferenceAbs(p_19917_, this.lastRotX) > 1.0F;
      super.setRot(p_19916_, p_19917_);
      if (this.level().isClientSide && b) {
         this.lastRotX = this.getXRot();
         this.lastRotY = this.getYRot();
         Vector3fc look = this.getLookAngle().toVector3f();
         Vector3fc forward = Direction.fromYRot(this.getBaseYaw()).step();
         Vector3fc up = Direction.UP.step();
         Ship s = VSGameUtilsKt.getShipMountedTo(this);
         if (s != null) {
            up = s.getShipToWorld().transformDirection(up, new Vector3f());
            forward = s.getShipToWorld().transformDirection(forward, new Vector3f());
         }

         Vector3fc rollout = look.sub(forward.get(new Vector3f()).mul(look.dot(forward)), new Vector3f()).normalize();
         this.roll = (float)Math.toDegrees(Math.asin(rollout.cross(up, new Vector3f()).dot(forward)));
         this.pitch = (float)Math.toDegrees(look.angle(forward));
         if (look.dot(up) < 0.0F) {
            this.roll *= -1.0F;
            this.pitch *= -1.0F;
         }

         if (Float.isNaN(this.roll)) {
            this.roll = 0.0F;
         }

         if (Float.isNaN(this.pitch)) {
            this.pitch = 0.0F;
         }
      }
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_LAZING, false);
   }

   @NotNull
   @Override
   public CameraEntity2.PostFXType getOverlayType() {
      return this.opticMode.getCurrentOptic();
   }

   @Override
   protected boolean isShortLived() {
      return false;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   @Override
   protected float getEyeHeight(@NotNull Pose p, EntityDimensions dim) {
      return dim.height * 0.5F;
   }

   public enum TGP_MODE {
      NONE(0),
      INR(1),
      LSRCH(2);

      private final int id;
      private static final TargetingPodEntity.TGP_MODE[] BY_ID = Arrays.stream(values())
         .sorted(Comparator.comparingInt(o -> o.id))
         .toArray(TargetingPodEntity.TGP_MODE[]::new);

      TGP_MODE(int v) {
         this.id = v;
      }

      public static TargetingPodEntity.TGP_MODE get(int i) {
         return 0 < i && i < BY_ID.length ? BY_ID[i] : NONE;
      }

      public int id() {
         return this.id;
      }
   }
}
