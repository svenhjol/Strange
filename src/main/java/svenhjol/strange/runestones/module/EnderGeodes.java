package svenhjol.strange.runestones.module;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.runestones.gen.GeodesFeature;
import svenhjol.strange.runestones.gen.GeodesPlacement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES,
    description = "Geodes are rough spherical shapes that spawn around large islands in the End.\n" +
        "Inside a Geode you may find Raw Amethyst or Moonstone crystals (if enabled)."
)
public class EnderGeodes extends MesonModule {
    public static Feature<NoFeatureConfig> feature = null;
    public static Placement<ChanceConfig> placement = null;

    @Config(name = "Chance", description = "Chance (1 in n) of a Geode spawning in a chunk.")
    public static int chance = 80;

    @Override
    public void init() {
        feature = new GeodesFeature(NoFeatureConfig::deserialize);
        placement = new GeodesPlacement(ChanceConfig::deserialize);
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
            b.addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, feature
                .withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
                .withPlacement(placement.configure(new ChanceConfig(chance))));
        }
    }
}
