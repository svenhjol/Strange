package svenhjol.strange.stonecircles.module;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.BiomeHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.VersionHelper;
import svenhjol.strange.stonecircles.structure.StoneCirclePiece;
import svenhjol.strange.stonecircles.structure.StoneCircleStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, hasSubscriptions = true,
    description = "Stone circles are surface structures of stone pillars with a runestone on top.\n" +
        "This module depends on the Runestones module.")
public class StoneCircles extends MesonModule {
    public static final String NAME = "stone_circle";
    public static final String RESNAME = "strange:stone_circle";
    public static Structure<NoFeatureConfig> structure;

    @Config(name = "Distance", description = "Distance between stone cicles. For reference, shipwrecks are 16.")
    public static int distance = 20;

    @Config(name = "Allowed generation biomes", description = "Biomes that stone circles may generate in.")
    public static List<String> validBiomesConfig = new ArrayList<>(Arrays.asList(
        BiomeHelper.getBiomeName(Biomes.PLAINS),
        BiomeHelper.getBiomeName(Biomes.SUNFLOWER_PLAINS),
        BiomeHelper.getBiomeName(Biomes.BADLANDS),
        BiomeHelper.getBiomeName(Biomes.BADLANDS_PLATEAU),
        BiomeHelper.getBiomeName(Biomes.WOODED_BADLANDS_PLATEAU),
        BiomeHelper.getBiomeName(Biomes.DESERT),
        BiomeHelper.getBiomeName(Biomes.DESERT_LAKES),
        BiomeHelper.getBiomeName(Biomes.BEACH),
        BiomeHelper.getBiomeName(Biomes.RIVER),
        BiomeHelper.getBiomeName(Biomes.SAVANNA),
        BiomeHelper.getBiomeName(Biomes.SAVANNA_PLATEAU),
        BiomeHelper.getBiomeName(Biomes.SNOWY_TUNDRA),
        BiomeHelper.getBiomeName(Biomes.SNOWY_BEACH),
        BiomeHelper.getBiomeName(Biomes.FROZEN_RIVER),
        BiomeHelper.getBiomeName(Biomes.SWAMP)
    ));

    public static final List<Biome> validBiomes = new ArrayList<>();

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:runestones");
    }

    @Override
    public void init() {
        structure = new StoneCircleStructure();

        RegistryHandler.registerStructure(structure, new ResourceLocation(Strange.MOD_ID, NAME));
        RegistryHandler.registerStructurePiece(StoneCirclePiece.PIECE, new ResourceLocation(Strange.MOD_ID, "scp"));
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        validBiomesConfig.forEach(biomeName -> {
            //noinspection deprecation
            Biome biome = Registry.BIOME.getOrDefault(new ResourceLocation(biomeName));
            if (!validBiomes.contains(biome)) validBiomes.add(biome);
        });

        ForgeRegistries.BIOMES.forEach(biome -> {
            //Structure can finish generating in any biome so it doesn't get cut off.
            VersionHelper.addStructureToBiomeFeature(structure, biome);

            //Only these biomes can start the structure generation.
            if(validBiomes.contains(biome) && Meson.isModuleEnabled("strange:stone_circles"))
                VersionHelper.addStructureToBiomeStructure(structure, biome);
        });
    }
}
