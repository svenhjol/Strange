package svenhjol.strange.mixin.dimensions;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.module.dimensions.DimensionsClient;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
        method = "renderSnowAndRain",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookRenderSnowAndRain(LightTexture lightTexture, float f, double d, double e, double g, CallbackInfo ci) {
        DimensionsClient.shouldRenderPrecipitation().ifPresent(b -> {
            if (!b) {
                ci.cancel();
            }
        });
    }
}
