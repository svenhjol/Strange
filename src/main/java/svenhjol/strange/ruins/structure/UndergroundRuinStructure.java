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
import svenhjol.strange.Strange;
import svenhjol.strange.ruins.module.UndergroundRuins;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class UndergroundRuinStructure extends ScatteredStructure<UndergroundRuinConfig>
{
    public static final int SEED_MODIFIER = 135318;
    public static final String GENERAL = "general";
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
        return 6;
    }

    @Override
    protected int getBiomeFeatureSeparation(ChunkGenerator<?> gen)
    {
        return 5;
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
                || !UndergroundRuins.biomeRuins.containsKey(biomeCategory)
                || UndergroundRuins.biomeRuins.get(biomeCategory).isEmpty();

            if (useOverworld) {
                if (!(gen instanceof OverworldChunkGenerator)) return; // don't generate overworld structures in non-overworld dims
                biomeCategory = Biome.Category.NONE; // chance of being a general overworld structure
            }

            if (UndergroundRuins.biomeRuins.containsKey(biomeCategory) && !UndergroundRuins.biomeRuins.get(biomeCategory).isEmpty()) {
                ResourceLocation start = getStart(biomeCategory, rand);
                if (start == null) return;
                JigsawManager.func_214889_a(start, 5, Piece::new, gen, templates, pos, components, rand);
                this.recalculateStructureSize();

                int maxTop = 60;
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

        @Nullable
        public ResourceLocation getStart(Biome.Category cat, Random rand)
        {
            String catName = cat.toString().toLowerCase();
            List<String> subcats = UndergroundRuins.biomeRuins.get(cat);
            if (subcats.size() == 0) return null;

            String subcat = subcats.get(rand.nextInt(subcats.size()));
            return new ResourceLocation(Strange.MOD_ID, "underground_ruins/" + catName + "/" + subcat + "/rooms");
        }

        public static class Piece extends AbstractVillagePiece
        {
            protected TemplateManager templates;

            public Piece(TemplateManager templates, JigsawPiece piece, BlockPos pos, int groundLevelDelta, Rotation rotation, MutableBoundingBox bounds) {
                super(UNDERGROUND_RUIN_PIECE, templates, piece, pos, groundLevelDelta, rotation, bounds);
                this.templates = templates;
            }

            public Piece(TemplateManager templates, CompoundNBT nbt) {
                super(templates, nbt, UNDERGROUND_RUIN_PIECE);
                this.templates = templates;
            }

            @Override
            public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox structureBox, ChunkPos chunk)
            {
                MutableBoundingBox templateBox = this.jigsawPiece.getBoundingBox(this.templates, this.pos, this.rotation);
                BlockPos checkPos = new BlockPos(templateBox.minX, templateBox.minY, templateBox.minZ).down();
                BlockState checkState = world.getBlockState(checkPos);

                int i = templateBox.minY;

                while (--i > 12 && !checkState.isSolid() || world.isAirBlock(checkPos) || checkState.getMaterial().isLiquid()) {
//                    world.setBlockState(checkPos, Blocks.GLOWSTONE.getDefaultState(), 0); // for testing
                    checkPos = checkPos.down();
                    checkState = world.getBlockState(checkPos);

                    if (checkState.isSolid()) {
                        templateBox.offset(0, -(templateBox.minY - i), 0);
                        this.pos = new BlockPos(this.pos.getX(), i - 1, this.pos.getZ());
                        break;
                    }
                }

                return super.addComponentParts(world, rand, structureBox, chunk);
            }
        }
    }
}
