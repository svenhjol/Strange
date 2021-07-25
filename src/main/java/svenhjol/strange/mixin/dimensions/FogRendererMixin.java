package svenhjol.strange.mixin.dimensions;

import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
//    @Shadow private static float fogRed;
//
//    @Shadow private static float fogGreen;
//
//    @Shadow private static float fogBlue;
//
//    @Shadow private static long biomeChangedTime;
//
//    @Inject(
//        method = "setupColor",
//        at = @At("HEAD"),
//        cancellable = true
//    )
//    private static void hookSetupColor(Camera camera, float ticks, ClientLevel level, int i, float gg, CallbackInfo ci) {
//        FogType fogType = camera.getFluidInCamera();
//        ResourceKey<Level> dimension = level.dimension();
//        if (dimension != null && fogType == FogType.NONE && dimension.location().equals(Dimensions.DARKLAND_ID)) {
//            int s = level.getSeaLevel();
//            int y = camera.getBlockPosition().getY();
//            float r = 0.1F;
//            float g = 0.38F;
//            float b = 0.34F;
//
//            if (y < s) {
//                r = Math.max(0.0F, r * ((float)y / (float)s));
//                g = Math.max(0.0F, g * ((float)y / (float)s));
//                b = Math.max(0.0F, b * ((float)y / (float)s));
//            }
//
//            fogRed = r;
//            fogGreen = g;
//            fogBlue = b;
//            biomeChangedTime = -1L;
//            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
//            ci.cancel();
//        }
//    }
}
