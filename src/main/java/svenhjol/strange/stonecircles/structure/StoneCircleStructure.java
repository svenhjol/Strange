package svenhjol.strange.stonecircles.structure;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.EndChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.module.Runestones;
import svenhjol.strange.stonecircles.module.StoneCircles;
import svenhjol.strange.stonecircles.module.Vaults;

import java.util.Objects;
import java.util.Random;

public class StoneCircleStructure extends ScatteredStructure<NoFeatureConfig> {
    public static final int SEED_MODIFIER = 1684681;
    public static final int MIN_DISTANCE = 150;

    public StoneCircleStructure() {
        super(config -> NoFeatureConfig.NO_FEATURE_CONFIG);
        setRegistryName(Strange.MOD_ID, StoneCircles.NAME);
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
    protected int getBiomeFeatureDistance(ChunkGenerator<?> gen) {
        int dist = StoneCircles.distance;
        if (gen instanceof EndChunkGenerator) {
            return dist / 2;
        } else {
            return dist;
        }
    }

    @Override
    protected int getBiomeFeatureSeparation(ChunkGenerator<?> gen) {
        int dist = StoneCircles.distance;
        if (gen instanceof EndChunkGenerator) {
            return dist / 4;
        } else {
            return dist / 2;
        }
    }

    @Override
    public boolean canBeGenerated(BiomeManager biomes, ChunkGenerator<?> gen, Random rand, int x, int z, Biome biome) {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            BlockPos pos = new BlockPos((x << 4) + 9, 0, (z << 4) + 9);
            Biome b = biomes.getBiome(pos);

            return !Runestones.destinations.isEmpty()
                && gen.hasStructure(b, StoneCircles.structure)
                && Math.abs(pos.getX()) > MIN_DISTANCE
                && Math.abs(pos.getZ()) > MIN_DISTANCE;
        }

        return false;
    }

    @Override
    protected int getSeedModifier() {
        return SEED_MODIFIER;
    }

    @Override
    public IStartFactory getStartFactory() {
        return StoneCircleStructure.Start::new;
    }

    public static class Start extends StructureStart {
        public Start(Structure<?> structure, int chunkX, int chunkZ, MutableBoundingBox bb, int ref, long seed) {
            super(structure, chunkX, chunkZ, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> gen, TemplateManager templates, int chunkX, int chunkZ, Biome biomeIn) {
            BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
            components.add(new StoneCirclePiece(this.rand, pos));

            if (rand.nextFloat() < Vaults.vaultChance && Vaults.isValidPosition(pos))
                generateVaults(gen, templates, pos);

            this.recalculateStructureSize();
        }

        private void generateVaults(ChunkGenerator<?> gen, TemplateManager templates, BlockPos pos) {
            final String dir = Vaults.isValidPosition(pos) ? Vaults.VAULTS_DIR : Vaults.VAULTS_LOCAL;
            final ResourceLocation start = new ResourceLocation(Strange.MOD_ID, dir + "/starts");

            JigsawManager.addPieces(start, Vaults.size, VaultPiece::new, gen, templates, pos, components, rand);
            this.recalculateStructureSize();

            int top = bounds.maxY;

            // move components into the earth
            if (top >= Vaults.generateBelow) {
                int shift = bounds.maxY - Vaults.generateBelow;
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
