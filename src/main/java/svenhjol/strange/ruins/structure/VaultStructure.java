package svenhjol.strange.ruins.structure;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.strange.Strange;
import svenhjol.strange.ruins.module.Vaults;

import java.util.Objects;
import java.util.Random;

public class VaultStructure extends ScatteredStructure<NoFeatureConfig> {
    public static final int SEED_MODIFIER = 188492881;
    public static final int MIN_DISTANCE = 8000;

    public VaultStructure() {
        super(config -> NoFeatureConfig.NO_FEATURE_CONFIG);
        setRegistryName(Strange.MOD_ID, Vaults.NAME);
    }

    @Override
    public String getStructureName() {
        return Objects.requireNonNull(getRegistryName()).toString();
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean func_225558_a_(BiomeManager biomes, ChunkGenerator<?> gen, Random rand, int x, int z, Biome biome) {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            BlockPos pos = new BlockPos((x << 4) + 9, 0, (z << 4) + 9);

            Biome b = biomes.getBiome(pos);
            return Math.abs(pos.getX()) > MIN_DISTANCE
                && Math.abs(pos.getZ()) > MIN_DISTANCE
                && gen.hasStructure(b, Vaults.structure);
        }

        return false;
    }

    @Override
    protected int getSeedModifier() {
        return SEED_MODIFIER;
    }

    @Override
    public IStartFactory getStartFactory() {
        return VaultStructure.Start::new;
    }

    public static class Start extends StructureStart {
        public Start(Structure<?> structure, int chunkX, int chunkZ, MutableBoundingBox bb, int ref, long seed) {
            super(structure, chunkX, chunkZ, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> gen, TemplateManager templates, int chunkX, int chunkZ, Biome biomeIn) {
            final BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);

            // create vaults beneath the circle
            if (gen instanceof OverworldChunkGenerator) {
                final String dir = Vaults.isValidPosition(pos) ? Vaults.VAULTS_DIR : Vaults.VAULTS_LOCAL;
                final ResourceLocation start = new ResourceLocation(Strange.MOD_ID, dir + "/starts");

                JigsawManager.func_214889_a(start, Vaults.size, VaultPiece::new, gen, templates, pos, components, rand);
                this.recalculateStructureSize();

                // move components into the earth
                if (bounds.maxY >= Vaults.generateBelow) {
                    int shift = 5 + (bounds.maxY - Vaults.generateBelow);
                    bounds.offset(0, -shift, 0);
                    components.forEach(p -> p.offset(0, -shift, 0));
                }

                if (bounds.minY < Vaults.generateAbove) {
                    int shift = Vaults.generateAbove - bounds.minY;
                    bounds.offset(0, shift, 0);
                    components.forEach(p -> p.offset(0, shift, 0));
                    //  Strange.LOG.debug("[VaultStructure] Shifting up by " + shift + " at " + pos);
                }
            }
        }
    }
}
