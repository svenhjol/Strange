package svenhjol.strange.travelrunes.module;

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
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.travelrunes.structure.StoneCircleConfig;
import svenhjol.strange.travelrunes.structure.StoneCircleStructure;
import svenhjol.strange.travelrunes.structure.UndergroundStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TRAVEL_RUNES)
public class StoneCircles extends MesonModule
{
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "stone_circle");
    public static final String NAME = "stone_circle";
    public static Structure<StoneCircleConfig> structure;

    @Config(name = "Generation chance", description = "Chance (out of 1.0) of a stone circle generating in a chunk.")
    public static double chance = 0.005D;

    @Config(name = "Allowed biomes", description = "Biomes that stone circles may generate in.")
    public static List<String> configBiomes = new ArrayList<>(Arrays.asList(
        Objects.requireNonNull(Biomes.PLAINS.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.SUNFLOWER_PLAINS.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.DESERT.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.DESERT_LAKES.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.BEACH.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.SAVANNA.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.SNOWY_TUNDRA.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.SNOWY_BEACH.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.SWAMP.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.END_BARRENS.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.END_HIGHLANDS.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.END_MIDLANDS.getRegistryName()).getPath(),
        Objects.requireNonNull(Biomes.NETHER.getRegistryName()).getPath()
    ));

    public static List<Biome> validBiomes = new ArrayList<>();

    @Override
    public void init()
    {
        structure = new StoneCircleStructure(StoneCircleConfig::deserialize);

//        Registry.register(Registry.FEATURE, "stone_circle", structure);
//        RegistryHandler.addRegisterable(structure, ID);

        RegistryHandler.registerStructure(structure, ID, null);

        // TODO add to Meson structure registry
        Registry.register(Registry.STRUCTURE_PIECE, "scp", StoneCircleStructure.SCP);
        Registry.register(Registry.STRUCTURE_PIECE, "scup", UndergroundStructure.SCUP);
//        RegistryHandler.addRegisterable(, new ResourceLocation(Strange.MOD_ID, "SCP"));

        configBiomes.forEach(biomeName -> {
            Biome biome = Registry.BIOME.getOrDefault(new ResourceLocation(biomeName));
            if (!validBiomes.contains(biome)) validBiomes.add(biome);
        });

        Registry.BIOME.forEach(biome -> {
            biome.addFeature(
                GenerationStage.Decoration.SURFACE_STRUCTURES,
                Biome.createDecoratedFeature(structure, new StoneCircleConfig((float)chance), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
        });

        validBiomes.forEach(biome -> {
            biome.addStructure(structure, new StoneCircleConfig((float)chance));
        });
    }
}
