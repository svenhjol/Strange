package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
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
            public Optional<Location> getLocation(LevelAccessor level, BlockPos pos, RandomSource random) {
                Optional<Location> location;

                if (random.nextDouble() < 0.25d) {
                    TagKey<Biome> tag;
                    if (random.nextDouble() < 0.12d) {
                        tag = StrangeTags.STONE_RUNESTONE_RARE_BIOME_LOCATED;
                    } else {
                        tag = StrangeTags.STONE_RUNESTONE_BIOME_LOCATED;
                    }
                    location = RunestoneHelper.getRandomLocation(level.registryAccess(), tag, LocationType.BIOME, random);
                } else {
                    TagKey<Structure> tag;
                    if (random.nextDouble() < 0.12d) {
                        tag = StrangeTags.STONE_RUNESTONE_RARE_STRUCTURE_LOCATED;
                    } else {
                        if (stoneCirclesEnabled && random.nextDouble() < 0.2d) {
                            tag = StrangeTags.STONE_RUNESTONE_CIRCLE_LOCATED;
                        } else {
                            tag = StrangeTags.STONE_RUNESTONE_STRUCTURE_LOCATED;
                        }
                    }
                    location = RunestoneHelper.getRandomLocation(level.registryAccess(), tag, LocationType.STRUCTURE, random);
                }

                return location;
            }
        });

        // Blackstone runestone.
        Runestones.registerDefinition(new IRunestoneDefinition() {
            @Override
            public Supplier<RunestoneBlock> block() {
                return Runestones.blackstoneBlock;
            }

            @Override
            public Optional<Location> getLocation(LevelAccessor level, BlockPos pos, RandomSource random) {
                Optional<Location> location;

                if (random.nextDouble() < 0.33d) {
                    var tag = StrangeTags.BLACKSTONE_RUNESTONE_BIOME_LOCATED;
                    location = RunestoneHelper.getRandomLocation(level.registryAccess(), tag, LocationType.BIOME, random);
                } else {
                    TagKey<Structure> tag;
                    if (stoneCirclesEnabled && random.nextDouble() < 0.2d) {
                        tag = StrangeTags.BLACKSTONE_RUNESTONE_CIRCLE_LOCATED;
                    } else {
                        tag = StrangeTags.BLACKSTONE_RUNESTONE_STRUCTURE_LOCATED;
                    }
                    location = RunestoneHelper.getRandomLocation(level.registryAccess(), tag, LocationType.STRUCTURE, random);
                }

                return location;
            }
        });

        // Obsidian runestone.
        Runestones.registerDefinition(new IRunestoneDefinition() {
            @Override
            public Supplier<RunestoneBlock> block() {
                return Runestones.obsidianBlock;
            }

            @Override
            public Optional<Location> getLocation(LevelAccessor level, BlockPos pos, RandomSource random) {
                Optional<Location> location;

                if (random.nextDouble() < 0.33d) {
                    var tag = StrangeTags.OBSIDIAN_RUNESTONE_BIOME_LOCATED;
                    location = RunestoneHelper.getRandomLocation(level.registryAccess(), tag, LocationType.BIOME, random);
                } else {
                    TagKey<Structure> tag;
                    if (stoneCirclesEnabled && random.nextDouble() < 0.33d) {
                        tag = StrangeTags.OBSIDIAN_RUNESTONE_CIRCLE_LOCATED;
                    } else {
                        tag = StrangeTags.OBSIDIAN_RUNESTONE_STRUCTURE_LOCATED;
                    }
                    location = RunestoneHelper.getRandomLocation(level.registryAccess(), tag, LocationType.STRUCTURE, random);
                }

                return location;
            }
        });
    }
}
