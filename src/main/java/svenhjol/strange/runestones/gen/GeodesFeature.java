package svenhjol.strange.runestones.gen;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import svenhjol.strange.runestones.block.MoonstoneBlock;
import svenhjol.strange.runestones.module.Amethyst;
import svenhjol.strange.runestones.module.Moonstones;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class GeodesFeature extends Feature<GeodesConfig> {
   private static final BlockState AIR = Blocks.CAVE_AIR.getDefaultState();

   public GeodesFeature(Function<Dynamic<?>, ? extends GeodesConfig> p_i51485_1_) {
      super(p_i51485_1_);
   }

   public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, GeodesConfig config) {
      int startdepth = 8;
      int xmax = 24;
      int ymax = 24;
      int zmax = 24;
      int total = xmax * ymax * zmax;

//      List<CaveCrystalBlock> crystals = CaveCrystalUndergroundBiomeModule.crystals;
      List<MoonstoneBlock> moonstones = Moonstones.moonstones;
      int geodeColor1 = rand.nextInt(moonstones.size());
      int geodeColor2 = rand.nextInt(moonstones.size());

      if (geodeColor1 == geodeColor2) {
         geodeColor2 = (geodeColor1 + 1) % moonstones.size();
      }

      while(pos.getY() > 5 && !worldIn.isAirBlock(pos)) {
         pos = pos.up();
      }

      if (pos.getY() <= 4) {
         return false;
      } else {
         boolean[] outershell = new boolean[total];
         boolean[] hollow = new boolean[total];
         boolean[] decoration = new boolean[total];
         pos = pos.down(startdepth);

         int i = rand.nextInt(30);
         if (i > 1) return false;

         for(int j = 0; j < i; ++j) {
            double d0 = rand.nextDouble() * 6.0D + 4.0D;
            double d1 = rand.nextDouble() * 5.5D + 4.5D;
            double d2 = rand.nextDouble() * 6.0D + 4.0D;
            double d3 = rand.nextDouble() * (16.0D - d0 - 2.0D) + 1.0D + d0 / 2.0D;
            double d4 = rand.nextDouble() * (16.0D - d1 - 2.0D) + 1.0D + d1 / 2.0D;
            double d5 = rand.nextDouble() * (16.0D - d2 - 2.0D) + 1.0D + d2 / 2.0D;

            for(int x = 1; x < xmax; ++x) {
               for(int z = 1; z < zmax; ++z) {
                  for(int y = 1; y < ymax; ++y) {
                     double d6 = ((double)x - d3) / (d0 / 2.0D);
                     double d7 = ((double)y - d4) / (d1 / 2.0D);
                     double d8 = ((double)z - d5) / (d2 / 2.0D);
                     double d9 = d6 * d6 + d7 * d7 + d8 * d8;

                     int index = (x * xmax + z) * zmax + y;

                     if (d9 < 2.5D)
                        outershell[index] = true;

                     if (d9 < 1.3D)
                        hollow[index] = true;

                     if (d9 < 1.2D && d9 > 0.9D)
                        decoration[index] = true;
                  }
               }
            }
         }

         for (int x = 0; x < xmax; ++x) {
            for (int z = 0; z < zmax; ++z) {
               for (int y = 0; y < ymax; ++y) {
                  int index = (x * xmax + z) * zmax + y;
                  if (outershell[index]) {
                     worldIn.setBlockState(pos.add(x, y, z), y >= 4 ? Blocks.END_STONE.getDefaultState() : config.state, 2);
                  }

                  if (hollow[index]) {
                     worldIn.setBlockState(pos.add(x, y, z), Blocks.AIR.getDefaultState(), 2);
                  }

                  if (decoration[index] && rand.nextFloat() > 0.3F) {
                     BlockPos pa = pos.add(x, y, z);
                     if (rand.nextFloat() < 0.1F) {
                        worldIn.setBlockState(pa, Blocks.END_STONE.getDefaultState(), 2);
                     } else if (rand.nextFloat() < 0.05F) {
                        worldIn.setBlockState(pa, Amethyst.block.getDefaultState(), 2);
                     } else {
                        int col = rand.nextFloat() < 0.5F ? geodeColor1 : geodeColor2;
                        BlockState state = moonstones.get(col).getDefaultState();
                        worldIn.setBlockState(pa, state, 2);
                     }
                  }
               }
            }
         }

         return true;
      }
   }
}