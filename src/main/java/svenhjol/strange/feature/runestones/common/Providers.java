package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
                var blockSupplier = definition.runestoneBlock();
                feature().registry().blockEntityBlocks(feature().registers.blockEntity, List.of(blockSupplier));

                // Add the definition to the full set for mapping later.
                definitions.add(definition);
            }
        });
    }

    @Override
    public List<RunestoneDefinition> getRunestoneDefinitions() {
        return List.of(
            stone(),
            stoneRare(),
            stoneSpawnPoint(),
            blackstone(),
            blackstoneSpawnPoint(),
            obsidian(),
            obsidianSpawnPoint()
        );
    }

    private RunestoneDefinition stone() {
        return new CustomRunestoneDefinition(0.5d, 0.9d) {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.stoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.STONE;
            }

            @Override
            public String biomeTagPath() {
                return "runestone/stone_runestone_biome_located";
            }

            @Override
            public String structureTagPath() {
                return "runestone/stone_runestone_structure_located";
            }

            @Override
            public String ItemTagPath() {
                return "runestone/stone_runestone_item_sacrifices";
            }
        };
    }

    private RunestoneDefinition stoneRare() {
        return new CustomRunestoneDefinition(0.2d, 0.33d) {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.stoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.COBBLESTONE;
            }

            @Override
            public String biomeTagPath() {
                return "runestone/stone_runestone_rare_biome_located";
            }

            @Override
            public String structureTagPath() {
                return "runestone/stone_runestone_rare_structure_located";
            }

            @Override
            public String ItemTagPath() {
                return "runestone/stone_runestone_rare_item_sacrifices";
            }
        };
    }

    private RunestoneDefinition stoneSpawnPoint() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.stoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.MOSSY_COBBLESTONE;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                if (random.nextDouble() < 0.33d) {
                    return Optional.of(Helpers.SPAWN_POINT);
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/stone_runestone_item_sacrifices");
            }
        };
    }

    private RunestoneDefinition blackstone() {
        return new CustomRunestoneDefinition(0.6d, 0.9d) {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.blackstoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.BLACKSTONE;
            }

            @Override
            public String biomeTagPath() {
                return "runestone/blackstone_runestone_biome_located";
            }

            @Override
            public String structureTagPath() {
                return "runestone/blackstone_runestone_structure_located";
            }

            @Override
            public String ItemTagPath() {
                return "runestone/blackstone_runestone_item_sacrifices";
            }
        };
    }

    private RunestoneDefinition blackstoneSpawnPoint() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.blackstoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.GILDED_BLACKSTONE;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                return Optional.of(Helpers.SPAWN_POINT);
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/blackstone_runestone_item_sacrifices");
            }
        };
    }

    private RunestoneDefinition obsidian() {
        return new CustomRunestoneDefinition(0.5d, 0.9d) {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.obsidianBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.OBSIDIAN;
            }

            @Override
            public String biomeTagPath() {
                return "runestone/obsidian_runestone_biome_located";
            }

            @Override
            public String structureTagPath() {
                return "runestone/obsidian_runestone_structure_located";
            }

            @Override
            public String ItemTagPath() {
                return "runestone/obsidian_runestone_item_sacrifices";
            }
        };
    }

    private RunestoneDefinition obsidianSpawnPoint() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return feature().registers.obsidianBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.CRYING_OBSIDIAN;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                return Optional.of(Helpers.SPAWN_POINT);
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/obsidian_runestone_item_sacrifices");
            }
        };
    }

    /**
     * A simple implementation of RunestoneDefinition with chance of a runestone to link to a biome or a structure.
     */
    private abstract static class CustomRunestoneDefinition implements RunestoneDefinition {
        private final double biomeChance;
        private final double structureChance;

        public CustomRunestoneDefinition(double biomeChance, double structureChance) {
            this.biomeChance = biomeChance;
            this.structureChance = structureChance;
        }

        @Override
        public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
            if (random.nextDouble() < biomeChance) {
                return Helpers.randomBiome(level, random, TagKey.create(Registries.BIOME, Strange.id(biomeTagPath())));
            }
            if (random.nextDouble() < structureChance) {
                return Helpers.randomStructure(level, random, TagKey.create(Registries.STRUCTURE, Strange.id(structureTagPath())));
            }

            return Optional.empty();
        }

        @Override
        public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
            return () -> Helpers.randomItem(level, random, ItemTagPath());
        }

        public abstract String biomeTagPath();

        public abstract String structureTagPath();

        public abstract String ItemTagPath();
    }
}
