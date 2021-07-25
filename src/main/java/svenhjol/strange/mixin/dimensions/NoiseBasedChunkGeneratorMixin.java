package svenhjol.strange.mixin.dimensions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.module.dimensions.Dimensions;

import java.util.function.Supplier;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin extends ChunkGenerator {
    @Mutable @Final @Shadow private long seed;
    @Unique private static long LAST_SEED = Dimensions.SeedSupplier.MARKER;

    public NoiseBasedChunkGeneratorMixin(BiomeSource biomeSource, StructureSettings structureSettings) {
        super(biomeSource, structureSettings);
    }

    /**
     * IntelliJ minecraft plugin can't handle mixins into synthetic methods.
     * Suppress here or this mixin will cause a compile-time error.
     */
    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
        method = "lambda$static$3",
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
        method = "<init>(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/biome/BiomeSource;JLjava/util/function/Supplier;)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;seed:J",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void hookConstructorRandomSeed(BiomeSource populationSource, BiomeSource biomeSource, long seed, Supplier<NoiseGeneratorSettings> settings, CallbackInfo ci) {
        if (seed == Dimensions.SeedSupplier.MARKER) {
            this.seed = LAST_SEED;
            this.strongholdSeed = LAST_SEED;
        } else {
            LAST_SEED = seed;
        }
    }

    @Redirect(
        method = "<init>(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/biome/BiomeSource;JLjava/util/function/Supplier;)V",
        at = @At(
            value = "NEW",
            target = "net/minecraft/world/level/levelgen/WorldgenRandom"
        )
    )
    private WorldgenRandom hookConstructorRandomSourceWithSeed(long seed) {
        return new WorldgenRandom(this.seed);
    }

    // the old way of doing it, commented in case seedy-behavior breaks
    //
    //    private static ThreadLocal<Long> storedLevelSeed = new ThreadLocal<>();
    //

    //    @Inject(
    //        method = "<init>(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/biome/BiomeSource;JLjava/util/function/Supplier;)V",
    //        at = @At(
    //            value = "INVOKE",
    //            target = "Lnet/minecraft/world/level/levelgen/NoiseSettings;height()I",
    //            ordinal = 0
    //        )
    //    )
    //    private void hookInit(BiomeSource b1, BiomeSource b2, long l, Supplier<NoiseGeneratorSettings> supplier, CallbackInfo ci) {
    //        NoiseGeneratorSettings settings = supplier.get();
    //        if (l == 0 && storedLevelSeed != null && storedLevelSeed.get() != 0) {
    //            seed = storedLevelSeed.get();
    //            ((ChunkGeneratorAccessor)this).setBiomeSource(b1.withSeed(seed));
    //            ((ChunkGeneratorAccessor)this).setRuntimeBiomeSource(b2.withSeed(seed));
    //            LogHelper.info(this.getClass(), ">> Set seed to " + storedLevelSeed.get());
    //        } else if (l != 0) {
    //            storedLevelSeed.remove();
    //            storedLevelSeed.set(l);
    //        }
    //    }
}
