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
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.ruins.module.UndergroundRuins;

import javax.annotation.Nullable;
import java.util.*;

public class UndergroundStructure extends ScatteredStructure<NoFeatureConfig> {
    public static final int SEED_MODIFIER = 135318;

    public UndergroundStructure() {
        super(config -> NoFeatureConfig.NO_FEATURE_CONFIG);
        setRegistryName(Strange.MOD_ID, UndergroundRuins.NAME);
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
    protected int getSeedModifier() {
        return SEED_MODIFIER;
    }

    @Override
    public boolean canBeGenerated(BiomeManager biomes, ChunkGenerator<?> gen, Random rand, int x, int z, Biome biome) {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            if (gen.hasStructure(biome, UndergroundRuins.structure)) {
                for (int k = x - 10; k <= x + 10; ++k) {
                    for (int l = z - 10; l <= z + 10; ++l) {
                        for (Structure<?> structure : UndergroundRuins.blacklist) {
                            if (structure.canBeGenerated(biomes, gen, rand, k, l, biome)) {
                                Strange.LOG.debug("[UndergroundStructure] too close to " + structure.getStructureName());
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
    protected int getBiomeFeatureDistance(ChunkGenerator<?> gen) {
        return UndergroundRuins.distance;
    }

    @Override
    protected int getBiomeFeatureSeparation(ChunkGenerator<?> gen) {
        return UndergroundRuins.distance / 2;
    }

    @Override
    public IStartFactory getStartFactory() {
        return UndergroundStructure.Start::new;
    }

    @SuppressWarnings("unused")
    public static class Start extends StructureStart {
        public Start(Structure<?> structure, int chunkX, int chunkZ, MutableBoundingBox bb, int ref, long seed) {
            super(structure, chunkX, chunkZ, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> gen, TemplateManager templates, int chunkX, int chunkZ, Biome biome) {
            BlockPos pos = new BlockPos(chunkX * 16, rand.nextInt(16) + 42, chunkZ * 16);
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

            Strange.LOG.debug("[UndergroundStructure] create ruin " + ruin + " at " + pos);
            JigsawManager.addPieces(start, size, UndergroundPiece::new, gen, templates, pos, components, rand);
            this.recalculateStructureSize();

            int maxTop = getMaxTopForBiome(biomeCategory, new Random(pos.toLong()));
            if (bounds.maxY >= maxTop) {
                int shift = 5 + (bounds.maxY - maxTop);
                bounds.offset(0, -shift, 0);
                components.forEach(p -> p.offset(0, -shift, 0));
            }

            if (bounds.minY < 6) {
                int shift = 6 - bounds.minY;
                bounds.offset(0, shift, 0);
                components.forEach(p -> p.offset(0, shift, 0));
            }

            if (UndergroundRuins.addMarker && this.rand.nextFloat() < (float)UndergroundRuins.markerChance)
                components.add(new MarkerPiece(this.rand, pos));
        }

        public Biome.Category getBiomeCategory(Biome biome, ChunkGenerator<?> gen) {
            Biome.Category biomeCategory = biome.getCategory();

            if (gen instanceof OverworldChunkGenerator
                && ((biomeCategory != Biome.Category.OCEAN && rand.nextFloat() < 0.1F)
                || !UndergroundRuins.ruins.containsKey(biomeCategory)
                || UndergroundRuins.ruins.get(biomeCategory).isEmpty())
            ) {
                biomeCategory = Biome.Category.NONE; // chance of being a general overworld structure
            }

            return biomeCategory;
        }

        @Nullable
        public String getRuin(Biome.Category biomeCategory, BlockPos pos) {
            if (UndergroundRuins.ruins.size() == 0) return null;
            if (UndergroundRuins.ruins.get(biomeCategory) == null || UndergroundRuins.ruins.get(biomeCategory).size() == 0)
                return null;

            List<String> ruins = new ArrayList<>(UndergroundRuins.ruins.get(biomeCategory));
            Collections.shuffle(ruins, rand);

            if (Meson.isModuleEnabled("strange:outerlands") && Outerlands.isOuterPos(pos)) {
                return ruins.stream().filter(r -> r.contains("outerlands")).findFirst().orElse(ruins.get(0));
            } else {
                return ruins.get(0);
            }
        }

        public BlockPos adjustPosForBiome(Biome.Category biomeCategory, BlockPos pos, Random rand) {
            if (biomeCategory == Biome.Category.OCEAN) {
                return new BlockPos(pos.getX(), Math.min(pos.getY(), 20), pos.getZ());
            }
            if (biomeCategory == Biome.Category.NETHER) {
                return new BlockPos(pos.getX(), Math.min(pos.getY(), 20), pos.getZ());
            }
            return pos;
        }

        public int getMaxTopForBiome(Biome.Category biomeCategory, Random rand) {
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
