package svenhjol.strange.runestones.structure;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.strange.Strange;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.runestones.module.StoneCircles;

import java.util.Random;

public class StoneCircleStructure extends ScatteredStructure<NoFeatureConfig>
{
    public static final int SEED_MODIFIER = 247474720;
    public static final int SIZE = 8;

    public StoneCircleStructure()
    {
        super(config -> NoFeatureConfig.NO_FEATURE_CONFIG);
        setRegistryName(Strange.MOD_ID, StoneCircles.NAME);
    }

    @Override
    public String getStructureName()
    {
        return getRegistryName().toString();
    }

    @Override
    public int getSize()
    {
        return 1;
    }

    @Override
    public boolean hasStartAt(ChunkGenerator<?> gen, Random rand, int x, int z)
    {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            Biome biome = gen.getBiomeProvider().getBiome(new BlockPos((x << 4) + 9, 0, (z << 4) + 9));
            return gen.hasStructure(biome, StoneCircles.structure);
        }

        return false;
    }

    @Override
    protected int getSeedModifier()
    {
        return SEED_MODIFIER;
    }

    @Override
    public IStartFactory getStartFactory()
    {
        return StoneCircleStructure.Start::new;
    }

    public static class Start extends StructureStart
    {
        public Start(Structure<?> structure, int chunkX, int chunkZ, Biome biome, MutableBoundingBox bb, int ref, long seed)
        {
            super(structure, chunkX, chunkZ, biome, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> gen, TemplateManager templates, int chunkX, int chunkZ, Biome biomeIn)
        {
            BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);

            // add the stone circle
            components.add(new StoneCirclePiece(this.rand, pos));
            this.recalculateStructureSize();

            // create vaults beneath the circle
            if (isVaultValid(pos)
                && gen instanceof OverworldChunkGenerator
                && this.rand.nextFloat() < 1.0F) {

                ResourceLocation start = new ResourceLocation(Strange.MOD_ID, StoneCircles.VAULTS_DIR + "/starts");
                JigsawManager.func_214889_a(start, SIZE, VaultPiece::new, gen, templates, pos, components, rand);
                this.recalculateStructureSize();

                // move components into the earth
                int maxTop = 54;
                if (bounds.maxY >= maxTop) {
                    int shift = 5 + (bounds.maxY - maxTop);
                    bounds.offset(0, -shift, 0);
                    components.forEach(p -> p.offset(0, -shift, 0));
                }

                if (bounds.minY < 6) {
                    int shift = 6 - bounds.minY;
                    bounds.offset(0, shift, 0);
                    components.forEach(p -> p.offset(0, shift, 0));
//                  Meson.debug("[UndergroundRuinStructure] Shifting up by " + shift + " at " + pos);
                }
            }
        }
    }

    private static boolean isVaultValid(BlockPos pos)
    {
        if (!Strange.hasModule(Outerlands.class) || !StoneCircles.outerOnly) return true;
        return Strange.hasModule(Outerlands.class) && Outerlands.isOuterPos(pos);
    }
}
