package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeTags;
import svenhjol.strange.feature.stone_circles.StoneCircles;

import java.util.Optional;
import java.util.function.Supplier;

public class RunestoneDefinitions {
    public static void init() {
        var loader = Mods.common(Strange.ID).loader();
        var stoneCirclesEnabled = loader.isEnabled(StoneCircles.class);

        // Stone runestone.
        Runestones.registerDefinition(new IRunestoneDefinition() {
            @Override
            public Supplier<RunestoneBlock> block() {
                return Runestones.stoneBlock;
            }

            @Override
            public Optional<TagKey<?>> getDestination(LevelAccessor level, BlockPos pos, RandomSource random) {
                TagKey<?> tag;

                if (random.nextDouble() < 0.25d) {
                    if (random.nextDouble() < 0.12d) {
                        tag = StrangeTags.STONE_RUNESTONE_RARE_BIOME_LOCATED;
                    } else {
                        tag = StrangeTags.STONE_RUNESTONE_BIOME_LOCATED;
                    }
                } else {
                    if (random.nextDouble() < 0.12d) {
                        tag = StrangeTags.STONE_RUNESTONE_RARE_STRUCTURE_LOCATED;
                    } else {
                        if (stoneCirclesEnabled && random.nextDouble() < 0.4d) {
                            tag = StrangeTags.STONE_RUNESTONE_CIRCLE_LOCATED;
                        } else {
                            tag = StrangeTags.STONE_RUNESTONE_STRUCTURE_LOCATED;
                        }
                    }
                }

                return Optional.of(tag);
            }
        });

        // Blackstone runestone.
        Runestones.registerDefinition(new IRunestoneDefinition() {
            @Override
            public Supplier<RunestoneBlock> block() {
                return Runestones.blackstoneBlock;
            }

            @Override
            public Optional<TagKey<?>> getDestination(LevelAccessor level, BlockPos pos, RandomSource random) {
                TagKey<?> tag;

                if (random.nextDouble() < 0.33d) {
                    tag = StrangeTags.BLACKSTONE_RUNESTONE_BIOME_LOCATED;
                } else {
                    if (stoneCirclesEnabled && random.nextDouble() < 0.2d) {
                        tag = StrangeTags.BLACKSTONE_RUNESTONE_CIRCLE_LOCATED;
                    } else {
                        tag = StrangeTags.BLACKSTONE_RUNESTONE_STRUCTURE_LOCATED;
                    }
                }

                return Optional.of(tag);
            }
        });

        // Obsidian runestone.
        Runestones.registerDefinition(new IRunestoneDefinition() {
            @Override
            public Supplier<RunestoneBlock> block() {
                return Runestones.obsidianBlock;
            }

            @Override
            public Optional<TagKey<?>> getDestination(LevelAccessor level, BlockPos pos, RandomSource random) {
                TagKey<?> tag;

                if (random.nextDouble() < 0.33d) {
                    tag = StrangeTags.OBSIDIAN_RUNESTONE_BIOME_LOCATED;
                } else {
                    if (stoneCirclesEnabled && random.nextDouble() < 0.33d) {
                        tag = StrangeTags.OBSIDIAN_RUNESTONE_CIRCLE_LOCATED;
                    } else {
                        tag = StrangeTags.OBSIDIAN_RUNESTONE_STRUCTURE_LOCATED;
                    }
                }

                return Optional.of(tag);
            }
        });
    }
}
