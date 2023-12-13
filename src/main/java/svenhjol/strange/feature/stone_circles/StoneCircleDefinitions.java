package svenhjol.strange.feature.stone_circles;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeTags;
import svenhjol.strange.feature.runestones.RunestoneBlock;
import svenhjol.strange.feature.runestones.Runestones;

import java.util.Optional;
import java.util.function.Supplier;

public class StoneCircleDefinitions {
    public static final String DEFAULT = "stone";
    public static void init() {
        var loader = Mods.common(Strange.ID).loader();
        var runestonesEnabled = loader.isEnabled(Runestones.class);

        // Overworld stone circles.
        StoneCircles.registerDefinition(new IStoneCircleDefinition() {
            @Override
            public String name() {
                return DEFAULT;
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return StrangeTags.STONE_PILLAR_BLOCKS;
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
            public Optional<Supplier<RunestoneBlock>> runestoneBlock() {
                return runestonesEnabled ? Optional.of(Runestones.stoneBlock) : Optional.empty();
            }
        });

        // Nether stone circles.
        StoneCircles.registerDefinition(new IStoneCircleDefinition() {
            @Override
            public String name() {
                return "blackstone";
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return StrangeTags.BLACKSTONE_PILLAR_BLOCKS;
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
                    pos = IStoneCircleDefinition.super.ceilingReposition(level, pos);
                }
                return pos;
            }

            @Override
            public Optional<Supplier<RunestoneBlock>> runestoneBlock() {
                return runestonesEnabled ? Optional.of(Runestones.blackstoneBlock) : Optional.empty();
            }
        });

        // End stone circles.
        StoneCircles.registerDefinition(new IStoneCircleDefinition() {
            @Override
            public String name() {
                return "obsidian";
            }

            @Override
            public TagKey<Block> pillarBlocks() {
                return StrangeTags.OBSIDIAN_PILLAR_BLOCKS;
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
            public Optional<Supplier<RunestoneBlock>> runestoneBlock() {
                return runestonesEnabled ? Optional.of(Runestones.obsidianBlock) : Optional.empty();
            }
        });
    }
}
