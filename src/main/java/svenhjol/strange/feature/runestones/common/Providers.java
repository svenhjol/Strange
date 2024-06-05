package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
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
        return new DefaultRunestoneDefinition("stone", 0.12d, 0.25d, 0.75d) {
            @Override
            public Supplier<? extends Block> block() {
                return feature().registers.stoneBlock;
            }
        };
    }

    private RunestoneDefinition blackstone() {
        return new DefaultRunestoneDefinition("blackstone", 0.0d, 0.33d, 0.7d) {
            @Override
            public Supplier<? extends Block> block() {
                return feature().registers.blackstoneBlock;
            }
        };
    }

    private RunestoneDefinition obsidian() {
        return new DefaultRunestoneDefinition("obsidian", 0.0d, 0.25d, 0.8d) {
            @Override
            public Supplier<? extends Block> block() {
                return feature().registers.obsidianBlock;
            }
        };
    }

    /**
     * A simple implementation of RunestoneDefinition that allows for chance of a runestone to link to a biome
     * or structure, falling back to player spawn point.
     * Rarity calculation also adds some chance of more lucrative biomes and locations for the overworld.
     */
    private abstract static class DefaultRunestoneDefinition implements RunestoneDefinition {
        private final String material;
        private final double rareChance;
        private final double biomeChance;
        private final double structureChance;
        private boolean isRare;

        public DefaultRunestoneDefinition(String material, double rareChance, double biomeChance, double structureChance) {
            this.material = material;
            this.rareChance = rareChance;
            this.biomeChance = biomeChance;
            this.structureChance = structureChance;
        }

        @Override
        public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
            this.isRare = random.nextDouble() < rareChance;
            var spawnPoint = Optional.of(Helpers.SPAWN_POINT);
            Optional<RunestoneLocation> opt;

            if (random.nextDouble() < biomeChance) {
                opt = Helpers.randomBiome(level, makeBiomeTag(), random);
            } else if (random.nextDouble() < structureChance) {
                opt = Helpers.randomStructure(level, makeStructureTag(), random);
            } else {
                opt = Optional.empty();
            }

            // Default to spawnpoint if checks don't pass.
            if (opt.isEmpty()) {
                return spawnPoint;
            }

            return opt;
        }

        @Override
        public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
            return () -> Helpers.randomItem(level, makeItemTag(), random).orElseThrow(
                () -> new RuntimeException("Could not find an item to use for the sacrifice"));
        }

        private TagKey<Biome> makeBiomeTag() {
            return TagKey.create(Registries.BIOME,
                Strange.id("runestone/" + material + "_runestone" + (isRare ? "_rare" : "") + "_biome_located"));
        }

        private TagKey<Structure> makeStructureTag() {
            return TagKey.create(Registries.STRUCTURE,
                Strange.id("runestone/" + material + "_runestone" + (isRare ? "_rare" : "") + "_structure_located"));
        }

        private TagKey<Item> makeItemTag() {
            return TagKey.create(Registries.ITEM,
                Strange.id("runestone/" + material + "_runestone" + (isRare ? "_rare" : "") + "_item_sacrifices"));
        }
    }
}
