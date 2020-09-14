package svenhjol.strange.module;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.BiomeHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.structure.RuinFeature;
import svenhjol.strange.structure.RuinGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Module(description = "Ruins that spawn underground with different types according to biome.")
public class Ruins extends MesonModule {
    public static final Identifier STRUCTURE_ID = new Identifier(Strange.MOD_ID, "ruin");

    public static StructureFeature<StructurePoolFeatureConfig> RUIN_STRUCTURE;
    public static ConfiguredStructureFeature<?, ?> RUIN;

    @Config(name = "Available biomes", description = "Biomes that ruins may generate in.")
    public static List<String> configBiomes = new ArrayList<>(Arrays.asList(
        "minecraft:plains",
        "minecraft:desert",
        "minecraft:savanna",
        "minecraft:swamp",
        "minecraft:sunflower_plains",
        "minecraft:flower_forest",
        "minecraft:snowy_tundra"
    ));

    @Override
    public void register() {
        RUIN_STRUCTURE = new RuinFeature(StructurePoolFeatureConfig.CODEC);
        RUIN = RUIN_STRUCTURE.configure(new StructurePoolFeatureConfig(() -> {
            return RuinGenerator.pools.get(0);
        }, 6));

        FabricStructureBuilder.create(STRUCTURE_ID, RUIN_STRUCTURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(24, 8, 12125)
            .register();

        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, STRUCTURE_ID, RUIN);
    }

    @Override
    public void init() {
        RuinGenerator.init();

        configBiomes.forEach(biomeId -> {
            Optional<Biome> biome = BuiltinRegistries.BIOME.getOrEmpty(new Identifier(biomeId));
            biome.ifPresent(value -> BiomeHelper.addStructureFeature(value, RUIN));
        });
    }
}
