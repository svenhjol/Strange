package svenhjol.strange.runestones.module;

import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.runestones.gen.GeodesConfig;
import svenhjol.strange.runestones.gen.GeodesFeature;
import svenhjol.strange.runestones.gen.GeodesPlacement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES)
public class EnderGeodes extends MesonModule {
    public static Feature<GeodesConfig> feature = null;
    public static Placement<FrequencyConfig> placement = null;

    @Override
    public void init() {
        feature = new GeodesFeature(GeodesConfig::deserialize);
        placement = new GeodesPlacement(FrequencyConfig::deserialize);
        ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "ender_geode");
        RegistryHandler.registerFeature(feature, placement, ID);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        List<Biome> validBiomes = new ArrayList<>(Arrays.asList(
            Biomes.END_BARRENS,
            Biomes.END_HIGHLANDS,
            Biomes.END_MIDLANDS
        ));
        for (Biome b : validBiomes) {
            b.addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS,
                Biome.createDecoratedFeature(feature,
                    new GeodesConfig(Blocks.END_STONE.getDefaultState()),
                    placement,
                    new FrequencyConfig(2)
            ));
        }
    }
}
