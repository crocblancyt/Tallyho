package edn.stratodonut.tallyho.mixin.some_peri;

import edn.stratodonut.tallyho.missile.tracker.RadEmissionTracker;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
//import net.spaceeye.someperipherals.stuff.radar.ScanInRadiusKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//@Pseudo
@Mixin(value = {Level.class}) //ScanInRadiusKt.class}, remap = false)
public abstract class MixinScanInRadius {
    /*@Inject(method = {"scanForShips"}, at = {@At("HEAD")})
    private static void mixinScanForShips(double radius, ServerLevel level, BlockPos pos, CallbackInfoReturnable<List<Object>> cir) {
        RadEmissionTracker.emit((Level)level, pos, (radius > 0.0D) ? (int)radius : 200000);
    }

    @Inject(method = {"scanForEntities"}, at = {@At("HEAD")})
    private static void mixinScanForEntities(double radius, ServerLevel level, BlockPos pos, CallbackInfoReturnable<List<Object>> cir) {
        RadEmissionTracker.emit((Level)level, pos, (radius > 0.0D) ? (int)radius : 200000);
    }*/
}
