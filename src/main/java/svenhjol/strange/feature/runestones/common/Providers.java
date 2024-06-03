package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import svenhjol.charm.charmony.Api;
import svenhjol.charm.charmony.feature.ProviderHolder;
import svenhjol.strange.Strange;
import svenhjol.strange.api.iface.RunestoneDefinition;
import svenhjol.strange.api.iface.RunestoneDefinitionsProvider;
import svenhjol.strange.api.impl.RunestoneLocation;
import svenhjol.strange.feature.runestones.Runestones;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class Providers extends ProviderHolder<Runestones> implements RunestoneDefinitionsProvider {
    public final List<RunestoneDefinition> definitions = new ArrayList<>();

    public Providers(Runestones feature) {
        super(feature);
    }

    @Override
    public void onEnabled() {
        Api.consume(RunestoneDefinitionsProvider.class, providers -> {
            for (var definition : providers.getRunestoneDefinitions()) {
                // Add the block to the runestone block entity.
                var blockSupplier = definition.block();
                feature().registry().blockEntityBlocks(feature().registers.blockEntity, List.of(blockSupplier));

                // Add the definition to the full set for mapping later.
                definitions.add(definition);
            }
        });
    }

    @Override
    public List<RunestoneDefinition> getRunestoneDefinitions() {
        return List.of(stone(), blackstone(), obsidian());
    }

    private RunestoneDefinition stone() {
        return new RunestoneDefinition() {
            public final String material = "stone";
            public boolean isRare;

            @Override
            public Supplier<? extends Block> block() {
                return feature().registers.stoneBlock;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                isRare = random.nextDouble() < 0.12d;
                return randomLocation(this, level, random, 0.25d, 0.75d);
            }

            @Override
            public Optional<TagKey<Item>> items() {
                return makeItemTag(material, isRare);
            }

            @Override
            public Optional<TagKey<Biome>> biomes() {
                return makeBiomeTag(material, isRare);
            }

            @Override
            public Optional<TagKey<Structure>> structures() {
                return makeStructureTag(material, isRare);
            }
        };
    }

    private RunestoneDefinition blackstone() {
        return new RunestoneDefinition() {
            public final String material = "blackstone";
            public boolean isRare;

            @Override
            public Supplier<? extends Block> block() {
                return feature().registers.blackstoneBlock;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                isRare = random.nextDouble() < 0.5d;
                return randomLocation(this, level, random, 0.33d, 0.7d);
            }

            @Override
            public Optional<TagKey<Item>> items() {
                return makeItemTag(material, isRare);
            }

            @Override
            public Optional<TagKey<Biome>> biomes() {
                return makeBiomeTag(material, isRare);
            }

            @Override
            public Optional<TagKey<Structure>> structures() {
                return makeStructureTag(material, isRare);
            }
        };
    }

    private RunestoneDefinition obsidian() {
        return new RunestoneDefinition() {
            public final String material = "obsidian";
            public boolean isRare;

            @Override
            public Supplier<? extends Block> block() {
                return feature().registers.blackstoneBlock;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                isRare = random.nextDouble() < 0.5d;
                return randomLocation(this, level, random, 0.33d, 0.8d);
            }

            @Override
            public Optional<TagKey<Item>> items() {
                return makeItemTag(material, isRare);
            }

            @Override
            public Optional<TagKey<Biome>> biomes() {
                return makeBiomeTag(material, isRare);
            }

            @Override
            public Optional<TagKey<Structure>> structures() {
                return makeStructureTag(material, isRare);
            }
        };
    }

    private Optional<RunestoneLocation> randomLocation(RunestoneDefinition definition, LevelAccessor level,
                                                       RandomSource random, double biomeChance, double structureChance) {
        if (random.nextDouble() < biomeChance) {
            return Helpers.randomBiome(level, definition.biomes().orElseThrow(), random);
        }
        if (random.nextDouble() < structureChance) {
            return Helpers.randomStructure(level, definition.structures().orElseThrow(), random);
        }
        return Optional.of(Helpers.SPAWN_POINT);
    }

    private Optional<TagKey<Biome>> makeBiomeTag(String material, boolean isRare) {
        return Optional.of(TagKey.create(Registries.BIOME,
            Strange.id("runestone/" + material + "_runestone" + (isRare ? "_rare" : "") + "_biome_located")));
    }

    private Optional<TagKey<Structure>> makeStructureTag(String material, boolean isRare) {
        return Optional.of(TagKey.create(Registries.STRUCTURE,
            Strange.id("runestone/" + material + "_runestone" + (isRare ? "_rare" : "") + "_structure_located")));
    }

    private Optional<TagKey<Item>> makeItemTag(String material, boolean isRare) {
        return Optional.of(TagKey.create(Registries.ITEM,
            Strange.id("runestone/" + material + "_runestone" + (isRare ? "_rare" : "") + "_item_sacrifices")));
    }
}
