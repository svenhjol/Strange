package svenhjol.strange.feature.stone_circles.common;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import svenhjol.charm.charmony.Api;
import svenhjol.charm.charmony.Resolve;
import svenhjol.charm.charmony.feature.ProviderHolder;
import svenhjol.strange.Strange;
import svenhjol.strange.api.enums.RunestoneLocationType;
import svenhjol.strange.api.iface.RunestoneDefinition;
import svenhjol.strange.api.iface.RunestoneDefinitionsProvider;
import svenhjol.strange.api.iface.StoneCircleDefinition;
import svenhjol.strange.api.iface.StoneCircleDefinitionsProvider;
import svenhjol.strange.api.impl.RunestoneLocation;
import svenhjol.strange.feature.runestones.Runestones;
import svenhjol.strange.feature.runestones.common.Helpers;
import svenhjol.strange.feature.stone_circles.StoneCircles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class Providers extends ProviderHolder<StoneCircles> implements StoneCircleDefinitionsProvider, RunestoneDefinitionsProvider {
    public static final String DEFAULT = "stone";

    public Codec<StoneCircleDefinition> codec;
    public final Map<String, StoneCircleDefinition> definitions = new HashMap<>();

    public Providers(StoneCircles feature) {
        super(feature);

        // This class is a consumer of StoneCircleDefinitions.
        Api.consume(StoneCircleDefinitionsProvider.class, provider -> {
            for (var definition : provider.getStoneCircleDefinitions()) {
                this.definitions.put(definition.name(), definition);
            }

            codec = StringRepresentable.fromValues(
                () -> definitions.values().toArray(new StoneCircleDefinition[0]));
        });
    }

    @Override
    public List<StoneCircleDefinition> getStoneCircleDefinitions() {
        return List.of(
            stoneCirclesForOverworld(),
            stoneCirclesForTheNether(),
            stoneCirclesForTheEnd());
    }

    @Override
    public List<RunestoneDefinition> getRunestoneDefinitions() {
        return List.of(
            stoneRunestoneCircles(),
            blackstoneRunestoneCircles(),
            obsidianRunestoneCircles()
        );
    }

    private StoneCircleDefinition stoneCirclesForOverworld() {
        return new StoneCircleDefinition() {
            @Override
            public String name() {
                return DEFAULT;
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return TagKey.create(Registries.BLOCK, Strange.id("stone_circle/stone_pillar_blocks"));
            }

            @Override
            public Pair<Integer, Integer> pillarHeight() {
                return Pair.of(3, 8);
            }

            @Override
            public Pair<Integer, Integer> radius() {
                return Pair.of(6, 14);
            }

            @Override
            public Pair<Integer, Integer> degrees() {
                return Pair.of(40, 50);
            }

            @Override
            public int circleJitter() {
                return 1;
            }

            @Override
            public Optional<Supplier<? extends Block>> runestoneBlock() {
                return Optional.ofNullable(Resolve.tryFeature(Runestones.class)
                    .map(runestones -> runestones.registers.stoneBlock)
                    .orElse(null));
            }
        };
    }

    private StoneCircleDefinition stoneCirclesForTheNether() {
        return new StoneCircleDefinition() {
            @Override
            public String name() {
                return "blackstone";
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return TagKey.create(Registries.BLOCK, Strange.id("stone_circle/blackstone_pillar_blocks"));
            }

            @Override
            public Pair<Integer, Integer> pillarHeight() {
                return Pair.of(4, 6);
            }

            @Override
            public Pair<Integer, Integer> radius() {
                return Pair.of(5, 10);
            }

            @Override
            public Pair<Integer, Integer> degrees() {
                return Pair.of(30, 45);
            }

            @Override
            public int circleJitter() {
                return 2;
            }

            @Override
            public int terrainHeightTolerance() {
                return 40;
            }

            @Override
            public BlockPos ceilingReposition(WorldGenLevel level, BlockPos pos) {
                var foundSpace = false;
                var min = level.getMinBuildHeight() + 15;
                var random = level.getRandom();
                var maxTries = 8;

                for (int tries = 1; tries <= maxTries; tries++) {
                    var x = pos.getX() + random.nextInt(tries * 2) - tries;
                    var z = pos.getZ() + random.nextInt(tries * 2) - tries;

                    for (int i = pos.getY() - 30; i > min; i--) {
                        var checkPos = new BlockPos(x, i, z);
                        var checkState = level.getBlockState(checkPos);
                        var checkBelowState = level.getBlockState(checkPos.below());

                        if (checkState.isAir() && (checkBelowState.canOcclude() || checkBelowState.getFluidState().is(Fluids.LAVA))) {
                            pos = checkPos;
                            foundSpace = true;
                            break;
                        }
                    }

                    if (foundSpace) break;
                }

                if (!foundSpace) {
                    pos = StoneCircleDefinition.super.ceilingReposition(level, pos);
                }
                return pos;
            }

            @Override
            public Optional<Supplier<? extends Block>> runestoneBlock() {
                return Optional.ofNullable(Resolve.tryFeature(Runestones.class)
                    .map(runestones -> runestones.registers.blackstoneBlock)
                    .orElse(null));
            }
        };
    }

    private StoneCircleDefinition stoneCirclesForTheEnd() {
        return new StoneCircleDefinition() {
            @Override
            public String name() {
                return "obsidian";
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return TagKey.create(Registries.BLOCK, Strange.id("stone_circle/obsidian_pillar_blocks"));
            }

            @Override
            public Pair<Integer, Integer> pillarHeight() {
                return Pair.of(4, 6);
            }

            @Override
            public Pair<Integer, Integer> radius() {
                return Pair.of(8, 16);
            }

            @Override
            public Pair<Integer, Integer> degrees() {
                return Pair.of(35, 55);
            }

            @Override
            public double runestoneChance() {
                return 0.5d;
            }

            @Override
            public int maxRunestones() {
                return 10;
            }

            @Override
            public Optional<Supplier<? extends Block>> runestoneBlock() {
                return Optional.ofNullable(Resolve.tryFeature(Runestones.class)
                    .map(runestones -> runestones.registers.obsidianBlock)
                    .orElse(null));
            }
        };
    }

    private RunestoneDefinition stoneRunestoneCircles() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return runestones().registers.stoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.CRACKED_STONE_BRICKS;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                if (feature().isEnabled() && random.nextDouble() < feature().stoneCircleRunestoneChance()) {
                    return Optional.of(new RunestoneLocation(RunestoneLocationType.STRUCTURE, Strange.id("stone_circle_stone")));
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/stone_runestone_item_sacrifices");
            }
        };
    }

    private RunestoneDefinition blackstoneRunestoneCircles() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return runestones().registers.blackstoneBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                if (feature().isEnabled() && random.nextDouble() < feature().stoneCircleRunestoneChance()) {
                    return Optional.of(new RunestoneLocation(RunestoneLocationType.STRUCTURE, Strange.id("stone_circle_blackstone")));
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/blackstone_runestone_item_sacrifices");
            }
        };
    }

    private RunestoneDefinition obsidianRunestoneCircles() {
        return new RunestoneDefinition() {
            @Override
            public Supplier<? extends Block> runestoneBlock() {
                return runestones().registers.obsidianBlock;
            }

            @Override
            public Supplier<? extends Block> baseBlock() {
                return () -> Blocks.CRYING_OBSIDIAN;
            }

            @Override
            public Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random) {
                if (feature().isEnabled() && random.nextDouble() < feature().stoneCircleRunestoneChance()) {
                    return Optional.of(new RunestoneLocation(RunestoneLocationType.STRUCTURE, Strange.id("stone_circle_obsidian")));
                }
                return Optional.empty();
            }

            @Override
            public Supplier<ItemLike> sacrifice(LevelAccessor level, BlockPos pos, RandomSource random) {
                return () -> Helpers.randomItem(level, random, "runestone/obsidian_runestone_item_sacrifices");
            }
        };
    }

    private Runestones runestones() {
        return Resolve.feature(Runestones.class);
    }
}
