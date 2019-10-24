package svenhjol.strange.stonecircles.structure;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.runestones.module.Runestones;
import svenhjol.strange.stonecircles.module.StoneCircles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class StoneCircleStructure extends ScatteredStructure<StoneCircleConfig>
{
    public static final int SEED_MODIFIER = 247474720;
    public static final int TRIES = 64;
    public static final String STRUCTURE_NAME = "Stone_Circle";
    public static IStructurePieceType SCP = StoneCirclePiece::new;

    public StoneCircleStructure(Function<Dynamic<?>, ? extends StoneCircleConfig> config)
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
    public boolean hasStartAt(ChunkGenerator<?> gen, Random rand, int x, int z)
    {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            int px = x >> 4;
            int pz = z >> 4;

            rand.setSeed((long)(px ^ pz << 4) ^ gen.getSeed());
            rand.nextInt();

            if (rand.nextInt(2) > 0) return false;

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
        public void init(ChunkGenerator<?> generator, TemplateManager templates, int chunkX, int chunkZ, Biome biomeIn)
        {
            BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);

            // add the stone circle
            components.add(new StoneCirclePiece(this.rand, pos));

            // create vaults beneath the circle
            if (isValidPosition(pos)
                && generator instanceof OverworldChunkGenerator
                && this.rand.nextFloat() < StoneCircles.vaultChance) {
                VaultStructure vaults = new VaultStructure(templates, components, biomeIn, rand);
                vaults.generate(pos);
            }

            this.recalculateStructureSize();
        }
    }

    private static boolean isValidPosition(BlockPos pos)
    {
        if (!Strange.loader.hasModule(Outerlands.class) || !StoneCircles.outerOnly) return true;
        return Strange.loader.hasModule(Outerlands.class) && Outerlands.isOuterPos(pos);
    }

    public static class StoneCirclePiece extends ScatteredStructurePiece
    {
        public StoneCirclePiece(Random rand, BlockPos pos)
        {
            super(SCP, rand, pos.getX(), 64, pos.getZ(), 16, 6, 16);
        }

        public StoneCirclePiece(TemplateManager templateManager, CompoundNBT tag)
        {
            super(SCP, tag);
        }

        @Override
        public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox bb, ChunkPos chunkPos)
        {
            int y = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, this.boundingBox.minX, this.boundingBox.minZ);
            MutableBlockPos pos;
            BlockPos surfacePos, surfacePosDown;
            GenerationConfig config = new GenerationConfig();

            if (world.getDimension().getType() == DimensionType.THE_NETHER) {

                config.runeChance = 0.75F;
                config.withChest = true;
                config.radius = rand.nextInt(4) + 5;
                config.columnMinHeight = 3;
                config.columnVariation = 4;
                config.blocks = new ArrayList<>(Arrays.asList(
                    Blocks.NETHER_BRICKS.getDefaultState(),
                    Blocks.RED_NETHER_BRICKS.getDefaultState()
                ));

                for (int i = 20; i < 100; i++) {
                    for (int ii = 1; ii < TRIES; ii++) {
                        pos = new MutableBlockPos(this.boundingBox.minX, i, this.boundingBox.minZ);
                        surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                        surfacePosDown = surfacePos.down();

                        if (world.isAirBlock(surfacePos) && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.NETHERRACK)) {
                            return generateCircle(world, pos, rand, config);
                        }
                    }
                }

            } else if (world.getDimension().getType() == DimensionType.THE_END) {

                config.runeChance = 0.9F;
                config.withChest = true;
                config.radius = rand.nextInt(7) + 4;
                config.columnMinHeight = 3;
                config.columnVariation = 3;
                config.blocks = new ArrayList<>(Arrays.asList(
                    Blocks.OBSIDIAN.getDefaultState()
                ));

                for (int ii = 1; ii < TRIES; ii++) {
                    pos = new MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
                    surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                    surfacePosDown = surfacePos.down();

                    if (world.isAirBlock(surfacePos) && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.END_STONE)) {
                        return generateCircle(world, pos, rand, config);
                    }
                }

            } else {

                config.runeChance = 0.6F;
                config.radius = rand.nextInt(6) + 5;
                config.columnMinHeight = 2;
                config.columnVariation = 3;
                config.blocks = new ArrayList<>(Arrays.asList(
                    Blocks.STONE.getDefaultState(),
                    Blocks.COBBLESTONE.getDefaultState(),
                    Blocks.MOSSY_COBBLESTONE.getDefaultState()
                ));

                for (int ii = 1; ii < TRIES; ii++) {
                    pos = new MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
                    surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                    surfacePosDown = surfacePos.down();

                    if ((world.isAirBlock(surfacePos) || world.hasWater(surfacePos))
                        && world.getBlockState(surfacePosDown).isSolid() && world.isSkyLightMax(surfacePosDown)
                    ) {
                        return generateCircle(world, pos, rand, config);
                    }
                }
            }

            return false;
        }

        public boolean generateCircle(IWorld world, MutableBlockPos pos, Random rand, GenerationConfig config)
        {
            boolean generated = false;
            boolean generatedWithRune = false;
            boolean runestonesEnabled = Strange.loader.hasModule(Runestones.class);

            List<Integer> availableRunes = new ArrayList<>();
            if (runestonesEnabled) {
                for (int i = 0; i < Runestones.dests.size(); i++) {
                    availableRunes.add(i);
                }
            }

            if (config.blocks.isEmpty()) {
                Meson.warn("You must pass blockstates to generate a circle");
                return false;
            }

            for (int i = 0; i < 360; i += 45)
            {
                double x1 = config.radius * Math.cos(i * Math.PI / 180);
                double z1 = config.radius * Math.sin(i * Math.PI / 180);

                for (int k = 5; k > -15; k--) {
                    BlockPos findPos = pos.add(x1, k, z1);
                    BlockPos findPosUp = findPos.up();
                    BlockState findState = world.getBlockState(findPos);
                    BlockState findStateUp = world.getBlockState(findPosUp);

                    if ((findState.isSolid() || findState.getBlock() == Blocks.LAVA)
                        && (findStateUp.isAir(world, findPosUp) || world.hasWater(findPosUp))
                    ) {
                        boolean madeColumn = false;

                        int maxHeight = rand.nextInt(config.columnVariation + 1) + config.columnMinHeight;
                        world.setBlockState(findPos, config.blocks.get(0), 2);

                        for (int l = 1; l < maxHeight; l++) {
                            float f = rand.nextFloat();
                            BlockState state = config.blocks.get(rand.nextInt(config.blocks.size()));

                            if (runestonesEnabled && l == maxHeight - 1 && f < config.runeChance) {
                                int index = rand.nextInt(availableRunes.size());
                                int rune = availableRunes.get(index);

                                if (rand.nextFloat() < Runestones.dests.get(index).weight) {
                                    availableRunes.remove(index);
                                    state = Runestones.getRunestoneBlock(world, rune);
                                    generatedWithRune = true;
                                }
                            }

                            world.setBlockState(findPos.up(l), state, 2);
                            madeColumn = true;
                        }

                        if (madeColumn) {
                            generated = true;
                            break;
                        }
                    }
                }
            }

            for (int k = 5; k > -15; k--) {
                BlockPos findPos = pos.add(0, k, 0);
                BlockPos findPosUp = findPos.up();
                BlockState findState = world.getBlockState(findPos);
                BlockState findStateUp = world.getBlockState(findPosUp);

                if (findState.isSolid() && findStateUp.isAir(world, findPosUp)) {
                    BlockState chest = Blocks.CHEST.getDefaultState();
                    world.setBlockState(findPos, chest, 2);
                    LockableLootTileEntity.setLootTable(world, rand, findPos, StrangeLoot.CHESTS_VAULT_TREASURE);
                    Meson.debug("Generated with chest " + pos);
                    break;
                }
            }

            if (generatedWithRune) {
                Meson.debug("Generated with rune " + pos);
            }

            return generated;
        }
    }

    public static class GenerationConfig
    {
        public float runeChance = 0.5F;
        public int radius = 4;
        public int columnMinHeight = 3;
        public int columnVariation = 3;
        public boolean withChest = false;
        public List<BlockState> blocks = new ArrayList<>();
    }
}
