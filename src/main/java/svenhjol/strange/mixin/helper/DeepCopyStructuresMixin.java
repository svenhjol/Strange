package svenhjol.strange.mixin.helper;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.charm.annotation.CharmMixin;

import java.util.Optional;

/**
 * This mixin is already implemented by RS.
 */
@CharmMixin(disableIfModsPresent = {"repurposed_structures"})
@Mixin(ChunkGenerator.class)
public class DeepCopyStructuresMixin {
    @Mutable
    @Shadow @Final private StructureSettings settings;

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/levelgen/StructureSettings;J)V",
        at = @At("RETURN")
    )
    private void hookInit(BiomeSource biomeSource, BiomeSource biomeSource2, StructureSettings structureSettings, long l, CallbackInfo ci) {
        settings = deepCopy(structureSettings);
    }

    private static StructureSettings deepCopy(StructureSettings structureSettings) {
        StrongholdConfiguration stronghold = structureSettings.stronghold();

        Optional<StrongholdConfiguration> newStrongholdSettings;

        if (stronghold == null) {
            newStrongholdSettings = Optional.empty();
        } else {
            newStrongholdSettings = Optional.of(new StrongholdConfiguration(
                stronghold.distance(),
                stronghold.spread(),
                stronghold.count()
            ));
        }

        StructureSettings newStructureSettings = new StructureSettings(newStrongholdSettings, structureSettings.structureConfig());
        newStructureSettings.configuredStructures = ImmutableMap.copyOf(structureSettings.configuredStructures);
        return newStructureSettings;
    }
}
