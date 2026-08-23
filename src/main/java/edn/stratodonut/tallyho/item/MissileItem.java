package edn.stratodonut.tallyho.item;

import edn.stratodonut.tallyho.missile.MissileRegistry;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class MissileItem extends Item {
    public final String id;

    public MissileItem(Item.Properties properties) {
        super(properties);
        this.id = "null";
    }

    public MissileItem(Item.Properties properties, String id) {
        super(properties);
        this.id = id;
    }

    @Nonnull
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Ship s = VSGameUtilsKt.getShipManagingPos(level, pos);
        Player player = ctx.getPlayer();
        if (s == null || player == null)
            return InteractionResult.FAIL;
        if (level.isClientSide)
            return InteractionResult.CONSUME;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (ctx.getPlayer() instanceof net.minecraft.server.level.ServerPlayer) {
                Direction face = ctx.getClickedFace();
                Vector3d lookInShip = s.getWorldToShip().transformDirection(VectorConversionsMCKt.toJOML(player.getLookAngle()));
                MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(this.id);
                if (entry == null)
                    return InteractionResult.FAIL;
                Vec3 spawnPos = ctx.getClickLocation();
                if (face == Direction.DOWN)
                    spawnPos = spawnPos.add(0.0D, entry.getHitboxScale(), 0.0D);
                spawnPos = gridify(spawnPos, entry.getHitboxScale()).add(0.0D, 0.25D, 0.0D);
                entry.spawn(serverLevel, spawnPos,
                    Direction.getNearest(lookInShip.x, 0.0D, lookInShip.z).toYRot());
                if (!ctx.getPlayer().isCreative()) {
                    ctx.getItemInHand().shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static Vec3 gridify(Vec3 original, float scale) {
        scale = 1.0F / Math.max(scale, 0.0625F) + 1.0F;
        Vec3 t = new Vec3(Math.floor(original.x), Math.floor(original.y), Math.floor(original.z));
        Vec3 f = original.subtract(t);
        return new Vec3(t.x + ((float)Math.round(f.x * scale) / scale), t.y + (
            (float)Math.round(f.y * scale) / scale), t.z + (
            (float)Math.round(f.z * scale) / scale));
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, level, components, flag);
        MissileRegistry.MissileRegistryEntry entry = MissileRegistry.getEntry(this.id);
        if (entry != null)
            entry.appendHoverText(components);
    }
}