package svenhjol.strange.travelrunes.feature;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import svenhjol.meson.Meson;
import svenhjol.strange.travelrunes.block.BaseRunestoneBlock;
import svenhjol.strange.travelrunes.module.Runestones;

import java.util.Random;
import java.util.function.Function;

public class StoneCircleFeature extends Feature<NoFeatureConfig>
{
    public StoneCircleFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> func)
    {
        super(func);
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config)
    {
        boolean generated = false;
        boolean withRune = false;

        for(int b = 0; b < 20; ++b) {
            BlockPos blockpos = pos.add(rand.nextInt(4) - rand.nextInt(4), 0, rand.nextInt(4) - rand.nextInt(4));

            if (worldIn.isAirBlock(blockpos)) {
                BlockPos blockpos1 = blockpos.down();
                if (worldIn.getBlockState(blockpos1).isSolid()
                    && worldIn.isSkyLightMax(blockpos1)
                    && blockpos.getY() > worldIn.getSeaLevel()
                ) {

                    // generate circle
                    int range = rand.nextInt(2) + 2;

                    for (int i = -range; i <= range; i+=2) {
                        for (int j = -range; j <= range; j+=2) {
                            float dist = (i * i) + (j * j);
                            if (dist < 10 || dist > 14)
                                continue;

                            for (int k = 2; k > -2; k--) {
                                BlockPos fpos = pos.add(i, k, j);
                                BlockState fstate = worldIn.getBlockState(fpos);
                                BlockState fstateup = worldIn.getBlockState(fpos.up());
                                if (rand.nextFloat() < 0.75F && fstate.isSolid() && fstate.isOpaqueCube(worldIn, fpos) && fstateup.isAir()) {

                                    int maxHeight = rand.nextInt(3) + 3;
                                    worldIn.setBlockState(fpos, Blocks.STONE.getDefaultState(), 2);

                                    for (int l = 1; l < maxHeight; l++) {
                                        BlockState setState = null;
                                        float f = rand.nextFloat();

                                        if (f < 0.6F) {
                                            setState = Blocks.COBBLESTONE.getDefaultState();
                                        } else if (f < 0.8F) {
                                            setState = Blocks.MOSSY_COBBLESTONE.getDefaultState();
                                        } else if (f < 0.94F) {
                                            setState = Blocks.STONE.getDefaultState();
                                        } else {
                                            withRune = true;
                                            setState = Runestones.stone.getDefaultState().with(BaseRunestoneBlock.RUNE, rand.nextInt(12));
                                        }
                                        worldIn.setBlockState(fpos.up(l), setState, 2);
                                    }

                                    break;
                                }
                            }
                        }
                    }

                    if (withRune) {
                        Meson.log("Generated with rune " + blockpos);
                    }
                    break;
                }
            }
        }

        return generated;
    }
}
