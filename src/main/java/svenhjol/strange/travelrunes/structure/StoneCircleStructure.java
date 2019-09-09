package svenhjol.strange.travelrunes.structure;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.strange.travelrunes.module.StoneCircles;

import java.util.Random;
import java.util.function.Function;

public class StoneCircleStructure extends Structure<StoneCircleConfig>
{
    public StoneCircleStructure(Function<Dynamic<?>, ? extends StoneCircleConfig> config)
    {
        super(config);
    }

    @Override
    public boolean hasStartAt(ChunkGenerator<?> chunkGenerator, Random rand, int x, int z)
    {
        Biome biome = chunkGenerator.getBiomeProvider().getBiome(new BlockPos((x << 4) + 9, 0, (z << 4) + 9));
        if (chunkGenerator.hasStructure(biome, StoneCircles.structure)) {
            ((SharedSeedRandom)rand).setLargeFeatureSeedWithSalt(chunkGenerator.getSeed(), x, z, 247474724);
            StoneCircleConfig config = chunkGenerator.getStructureConfig(biome, StoneCircles.structure);
            if (config == null) return false;

            boolean result = rand.nextFloat() < config.probability;
            return result;
        } else {
            return false;
        }
    }

    @Override
    public IStartFactory getStartFactory()
    {
        return StoneCircleStructure.Start::new;
    }

    @Override
    public String getStructureName()
    {
        return StoneCircles.NAME;
    }

    @Override
    public int getSize()
    {
        return 1;
    }

    public static class Start extends StructureStart
    {
        public Start(Structure<?> structure, int i1, int i2, Biome biome, MutableBoundingBox bb, int i3, long l)
        {
            super(structure, i1, i2, biome, bb, i3, l);
        }

        @Override
        public void init(ChunkGenerator<?> generator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn)
        {
            int x = chunkX * 16;
            int z = chunkZ * 16;
            BlockPos pos = new BlockPos(x + 9, 90, z + 9);
            this.components.add(new StoneCircle.Piece(pos));
            this.recalculateStructureSize();
        }

        @Override
        public BlockPos getPos()
        {
            return new BlockPos((this.getChunkPosX() << 4) + 9, 0, (this.getChunkPosZ() << 4) + 9);
        }
    }
}
