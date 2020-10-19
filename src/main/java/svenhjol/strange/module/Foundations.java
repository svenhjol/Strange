package svenhjol.strange.module;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.BiomeHelper;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.structure.FoundationFeature;
import svenhjol.strange.structure.FoundationGenerator;
import svenhjol.strange.structure.foundation.StoneRoomFoundation;

import static svenhjol.charm.base.helper.StructureHelper.addToBiome;
import static svenhjol.charm.base.helper.StructureHelper.registerConfiguredFeature;

@Module(mod = Strange.MOD_ID)
public class Foundations extends CharmModule {
    public static final Identifier FOUNDATION_ID = new Identifier(Strange.MOD_ID, "foundation");

    public static StructureFeature<StructurePoolFeatureConfig> FEATURE;
    public static ConfiguredStructureFeature<?, ?> CONFIGURED_FEATURE;

    public static int foundationSize = 2;

    @Override
    public void init() {
        // register all custom foundations here
        FoundationGenerator.FOUNDATIONS.add(new StoneRoomFoundation());

        // builds and registers all foundations into pools
        FoundationGenerator.init();

        if (!FoundationGenerator.FOUNDATIONS.isEmpty()) {
            addToBiome(BiomeHelper.MOUNTAINS, CONFIGURED_FEATURE);
            addToBiome(BiomeHelper.PLAINS, CONFIGURED_FEATURE);
        }
    }

    @Override
    public void register() {
        FEATURE = new FoundationFeature(StructurePoolFeatureConfig.CODEC);

        FabricStructureBuilder.create(FOUNDATION_ID, FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(48, 24, 4231521)
            .register();

        // create configuredFeature objects for each jigsaw pool
        CONFIGURED_FEATURE = FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationGenerator.FOUNDATION_POOL, foundationSize));

        // register each configuredFeature with MC registry against the configured feature
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "foundation_overworld"), CONFIGURED_FEATURE);
    }
}
