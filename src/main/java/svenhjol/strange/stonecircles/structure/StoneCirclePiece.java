package svenhjol.strange.stonecircles.structure;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.ScatteredStructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.storage.loot.LootTables;
import svenhjol.meson.Meson;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.runestones.module.Runestones;

import java.util.*;

public class StoneCirclePiece extends ScatteredStructurePiece {
    public static final IStructurePieceType PIECE = StoneCirclePiece::new;
    public static final int TRIES = 64;

    public StoneCirclePiece(Random rand, BlockPos pos) {
        super(PIECE, rand, pos.getX(), 64, pos.getZ(), 16, 6, 16);
    }

    public StoneCirclePiece(TemplateManager templateManager, CompoundNBT tag) {
        super(PIECE, tag);
    }

    @Override
    public boolean create(IWorld world, ChunkGenerator<?> gen, Random rand, MutableBoundingBox bb, ChunkPos chunkPos) {
        BlockPos foundPos = null;
        DimensionType dim = world.getDimension().getType();
        GenerationConfig config = new GenerationConfig();
        int x = this.boundingBox.minX;
        int z = this.boundingBox.minZ;
        int y = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, x, z);

//        if (world instanceof ServerWorld) {
//            Biome biome = BiomeHelper.getBiomeAtPos((ServerWorld)world, new BlockPos((chunkPos.getXStart() << 4) + 9, 0, (chunkPos.getZStart() << 4) + 9));
//        }

        config.radius = rand.nextInt(6) + 5;
        config.runeTries = 2;
        config.runeChance = 0.8F;
        config.columnMinHeight = 4;
        config.columnVariation = 2;
        config.lootTable = LootTables.CHESTS_PILLAGER_OUTPOST;
        config.blocks = new ArrayList<>(Arrays.asList(
            Blocks.STONE.getDefaultState(),
            Blocks.COBBLESTONE.getDefaultState(),
            Blocks.MOSSY_COBBLESTONE.getDefaultState()
        ));

        for (int ii = 1; ii < TRIES; ii++) {
            BlockPos.Mutable pos = new BlockPos.Mutable(x, y, z);
            BlockPos surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
            BlockPos surfacePosDown = surfacePos.down();

            if ((world.isAirBlock(surfacePos) || world.hasWater(surfacePos))
                && world.getBlockState(surfacePosDown).isSolid()
                && WorldHelper.canSeeSky(world, surfacePosDown)
            ) {
                foundPos = surfacePos;
                break;
            }
        }

        if (foundPos != null) {
            config.withChest = Outerlands.isOuterPos(foundPos);
            return generateCircle(world, new BlockPos.Mutable(foundPos), rand, config);
        }

        return false;
    }

    public boolean generateCircle(IWorld world, Mutable pos, Random rand, GenerationConfig config) {
        boolean generated = false;
        boolean generatedWithRune = false;
        boolean runestonesEnabled = Meson.isModuleEnabled("strange:runestones");

        Map<Integer, Float> availableRunes = new HashMap<>();
        if (runestonesEnabled) {
            for (int i = 0; i < Runestones.destinations.size(); i++) {
                availableRunes.put(i, Runestones.destinations.get(i).weight);
            }

            if (availableRunes.size() == 0) {
                Strange.LOG.warn("No available runes to generate");
                return false;
            }
        }

        if (config.blocks.isEmpty()) {
            Strange.LOG.warn("You must pass blockstates to generate a circle");
            return false;
        }

        for (int i = 0; i < 360; i += 45) {
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
                        BlockState state = config.blocks.get(rand.nextInt(config.blocks.size()));

                        if (runestonesEnabled && l == maxHeight - 1 && rand.nextFloat() < config.runeChance) {
                            for (int t = 0; t < config.runeTries; t++) {
                                List<Integer> keys = new ArrayList<>(availableRunes.keySet());
                                int rune = keys.get(rand.nextInt(keys.size()));

                                float f = rand.nextFloat();
                                float weight = availableRunes.get(rune);

                                if (f < weight) {
                                    availableRunes.remove(rune);
                                    state = Runestones.getRunestoneBlock(rune);
                                    generatedWithRune = true;
                                    break;
                                }
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

        if (config.withChest) {
            for (int k = 5; k > -15; k--) {
                BlockPos findPos = pos.add(0, k, 0);
                BlockPos findPosUp = findPos.up();
                BlockState findState = world.getBlockState(findPos);
                BlockState findStateUp = world.getBlockState(findPosUp);

                if (findState.isSolid() && findStateUp.isAir(world, findPosUp) && config.lootTable != null) {
                    BlockState chest = Blocks.CHEST.getDefaultState();
                    world.setBlockState(findPosUp, chest, 2);
                    LockableLootTileEntity.setLootTable(world, rand, findPosUp, config.lootTable);
                    Strange.LOG.debug("Generated with chest " + pos);
                    break;
                }
            }
        }

        if (generatedWithRune)
            Strange.LOG.debug("Generated with rune " + pos);

        if (!generated)
            Strange.LOG.debug("Did not generate");

        return generated;
    }

    public static class GenerationConfig {
        public int radius = 4;
        public int columnMinHeight = 3;
        public int columnVariation = 3;
        public int runeTries = 1;
        public float runeChance = 0.8F;
        public boolean withChest = false;
        public ResourceLocation lootTable = null;
        public List<BlockState> blocks = new ArrayList<>();
    }
}