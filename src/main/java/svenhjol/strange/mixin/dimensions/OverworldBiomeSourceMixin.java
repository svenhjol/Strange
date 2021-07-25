package svenhjol.strange.mixin.dimensions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.Layers;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.module.dimensions.Dimensions;

@Mixin(OverworldBiomeSource.class)
public class OverworldBiomeSourceMixin {
    @Mutable @Shadow @Final private long seed;
    @Mutable @Shadow @Final private Layer noiseBiomeLayer;
    @Unique private static long LAST_SEED = Dimensions.SeedSupplier.MARKER;
    @Unique private static boolean DID_REPLACE = false;

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
        method = "lambda$static$4", // CODEC
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
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/biome/OverworldBiomeSource;seed:J",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void hookConstructorRandomSeed(long seed, boolean bl, boolean bl2, Registry<Biome> registry, CallbackInfo ci) {
        if (seed == Dimensions.SeedSupplier.MARKER) {
            this.seed = LAST_SEED;
            DID_REPLACE = true;
        } else {
            LAST_SEED = seed;
        }
    }

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void hookRecreateNoise(long l, boolean bl, boolean bl2, Registry<Biome> registry, CallbackInfo ci) {
        if (DID_REPLACE)
            this.noiseBiomeLayer = Layers.getDefaultLayer(this.seed, bl, bl2 ? 6 : 4, 4);
    }
}
