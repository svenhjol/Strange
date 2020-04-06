package svenhjol.strange.runestones.structure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.module.Runestones;
import svenhjol.strange.runestones.module.StoneCircles;

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
        return StoneCircles.distance;
    }

    @Override
    protected int getBiomeFeatureSeparation(ChunkGenerator<?> gen) {
        return StoneCircles.distance / 2;
    }

    @Override
    public boolean hasStartAt(ChunkGenerator<?> gen, Random rand, int x, int z) {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            BlockPos pos = new BlockPos((x << 4) + 9, 0, (z << 4) + 9);
            Biome biome = gen.getBiomeProvider().getBiome(pos);

            return StoneCircles.validBiomes.contains(biome)
                && Meson.isModuleEnabled("strange:stone_circles")
                && !Runestones.allDests.isEmpty()
                && gen.hasStructure(biome, StoneCircles.structure)
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
        public Start(Structure<?> structure, int chunkX, int chunkZ, Biome biome, MutableBoundingBox bb, int ref, long seed) {
            super(structure, chunkX, chunkZ, biome, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> gen, TemplateManager templates, int chunkX, int chunkZ, Biome biomeIn) {
            BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
            components.add(new StoneCirclePiece(this.rand, pos));
            this.recalculateStructureSize();
        }
    }
}
