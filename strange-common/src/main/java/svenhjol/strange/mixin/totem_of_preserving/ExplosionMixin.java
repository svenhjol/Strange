package svenhjol.strange.mixin.totem_of_preserving;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.feature.totem_of_preserving.ProtectedPositions;

@Mixin(Explosion.class)
public class ExplosionMixin {
    @Shadow @Final
    private Level level;

    @Shadow @Final private ObjectArrayList<BlockPos> toBlow;

    @Inject(
        method = "finalizeExplosion",
        at = @At("HEAD")
    )
    private void hookFinalizeExplosion(boolean bl, CallbackInfo ci) {
        for (var pos : ProtectedPositions.all(level.dimension().location())) {
            this.toBlow.remove(pos);
        }
    }
}
