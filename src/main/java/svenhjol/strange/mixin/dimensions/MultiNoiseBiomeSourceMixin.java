package svenhjol.strange.mixin.dimensions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.module.dimensions.Dimensions;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin {
    @Mutable @Final @Shadow private long seed;
    @Unique private static long LAST_SEED = Dimensions.SeedSupplier.MARKER;

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
        method = "lambda$static$7", // DIRECT_CODEC
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/codecs/PrimitiveCodec;fieldOf(Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;",
            ordinal = 0
        )
    )
    private static MapCodec<Long> hookCodec(PrimitiveCodec<Long> codec, final String name) {
        return codec.fieldOf(name).orElseGet(Dimensions.SeedSupplier::getSeed);
    }

    @Inject(
        method = "<init>(JLjava/util/List;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Ljava/util/Optional;)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource;seed:J",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void hookConstructorReplaceSeed(long l, List<Pair<Biome.ClimateParameters, Supplier<Biome>>> list, MultiNoiseBiomeSource.NoiseParameters noiseParameters, MultiNoiseBiomeSource.NoiseParameters noiseParameters2, MultiNoiseBiomeSource.NoiseParameters noiseParameters3, MultiNoiseBiomeSource.NoiseParameters noiseParameters4, Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> optional, CallbackInfo ci) {
        if (this.seed == Dimensions.SeedSupplier.MARKER) {
            this.seed = LAST_SEED;
        } else {
            LAST_SEED = seed;
        }
    }

    @Redirect(
        method = "<init>(JLjava/util/List;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Ljava/util/Optional;)V",
        at = @At(
            value = "NEW",
            target = "net/minecraft/world/level/levelgen/WorldgenRandom"
        )
    )
    private WorldgenRandom hookConstructorUseOurSeed(long seed) {
        return new WorldgenRandom(this.seed);
    }
}
