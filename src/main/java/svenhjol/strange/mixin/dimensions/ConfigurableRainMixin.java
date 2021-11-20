package svenhjol.strange.mixin.dimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.module.dimensions.Dimensions;

@Mixin(Level.class)
public class ConfigurableRainMixin {
    @Inject(
        method = "isRainingAt",
        at = @At("HEAD")
    )
    private void hookIsRainingAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Dimensions.LEVEL.remove();
        Dimensions.LEVEL.set((Level)(Object)this);
    }

    @Inject(
        method = "getRainLevel",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetRainLevel(float f, CallbackInfoReturnable<Float> cir) {
        Dimensions.getRainLevel((Level)(Object)this).ifPresent(cir::setReturnValue);
    }
}
