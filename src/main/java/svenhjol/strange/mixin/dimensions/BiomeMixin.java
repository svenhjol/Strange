package svenhjol.strange.mixin.dimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.dimensions.DimensionsClient;

import java.util.Optional;

@Mixin(Biome.class)
public class BiomeMixin {
    @Inject(
        method = "getFogColor",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetFogColor(CallbackInfoReturnable<Integer> cir) {
        DimensionsClient.getFogColor((Biome)(Object)this).ifPresent(cir::setReturnValue);
    }

    @Inject(
        method = "getSkyColor",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetSkyColor(CallbackInfoReturnable<Integer> cir) {
        DimensionsClient.getSkyColor((Biome)(Object)this).ifPresent(cir::setReturnValue);
    }

    @Inject(
        method = "getWaterColor",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetWaterColor(CallbackInfoReturnable<Integer> cir) {
        DimensionsClient.getSkyColor((Biome)(Object)this).ifPresent(cir::setReturnValue);
    }

    @Inject(
        method = "getWaterFogColor",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetWaterFogColor(CallbackInfoReturnable<Integer> cir) {
        DimensionsClient.getWaterFogColor((Biome)(Object)this).ifPresent(cir::setReturnValue);
    }

    @Inject(
        method = "getFoliageColor",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetFoliageColor(CallbackInfoReturnable<Integer> cir) {
        DimensionsClient.getFoliageColor((Biome)(Object)this).ifPresent(cir::setReturnValue);
    }

    @Inject(
        method = "getGrassColor",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetGrassColor(CallbackInfoReturnable<Integer> cir) {
        DimensionsClient.getGrassColor((Biome)(Object)this).ifPresent(cir::setReturnValue);
    }

    @Inject(
        method = "getAmbientParticle",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetAmbientParticle(CallbackInfoReturnable<Optional<AmbientParticleSettings>> cir) {
        Optional<AmbientParticleSettings> opt = DimensionsClient.getAmbientParticle((Biome) (Object) this);
        opt.ifPresent(p -> cir.setReturnValue(opt));
    }

    @Inject(
        method = "getBackgroundMusic",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetBackgroundMusic(CallbackInfoReturnable<Optional<Music>> cir) {
        Optional<Music> opt = DimensionsClient.getMusic((Biome)(Object)this);
        opt.ifPresent(p -> cir.setReturnValue(opt));
    }

    @Inject(
        method = {
            "shouldSnow",
            "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"
        },
        at = @At("HEAD")
    )
    private void hookShouldFreeze(LevelReader levelReader, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (levelReader.isClientSide()) return;

        Dimensions.LEVEL.remove();
        Dimensions.LEVEL.set(levelReader);
    }

    @Inject(
        method = "getTemperature",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetTemperature(CallbackInfoReturnable<Float> cir) {
        Dimensions.getTemperature(Dimensions.LEVEL.get(), (Biome)(Object)this).ifPresent(cir::setReturnValue);
    }

    @Inject(
        method = "getPrecipitation",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookGetPrecipitation(CallbackInfoReturnable<Biome.Precipitation> cir) {
        Dimensions.getPrecipitation(Dimensions.LEVEL.get()).ifPresent(cir::setReturnValue);
    }
}
