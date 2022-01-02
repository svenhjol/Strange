package svenhjol.strange.mixin.dimensions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import svenhjol.strange.module.dimensions.Dimensions;

@SuppressWarnings({"target", "mapping"})
@Mixin(WorldGenSettings.class)
public class WorldGenSettingsMixin {
    @Redirect(
        method = "lambda$static$1(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/kinds/App;", // CODEC
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/codecs/PrimitiveCodec;fieldOf(Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;",
            ordinal = 0
        )
    )
    private static MapCodec<Long> hookCodec(PrimitiveCodec<Long> codec, final String name) {
        return codec.fieldOf(name).orElseGet(Dimensions.SeedSupplier::getSeed);
    }
}
