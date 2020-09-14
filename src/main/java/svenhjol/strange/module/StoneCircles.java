package svenhjol.strange.module;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.BiomeHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.structure.StoneCircleFeature;
import svenhjol.strange.structure.StoneCircleGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(description = "Surface structures made of stone pillars with a runestone on the top.")
public class StoneCircles extends MesonModule {
    public static final Identifier STRUCTURE_ID = new Identifier(Strange.MOD_ID, "stone_circle");
    public static final Identifier PIECE_ID = new Identifier(Strange.MOD_ID, "stone_circle_piece");

    public static StructurePieceType STONE_CIRCLE_PIECE;
    public static StructureFeature<DefaultFeatureConfig> STONE_CIRCLE_STRUCTURE;
    public static ConfiguredStructureFeature<?, ?> STONE_CIRCLE;

    @Override
    public void register() {
        STONE_CIRCLE_PIECE = StoneCircleGenerator::new;
        STONE_CIRCLE_STRUCTURE = new StoneCircleFeature(DefaultFeatureConfig.CODEC);
        STONE_CIRCLE = STONE_CIRCLE_STRUCTURE.configure(DefaultFeatureConfig.DEFAULT);

        Registry.register(Registry.STRUCTURE_PIECE, PIECE_ID, STONE_CIRCLE_PIECE);

        FabricStructureBuilder.create(STRUCTURE_ID, STONE_CIRCLE_STRUCTURE)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(32, 8, 12345)
            .register();

        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, STRUCTURE_ID, STONE_CIRCLE);
    }

    @Override
    public void init() {
        // TODO move to depends()
        if (!Meson.enabled("strange:runestones")) {
            this.enabled = false;
            return;
        }

        List<RegistryKey<Biome>> validBiomes = new ArrayList<>(Arrays.asList(
            BiomeKeys.PLAINS,
            BiomeKeys.SNOWY_TUNDRA
        ));

        validBiomes.forEach(registryKey -> {
            // TODO biome helper add structure method that accepts a registry key
            Biome biome = BiomeHelper.getBiomeFromBiomeKey(registryKey);
            BiomeHelper.addStructureFeature(biome, STONE_CIRCLE);
        });
    }
}
