package svenhjol.strange.mixin.dimensions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.module.dimensions.DimensionsClient;

@Mixin(ClientLevel.ClientLevelData.class)
public class ConfigurableHorizonHeightMixin {
    @Inject(
        method = "getHorizonHeight",
        at = @At("RETURN"),
        cancellable = true
    )
    private void hookGetHorizonHeight(LevelHeightAccessor levelHeight, CallbackInfoReturnable<Double> cir) {
        DimensionsClient.getHorizonHeight(levelHeight).ifPresent(cir::setReturnValue);
    }
}
