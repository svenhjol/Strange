package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import svenhjol.strange.StrangeTags;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RunestoneDefinitions {
    public static void init() {
        // Register overworld stone runestone.
        Runestones.registerDefinition(new IRunestoneDefinition() {
            @Override
            public Supplier<RunestoneBlock> block() {
                return Runestones.stoneBlock;
            }

            @Override
            public BiFunction<LevelAccessor, BlockPos, Optional<TagKey<?>>> getDestination() {
                return (level, pos) -> {
                    var random = level.getRandom();
                    TagKey<?> tag;

                    if (random.nextDouble() < 0.25d) {
                        tag = random.nextDouble() < 0.25d
                            ? StrangeTags.STONE_RUNESTONE_RARE_BIOME_LOCATED
                            : StrangeTags.STONE_RUNESTONE_BIOME_LOCATED;
                    } else {
                        tag = random.nextDouble() < 0.25d
                            ? StrangeTags.STONE_RUNESTONE_RARE_STRUCTURE_LOCATED
                            : StrangeTags.STONE_RUNESTONE_STRUCTURE_LOCATED;
                    }

                    return Optional.of(tag);
                };
            }
        });

        // Register nether blackstone runestone.
        Runestones.registerDefinition(new IRunestoneDefinition() {
            @Override
            public Supplier<RunestoneBlock> block() {
                return Runestones.blackstoneBlock;
            }

            @Override
            public BiFunction<LevelAccessor, BlockPos, Optional<TagKey<?>>> getDestination() {
                return (level, pos) -> {
                    var random = level.getRandom();
                    TagKey<?> tag;

                    if (random.nextDouble() < 0.33d) {
                        tag = StrangeTags.BLACKSTONE_RUNESTONE_BIOME_LOCATED;
                    } else {
                        tag = StrangeTags.BLACKSTONE_RUNESTONE_STRUCTURE_LOCATED;
                    }

                    return Optional.of(tag);
                };
            }
        });
    }
}
