package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import svenhjol.charm.charmony.common.helper.TagHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.api.enums.RunestoneLocationType;
import svenhjol.strange.api.impl.RunestoneLocation;

import java.util.List;
import java.util.Optional;

public final class Helpers {
    public static final ResourceLocation SPAWN_POINT_ID = Strange.id("spawn_point");
    public static final RunestoneLocation SPAWN_POINT = new RunestoneLocation(RunestoneLocationType.PLAYER, SPAWN_POINT_ID);

    /**
     * Make a random runestone biome location from a given biome tag.
     */
    public static Optional<RunestoneLocation> randomBiome(LevelAccessor level, TagKey<Biome> tag, RandomSource random) {
        var values = getValues(level.registryAccess(), tag);
        if (!values.isEmpty()) {
            var location = new RunestoneLocation(RunestoneLocationType.BIOME, values.get(random.nextInt(values.size())));
            return Optional.of(location);
        }

        return Optional.empty();
    }

    /**
     * Make a random runestone structure location from a given structure tag.
     */
    public static Optional<RunestoneLocation> randomStructure(LevelAccessor level, TagKey<Structure> tag, RandomSource random) {
        var values = getValues(level.registryAccess(), tag);
        if (!values.isEmpty()) {
            var location = new RunestoneLocation(RunestoneLocationType.STRUCTURE, values.get(random.nextInt(values.size())));
            return Optional.of(location);
        }

        return Optional.empty();
    }

    /**
     * Get all values of a given tag.
     */
    public static <T> List<ResourceLocation> getValues(RegistryAccess registryAccess, TagKey<T> tag) {
        var registry = registryAccess.registryOrThrow(tag.registry());
        return TagHelper.getValues(registry, tag)
            .stream().map(registry::getKey).toList();
    }
}
