package svenhjol.strange.runestones.gen;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;
import java.util.function.Function;

public class GeodesFeature extends Feature<GeodesConfig> {
   private static final BlockState AIR = Blocks.CAVE_AIR.getDefaultState();

   public GeodesFeature(Function<Dynamic<?>, ? extends GeodesConfig> p_i51485_1_) {
      super(p_i51485_1_);
   }

   public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, GeodesConfig config) {
      int startdepth = 16;
      int xmax = 128;
      int ymax = 80;
      int zmax = 128;
      int total = xmax * ymax * zmax;

      while(pos.getY() > 5 && worldIn.isAirBlock(pos)) {
         pos = pos.down();
      }

      if (pos.getY() <= 4) {
         return false;
      } else {
         pos = pos.down(startdepth);
         int i = rand.nextInt(1) + 1;

         for(int j = 0; j < i; ++j) {
            double d0 = rand.nextDouble() * 6.0D + 3.0D;
            double d1 = rand.nextDouble() * 6.0D + 3.0D;
            double d2 = rand.nextDouble() * 6.0D + 3.0D;
            double d3 = rand.nextDouble() * ((double)xmax - d0 - 2.0D) + 1.0D + d0 / 2.0D;
            double d4 = rand.nextDouble() * ((double)ymax - d1 - 2.0D) + 1.0D + d1 / 1.0D;
            double d5 = rand.nextDouble() * ((double)zmax - d2 - 2.0D) + 1.0D + d2 / 2.0D;

            for(int x = 1; x < xmax; ++x) {
               for(int z = 1; z < zmax; ++z) {
                  for(int y = 1; y < ymax; ++y) {
                     double d6 = ((double)x - d3) / (d0 / 2.0D);
                     double d7 = ((double)y - d4) / (d1 / 2.0D);
                     double d8 = ((double)z - d5) / (d2 / 2.0D);
                     double d9 = d6 * d6 + d7 * d7 + d8 * d8;
                     if (d9 < 7.0D) {
                        worldIn.setBlockState(pos.add(x, y, z), y >= 4 ? AIR : config.state, 2);
//                        aboolean[(x * xmax + z) * zmax + y] = true;
                     }
                  }
               }
            }
         }

//         for(int l1 = 0; l1 < xmax; ++l1) {
//            for(int i3 = 0; i3 < zmax; ++i3) {
//               for(int i4 = 0; i4 < ymax; ++i4) {
//                  if (aboolean[(l1 * xmax + i3) * zmax + i4]) {
//                     worldIn.setBlockState(pos.add(l1, i4, i3), i4 >= 4 ? AIR : config.state, 2);
//                  }
//               }
//            }
//         }
//
//         for(int i2 = 0; i2 < xmax; ++i2) {
//            for(int j3 = 0; j3 < zmax; ++j3) {
//               for(int j4 = 4; j4 < ymax; ++j4) {
//                  if (aboolean[(i2 * xmax + j3) * zmax + j4]) {
//                     BlockPos blockpos = pos.add(i2, j4 - 1, j3);
//                     if (Block.isDirt(worldIn.getBlockState(blockpos).getBlock()) && worldIn.getLightFor(LightType.SKY, pos.add(i2, j4, j3)) > 0) {
//                        Biome biome = worldIn.getBiome(blockpos);
//                        if (biome.getSurfaceBuilderConfig().getTop().getBlock() == Blocks.MYCELIUM) {
//                           worldIn.setBlockState(blockpos, Blocks.MYCELIUM.getDefaultState(), 2);
//                        } else {
//                           worldIn.setBlockState(blockpos, Blocks.GRASS_BLOCK.getDefaultState(), 2);
//                        }
//                     }
//                  }
//               }
//            }
//         }

         return true;
      }
   }
}