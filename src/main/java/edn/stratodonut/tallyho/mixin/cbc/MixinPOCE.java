
package edn.stratodonut.tallyho.mixin.cbc;

import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import edn.stratodonut.tallyho.mixinducks.RecoilingCannonEntity;
import edn.stratodonut.tallyho.network.CreateTypePacketHandler;
import edn.stratodonut.tallyho.network.RecoilingCannonUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlockEntity;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.effects.particles.smoke.QuickFiringBreechSmokeParticleData;
import riftyboi.cbcmodernwarfare.cannon_control.compact_mount.CompactCannonMountBlock;
import riftyboi.cbcmodernwarfare.cannon_control.compact_mount.CompactCannonMountBlockEntity;
import riftyboi.cbcmodernwarfare.cannon_control.contraption.MountedMediumcannonContraption;
import riftyboi.cbcmodernwarfare.cannons.medium_cannon.breech.MediumcannonBreechBlockEntity;

@Pseudo
@Mixin(
        value = {PitchOrientedContraptionEntity.class},
        remap = false
)
public abstract class MixinPOCE extends OrientedContraptionEntity implements RecoilingCannonEntity {
    @Shadow
    private BlockPos controllerPos;
    @Unique
    public double tyh$target = (double)0.0F;
    @Unique
    public boolean tyh$recoiling = false;
    @Unique
    public boolean tyh$ejecting = false;

    @Shadow
    public abstract ControlPitchContraption getController();

    public MixinPOCE(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(
            method = {"tickContraption"},
            at = {@At("HEAD")}
    )
    private void mixintickContraption(CallbackInfo ci) {
        if (this.contraption instanceof MountedMediumcannonContraption) {
            if (this.tickCount > 2) {
                ControlPitchContraption controller = this.getController();
                Vec3 origin = null;
                if (controller instanceof CannonMountBlockEntity) {
                    CannonMountBlockEntity cmbe = (CannonMountBlockEntity)controller;
                    boolean upsideDown = cmbe.getBlockState().getValue(BlockStateProperties.VERTICAL_DIRECTION) == Direction.UP;
                    origin = Vec3.atBottomCenterOf(cmbe.getBlockPos().above(upsideDown ? -2 : 2));
                } else if (controller instanceof CompactCannonMountBlockEntity) {
                    CompactCannonMountBlockEntity ccmbe = (CompactCannonMountBlockEntity)controller;
                    Direction facing = ccmbe.getBlockState().getValue(CompactCannonMountBlock.HORIZONTAL_FACING);
                    origin = Vec3.atBottomCenterOf(ccmbe.getBlockPos().relative(facing.getClockWise(), 1));
                } else if (controller instanceof FixedCannonMountBlockEntity) {
                    FixedCannonMountBlockEntity fcmbe = (FixedCannonMountBlockEntity)controller;
                    Direction facing = fcmbe.getBlockState().getValue(BlockStateProperties.FACING);
                    origin = Vec3.atBottomCenterOf(fcmbe.getBlockPos().relative(facing));
                }

                if (origin == null) {
                    return;
                }

                Vec3 normal = this.getNormal();
                Vec3 target = origin.add(normal.scale(this.tyh$target));
                Vec3 offset = target.subtract(this.position());
                Vec3 movementVec = VecHelper.clamp(offset, this.getRecoilSpeed());
                this.move(movementVec.x, movementVec.y, movementVec.z);
                if (this.tyh$recoiling) {
                    if (offset.length() < 0.2 && this.tyh$ejecting) {
                        this.tyh$ejecting = false;
                        if (!this.level().isClientSide) {
                            this.tyh$ejectShell();
                        }
                    }

                    if (offset.length() < 0.1) {
                        this.setPos(target.x, target.y, target.z);
                        if (this.tyh$target == (double)-1.0F) {
                            this.tyh$ejecting = true;
                            this.tyh$target = (double)0.0F;
                            this.setContraptionMotion(normal.scale((double)this.getRecoilSpeed()));
                        } else if (this.tyh$target == (double)0.0F) {
                            this.setContraptionMotion(Vec3.ZERO);
                            this.tyh$recoiling = false;
                        }

                        this.tyh$sendPacket();
                    }
                }
            }

        }
    }

    @Unique
    protected void tyh$ejectShell() {
        AbstractMountedCannonContraption c = (AbstractMountedCannonContraption)this.contraption;
        BlockEntity be = c.presentBlockEntities.get(c.getStartPos());
        if (be instanceof MediumcannonBreechBlockEntity breech) {
            Level level = this.level();
            level.playSound(null, BlockPos.containing(this.toGlobalVector(c.getStartPos().getCenter(), 0.0F)), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.5F);
            breech.toggleOpening();
            if (breech.getOpenDirection() > 0) {
                ItemStack extract = breech.getInputBuffer();
                Vec3 normal = Vec3.atLowerCornerOf(c.initialOrientation().getNormal()).reverse();
                Vec3 dir = this.applyRotation(normal, 0.0F);
                if (!extract.isEmpty()) {
                    Vec3 smokePos = Vec3.atCenterOf(c.getStartPos()).add(normal.scale(0.2));
                    Vec3 globalPos = this.toGlobalVector(smokePos, 0.0F);
                    Vec3 vel = dir.scale(0.5F);
                    ItemEntity item = new ItemEntity(level, globalPos.x, globalPos.y, globalPos.z, extract, vel.x, vel.y, vel.z);
                    item.setPickUpDelay(CBCConfigs.SERVER.munitions.quickFiringBreechItemPickupDelay.get());
                    level.addFreshEntity(item);
                    breech.clearInputBuffer();
                }

                MountedMediumcannonContraption cannon = (MountedMediumcannonContraption)c;
                if (level instanceof ServerLevel) {
                    ServerLevel slevel = (ServerLevel)level;
                    if (cannon.hasFired) {
                        Vec3 smokePos = Vec3.atCenterOf(c.getStartPos()).add(normal.scale(0.6));
                        Vec3 globalPos = this.toGlobalVector(smokePos, 0.0F);
                        Vec3 vel = dir.scale(0.15);
                        slevel.sendParticles(new QuickFiringBreechSmokeParticleData(), globalPos.x, globalPos.y, globalPos.z, 0, vel.x, vel.y, vel.z, (double)1.0F);
                        cannon.hasFired = false;
                    }
                }
            }
        }

    }

    @Unique
    public void tyh$sendPacket() {
        Vec3 normal = this.getNormal();
        CreateTypePacketHandler.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new RecoilingCannonUpdatePacket(this.getId(), this.getDeltaMovement().dot(normal), this.tyh$target));
    }

    public Vec3 getNormal() {
        return this.applyRotation(Vec3.atLowerCornerOf(this.getInitialOrientation().getNormal()), 0.0F);
    }

    public float getRecoilSpeed() {
        return this.tyh$target < (double)0.0F ? 2.0F : 0.3F;
    }

    public void recoil(Vec3 v) {
        this.tyh$recoiling = true;
        this.tyh$target = -1.0F;
        this.setContraptionMotion(this.getNormal().scale((-this.getRecoilSpeed())));
        if (!this.level().isClientSide) {
            this.tyh$sendPacket();
        }

    }

    public void handlePacket(RecoilingCannonUpdatePacket p) {
        Vec3 n = this.getNormal();
        this.setContraptionMotion(n.scale(p.motion));
        this.tyh$target = p.target;
    }
}