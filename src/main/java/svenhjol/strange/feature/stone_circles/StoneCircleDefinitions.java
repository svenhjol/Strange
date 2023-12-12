package svenhjol.strange.feature.stone_circles;

import com.mojang.datafixers.util.Pair;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
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
            public Optional<Supplier<RunestoneBlock>> runestoneBlock() {
                return runestonesEnabled ? Optional.of(Runestones.blackstoneBlock) : Optional.empty();
            }
        });
    }
}
