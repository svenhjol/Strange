package svenhjol.strange.ruins.structure;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.ruins.module.UndergroundRuins;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UndergroundRuinStructure extends ScatteredStructure<UndergroundRuinConfig>
{
    public static final int SEED_MODIFIER = 135318;
    public static final String STRUCTURE_NAME = "Underground_Ruin";
    public static IStructurePieceType UNDERGROUND_RUIN_PIECE = Start.Piece::new;

    public UndergroundRuinStructure(Function<Dynamic<?>, ? extends UndergroundRuinConfig> config)
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

            // TODO don't spawn underground ruin near blacklisted structure
            return gen.hasStructure(biome, UndergroundRuins.structure);
        }
        return false;
    }

    @Override
    protected int getBiomeFeatureDistance(ChunkGenerator<?> gen)
    {
        return 5;
    }

    @Override
    protected int getBiomeFeatureSeparation(ChunkGenerator<?> gen)
    {
        return 4;
    }

    @Override
    public IStartFactory getStartFactory()
    {
        return UndergroundRuinStructure.Start::new;
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
            Biome.Category biomeCategory = biome.getCategory();
            BlockPos pos = new BlockPos(chunkX * 16,  rand.nextInt(16) + 42, chunkZ * 16);
            if (pos.getY() == 0 || pos.getY() > 48) return;

            boolean useOverworld = rand.nextFloat() < 0.1F
                || !UndergroundRuins.ruinBiomes.containsKey(biomeCategory)
                || UndergroundRuins.ruinBiomes.get(biomeCategory).isEmpty();

            if (useOverworld) {
                if (!(gen instanceof OverworldChunkGenerator)) return; // don't generate overworld structures in non-overworld dims
                biomeCategory = Biome.Category.NONE; // chance of being a general overworld structure
            }

            if (UndergroundRuins.ruinBiomes.containsKey(biomeCategory) && !UndergroundRuins.ruinBiomes.get(biomeCategory).isEmpty()) {
                List<String> ruins = UndergroundRuins.ruinBiomes.get(biomeCategory);
                if (ruins.size() == 0) return;

                // get random ruin and its preferred size for this biome
                Random ruinRand = new Random(pos.toLong());
                String ruin = ruins.get(rand.nextInt(ruins.size()));

                int size = 2;
                if (UndergroundRuins.ruinBiomeSizes.containsKey(biomeCategory))
                    size = UndergroundRuins.ruinBiomeSizes.get(biomeCategory).getOrDefault(ruin, 2) + ruinRand.nextInt(2);

                ResourceLocation start = new ResourceLocation(Strange.MOD_ID, UndergroundRuins.DIR + "/" + biomeCategory.toString().toLowerCase() + "/" + ruin + "/starts");
                JigsawManager.func_214889_a(start, size, Piece::new, gen, templates, pos, components, ruinRand);
                this.recalculateStructureSize();

                int maxTop = 50;
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
            }
        }

        public static class Piece extends AbstractVillagePiece
        {
            protected TemplateManager templates;

            public Piece(TemplateManager templates, JigsawPiece piece, BlockPos pos, int groundLevelDelta, Rotation rotation, MutableBoundingBox bounds) {
                super(UNDERGROUND_RUIN_PIECE, templates, piece, pos, groundLevelDelta, rotation, bounds);
                this.templates = templates;
            }

            public Piece(TemplateManager templates, CompoundNBT nbt)
            {
                super(templates, nbt, UNDERGROUND_RUIN_PIECE);
                this.templates = templates;
            }

            @Override
            public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox structureBox, ChunkPos chunk)
            {
                if (WorldHelper.getBiomeAtPos(world.getWorld(), this.pos).getCategory() == Biome.Category.OCEAN) {

                    MutableBoundingBox templateBox = this.jigsawPiece.getBoundingBox(this.templates, this.pos, this.rotation);
                    BlockPos checkPos = new BlockPos(templateBox.minX, templateBox.minY, templateBox.minZ).down();
                    BlockState checkState = world.getBlockState(checkPos);
                    int i = templateBox.minY;
                    int m = 0;

                    while (--i > 12 && (!checkState.isSolid() || world.isAirBlock(checkPos) || checkState.getMaterial().isLiquid())) {
//                    world.setBlockState(checkPos, Blocks.GLOWSTONE.getDefaultState(), 0); // for testing
                        checkPos = checkPos.down();
                        checkState = world.getBlockState(checkPos);

                        if (checkState.isSolid()) {
                            templateBox.offset(0, -(templateBox.minY - i), 0);
                            this.pos = new BlockPos(this.pos.getX(), i - 1, this.pos.getZ());
                            break;
                        }

                        if (++m > 100) {
                            Meson.debug("ARGH WHAT");
                            break;
                        }
                    }
                }

                return super.addComponentParts(world, rand, structureBox, chunk);
            }
        }
    }

    public static int getWeight(String prefix, String name, int def)
    {
        if (name.contains(prefix)) {
            Pattern p = Pattern.compile(prefix + "(\\d+)");
            Matcher m = p.matcher(name);
            if (m.find()) return Integer.parseInt(m.group(1));
        }
        return def;
    }
}
