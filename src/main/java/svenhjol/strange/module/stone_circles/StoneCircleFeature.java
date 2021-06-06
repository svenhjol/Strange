package svenhjol.strange.module.stone_circles;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class StoneCircleFeature extends StructureFeature<NoneFeatureConfiguration> {
    public StoneCircleFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return Start::new;
    }

    public static class Start extends StructureStart<NoneFeatureConfiguration> {
        public Start(StructureFeature<NoneFeatureConfiguration> feature, ChunkPos chunkPos, int references, long seed) {
            super(feature, chunkPos, references, seed);
        }

        @Override
        public void generatePieces(RegistryAccess registryManager, ChunkGenerator chunkGenerator, StructureManager manager, ChunkPos chunkPos, Biome biome, NoneFeatureConfiguration config, LevelHeightAccessor heightLimitView) {
            int x = chunkPos.getMinBlockX();
            int z = chunkPos.getMinBlockZ();
            int y = chunkGenerator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightLimitView);

            pieces.add(new StoneCircleStructurePiece(random, x, y, z));
            getBoundingBox();
        }
    }
}
