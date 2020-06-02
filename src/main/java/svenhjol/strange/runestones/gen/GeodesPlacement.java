package svenhjol.strange.runestones.gen;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.SimplePlacement;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

public class GeodesPlacement extends SimplePlacement<ChanceConfig> {
    public GeodesPlacement(Function<Dynamic<?>, ? extends ChanceConfig> config) {
        super(config);
    }

    @Override
    protected Stream<BlockPos> getPositions(Random rand, ChanceConfig config, BlockPos pos) {
        List<BlockPos> locations = Lists.newArrayList();

        for (int i = 0; i < rand.nextInt(rand.nextInt(config.chance) + 1) + 1; ++i) {
            if (rand.nextFloat() < 0.75F) continue;

            int x = rand.nextInt(16);
            int y = rand.nextInt(16) + 60;
            int z = rand.nextInt(16);
            locations.add(pos.add(x, y, z));
        }

        return locations.stream();
    }
}
