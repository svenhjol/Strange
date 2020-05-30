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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class GeodesFeature extends Feature<GeodesConfig> {
   public enum GeodeType {
      MOONSTONES,
      AMETHYST,
      PORTAL
   }

   public GeodesFeature(Function<Dynamic<?>, ? extends GeodesConfig> p_i51485_1_) {
      super(p_i51485_1_);
   }

   public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, GeodesConfig config) {
      int startdepth = 8;
      int xmax = 24;
      int ymax = 24;
      int zmax = 24;
      int total = xmax * ymax * zmax;
      int chance = 5; // 1 in n

      double outerEdge = 2.5D;
      double innerEdge = 1.3D;
      double decorationInnerEdge = 0.9D;
      double decorationOuterEdge = 1.2D;

      List<GeodeType> geodeTypes = new ArrayList<>(Arrays.asList(GeodeType.values()));
      GeodeType geodeType = geodeTypes.get(rand.nextInt(geodeTypes.size() - 1));

      List<MoonstoneBlock> moonstones = Moonstones.moonstones;
      int geodeColor1 = rand.nextInt(moonstones.size());
      int geodeColor2 = rand.nextInt(moonstones.size());

      if (geodeColor1 == geodeColor2)
         geodeColor2 = (geodeColor1 + 1) % moonstones.size();



      // tweak values based on geode type
      if (geodeType == GeodeType.AMETHYST) {
         decorationInnerEdge = 0.8D;
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

         int i = rand.nextInt(chance);
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

                     if (d9 < outerEdge)
                        outershell[index] = true;

                     if (d9 < innerEdge)
                        hollow[index] = true;

                     if (d9 < decorationOuterEdge && d9 > decorationInnerEdge)
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

                  if (decoration[index]) {
                     BlockPos pa = pos.add(x, y, z);

                     if (geodeType == GeodeType.MOONSTONES) {
                        renderMoonstoneDetail(worldIn, pa, rand, geodeColor1, geodeColor2);
                     } else if (geodeType == GeodeType.AMETHYST) {
                        renderAmethystDetail(worldIn, pa, rand);
                     }
                  }
               }
            }
         }

         return true;
      }
   }

   private void renderMoonstoneDetail(IWorld world, BlockPos pos, Random rand, int geodeColor1, int geodeColor2) {
      if (rand.nextFloat() < 0.1F) {
         world.setBlockState(pos, Blocks.END_STONE.getDefaultState(), 2);
      } else if (rand.nextFloat() < 0.8F) {
         int col = rand.nextFloat() < 0.5F ? geodeColor1 : geodeColor2;
         BlockState state = Moonstones.moonstones.get(col).getDefaultState();
         world.setBlockState(pos, state, 2);
      }
   }

   private void renderAmethystDetail(IWorld world, BlockPos pos, Random rand) {
      if (rand.nextFloat() < 0.6F) {
         world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), 2);
      } else {
         world.setBlockState(pos, Amethyst.block.getDefaultState(), 2);
      }
   }

   private void renderPortalGeode() {
      // TODO
   }
}