package svenhjol.strange.ruins.structure;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.ruins.module.UndergroundRuins;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class UndergroundStructure extends ScatteredStructure<UndergroundConfig>
{
    public static final int SEED_MODIFIER = 135318;
    public static final String STRUCTURE_NAME = "Underground_Ruin";

    public UndergroundStructure(Function<Dynamic<?>, ? extends UndergroundConfig> config)
    {
        super(config);
    }

    @Override
    public String getStructureName()
    {
        return STRUCTURE_NAME;
    }

    @Override
    public int getSize()
    {
        return 1;
    }

    @Override
    protected int getSeedModifier()
    {
        return SEED_MODIFIER;
    }

    @Override
    public boolean hasStartAt(ChunkGenerator<?> gen, Random rand, int x, int z)
    {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            Biome biome = gen.getBiomeProvider().getBiome(new BlockPos((x << 4) + 9, 0, (z << 4) + 9));

            // TEST don't spawn underground ruin near blacklisted structure
            if (gen.hasStructure(biome, UndergroundRuins.structure)) {
                for (int k = x - 10; k <= x + 10; ++k) {
                    for (int l = z - 10; l <= z + 10; ++l) {
                        for (Structure<?> structure : UndergroundRuins.blacklist) {
                            if (structure.hasStartAt(gen, rand, k, l)) {
                                Meson.debug("[UndergroundStructure] too close to " + structure.getStructureName());
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getBiomeFeatureDistance(ChunkGenerator<?> gen)
    {
        return UndergroundRuins.distance;
    }

    @Override
    protected int getBiomeFeatureSeparation(ChunkGenerator<?> gen)
    {
        return UndergroundRuins.distance / 2;
    }

    @Override
    public IStartFactory getStartFactory()
    {
        return UndergroundStructure.Start::new;
    }

    public static class Start extends StructureStart
    {
        public Start(Structure<?> structure, int chunkX, int chunkZ, Biome biome, MutableBoundingBox bb, int ref, long seed)
        {
            super(structure, chunkX, chunkZ, biome, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> gen, TemplateManager templates, int chunkX, int chunkZ, Biome biome)
        {
            BlockPos pos = new BlockPos(chunkX * 16,  rand.nextInt(16) + 42, chunkZ * 16);
            if (pos.getY() == 0 || pos.getY() > 48) return;

            Biome.Category biomeCategory = getBiomeCategory(biome, gen);
            String catName = biomeCategory.getName().toLowerCase();
            String ruin = getRuin(biomeCategory, pos);
            if (ruin == null) return;

            pos = adjustPosForBiome(biomeCategory, pos, new Random(pos.toLong()));

            int size = UndergroundRuins.defaultSize;
            if (UndergroundRuins.sizes.containsKey(biomeCategory))
                size = UndergroundRuins.sizes.get(biomeCategory).getOrDefault(ruin, UndergroundRuins.defaultSize);

            size += rand.nextInt(UndergroundRuins.variation + 1);
            ResourceLocation start = new ResourceLocation(Strange.MOD_ID, UndergroundRuins.DIR + "/" + catName + "/" + ruin + "/starts");

            Meson.debug("[UndergroundStructure] create ruin " + ruin + " at " + pos);
            JigsawManager.func_214889_a(start, size, UndergroundPiece::new, gen, templates, pos, components, rand);
            this.recalculateStructureSize();

            int maxTop = getMaxTopForBiome(biomeCategory, new Random(pos.toLong()));
            if (bounds.maxY >= maxTop) {
                int shift = 5 + (bounds.maxY - maxTop);
                bounds.offset(0, -shift, 0);
                components.forEach(p -> p.offset(0, -shift, 0));
//                Meson.debug("[UndergroundRuinStructure] Shifting down by " + shift + " at " + pos);
            }

            if (bounds.minY < 6) {
                int shift = 6 - bounds.minY;
                bounds.offset(0, shift, 0);
                components.forEach(p -> p.offset(0, shift, 0));
//                Meson.debug("[UndergroundRuinStructure] Shifting up by " + shift + " at " + pos);
            }
        }

        public Biome.Category getBiomeCategory(Biome biome, ChunkGenerator<?> gen)
        {
            Biome.Category biomeCategory = biome.getCategory();

            if ((gen instanceof OverworldChunkGenerator && rand.nextFloat() < 0.1F)
                || !UndergroundRuins.ruins.containsKey(biomeCategory)
                || UndergroundRuins.ruins.get(biomeCategory).isEmpty())
                biomeCategory = Biome.Category.NONE; // chance of being a general overworld structure

            return biomeCategory;
        }

        @Nullable
        public String getRuin(Biome.Category biomeCategory, BlockPos pos)
        {
            if (UndergroundRuins.ruins.size() == 0) return null;

            List<String> ruins = new ArrayList<>(UndergroundRuins.ruins.get(biomeCategory));
            Collections.shuffle(ruins, rand);

            if (Strange.loader.hasModule(Outerlands.class) && Outerlands.isOuterPos(pos)) {
                return ruins.stream().filter(r -> r.contains("outerlands")).findFirst().orElse(ruins.get(0));
            } else {
                return ruins.get(0);
            }
        }

        public BlockPos adjustPosForBiome(Biome.Category biomeCategory, BlockPos pos, Random rand)
        {
            if (biomeCategory == Biome.Category.OCEAN) {
                return new BlockPos(pos.getX(), Math.min(pos.getY(), 30), pos.getZ());
            }
            if (biomeCategory == Biome.Category.NETHER) {
                return new BlockPos(pos.getX(), Math.min(pos.getY(), 20), pos.getZ());
            }
            return pos;
        }

        public int getMaxTopForBiome(Biome.Category biomeCategory, Random rand)
        {
            if (biomeCategory == Biome.Category.NETHER) {
                return 32;
            }
            if (biomeCategory == Biome.Category.OCEAN) {
                return 32;
            }
            return 50;
        }
    }
}
