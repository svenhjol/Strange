package svenhjol.strange.stonecircles.module;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.BiomeHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.stonecircles.structure.StoneCircleConfig;
import svenhjol.strange.stonecircles.structure.StoneCircleStructure;
import svenhjol.strange.stonecircles.structure.VaultStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.STONE_CIRCLES)
public class StoneCircles extends MesonModule
{
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "stone_circle");
    public static final String NAME = "stone_circle";
    public static Structure<StoneCircleConfig> structure;

    @Config(name = "Generation chance", description = "Chance (out of 1.0) of a stone circle generating in a chunk.")
    public static double stoneCircleChance = 0.2D;

    @Config(name = "Vault generation chance", description = "Chance (out of 1.0) of vaults generating beneath a stone circle.")
    public static double vaultChance = 0.33D;

    @Config(name = "Vault generation size", description = "Maximum number of rooms generated in any vault corridor.")
    public static int vaultSize = 6;

    @Config(name = "Outer stone circles only", description = "If true, vaults will only generate under stone circles in 'outer lands'.\n" +
        "This has no effect if the Outerlands module is disabled.")
    public static boolean outerOnly = true;

    @Config(name = "Allowed biomes", description = "Biomes that stone circles may generate in.")
    public static List<String> biomesConfig = new ArrayList<>(Arrays.asList(
        BiomeHelper.getBiomeName(Biomes.PLAINS),
        BiomeHelper.getBiomeName(Biomes.SUNFLOWER_PLAINS),
        BiomeHelper.getBiomeName(Biomes.DESERT),
        BiomeHelper.getBiomeName(Biomes.DESERT_LAKES),
        BiomeHelper.getBiomeName(Biomes.BEACH),
        BiomeHelper.getBiomeName(Biomes.SAVANNA),
        BiomeHelper.getBiomeName(Biomes.SNOWY_TUNDRA),
        BiomeHelper.getBiomeName(Biomes.SNOWY_BEACH),
        BiomeHelper.getBiomeName(Biomes.SWAMP),
        BiomeHelper.getBiomeName(Biomes.END_BARRENS),
        BiomeHelper.getBiomeName(Biomes.END_HIGHLANDS),
        BiomeHelper.getBiomeName(Biomes.END_MIDLANDS),
        BiomeHelper.getBiomeName(Biomes.NETHER)
    ));

    public static List<Biome> validBiomes = new ArrayList<>();

    @Override
    public void init()
    {
        structure = new StoneCircleStructure(StoneCircleConfig::deserialize);

        // TODO check that this registers the stone_circle name properly
        Registry.register(Registry.FEATURE, NAME, structure);
        RegistryHandler.registerStructure(structure, ID);

        // TODO add structure pieces to Meson registry
        Registry.register(Registry.STRUCTURE_PIECE, "scp", StoneCircleStructure.SCP);
        Registry.register(Registry.STRUCTURE_PIECE, "scup", VaultStructure.SCUP);

        biomesConfig.forEach(biomeName -> {
            Biome biome = Registry.BIOME.getOrDefault(new ResourceLocation(biomeName));
            if (!validBiomes.contains(biome)) validBiomes.add(biome);
        });

        Registry.BIOME.forEach(biome -> {
            biome.addFeature(
                GenerationStage.Decoration.SURFACE_STRUCTURES,
                Biome.createDecoratedFeature(structure, new StoneCircleConfig((float) stoneCircleChance), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
        });

        validBiomes.forEach(biome -> {
            biome.addStructure(structure, new StoneCircleConfig((float) stoneCircleChance));
        });
    }
}
