package svenhjol.strange.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePieceWithDimensions;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import svenhjol.charm.Charm;
import svenhjol.charm.base.enums.IVariantMaterial;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.DecorationHelper;
import svenhjol.charm.module.VariantChests;
import svenhjol.strange.module.Excavation;
import svenhjol.strange.module.Runestones;
import svenhjol.strange.module.StoneCircles;

import java.util.*;

public class StoneCircleGenerator extends StructurePieceWithDimensions {
    public static float chestChance = 0.3F;
    public static float rubbleChance = 0.1F;
    public static float runeChance = 0.8F;
    public static int maxCheckSurface = 5;
    public static int minCheckSurface = -15;
    public static int maxRadius = 12;
    public static int minRadius = 5;
    public static int maxHeight = 8;
    public static int minHeight = 4;
    public static int runeTries = 10;

    public StoneCircleGenerator(Random random, BlockPos pos) {
        super(StoneCircles.STONE_CIRCLE_PIECE, random, pos.getX(), 64, pos.getZ(), 16, 8, 16);
    }

    public StoneCircleGenerator(StructureManager structureManager, CompoundTag tag) {
        super(StoneCircles.STONE_CIRCLE_PIECE, tag);
    }

    @Override
    public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator gen, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        int radius = random.nextInt(maxRadius - minRadius) + minRadius;

        // TODO: dedicated loot table here
        Identifier lootTable = LootTables.SIMPLE_DUNGEON_CHEST;

        List<BlockState> blocks = new ArrayList<>(Arrays.asList(
            Blocks.STONE.getDefaultState(),
            Blocks.COBBLESTONE.getDefaultState(),
            Blocks.MOSSY_COBBLESTONE.getDefaultState()
        ));

        // generate the circle
        boolean generatedSomething = false;
        Map<Integer, Float> availableRunes = new HashMap<>();

        for (int i = 0; i < Runestones.availableDestinations.size(); i++) {
            availableRunes.put(i, Runestones.availableDestinations.get(i).getWeight());
        }

        if (availableRunes.size() == 0) {
            Charm.LOG.warn("No available runes to generate");
            return false;
        }

        for (int i = 0; i < 360; i += 45) {
            double x = radius * Math.cos(i * Math.PI / 180);
            double z = radius * Math.sin(i * Math.PI / 180);

            for (int s = maxCheckSurface; s > minCheckSurface; s--) {
                BlockPos checkPos = blockPos.add(x, s, z);
                BlockPos checkUpPos = checkPos.up();
                BlockState checkState = world.getBlockState(checkPos);
                BlockState checkUpState = world.getBlockState(checkUpPos);

                boolean validSurfacePos = ((checkState.isOpaque() || checkState.getBlock() == Blocks.LAVA)
                    && (checkUpState.isAir() || world.isWater(checkUpPos)));

                if (!validSurfacePos)
                    continue;

                boolean generatedColumn = false;
                int height = random.nextInt(maxHeight - minHeight) + minHeight;
                world.setBlockState(checkPos, blocks.get(0), 2);

                for (int y = 1; y < height; y++) {
                    BlockState state = blocks.get(random.nextInt(blocks.size()));

                    boolean isTop = y == height - 1;
                    if (isTop && random.nextFloat() < runeChance) {

                        // Try and generate a rune. Replace the state with the runestone if successful
                        for (int tries = 0; tries < runeTries; tries++) {
                            List<Integer> keys = new ArrayList<>(availableRunes.keySet());
                            int rune = keys.get(random.nextInt(keys.size()));

                            float f = random.nextFloat();
                            float weight = availableRunes.get(rune);

                            if (f >= weight)
                                continue;

                            availableRunes.remove(rune);
                            state = Runestones.RUNESTONE_BLOCKS.get(rune).getDefaultState();
                            break;
                        }
                    }

                    world.setBlockState(checkPos.up(y), state, 2);
                    generatedColumn = true;
                }

                if (generatedColumn) {
                    generatedSomething = true;
                    break;
                }
            }
        }

        // generate chest at center
        if (random.nextFloat() < chestChance) {
            boolean useVariantChests = ModuleHandler.enabled("charm:variant_chests");
            for (int s = maxCheckSurface; s > minCheckSurface; s--) {
                BlockPos checkPos = blockPos.add(0, s, 0);
                BlockPos checkUpPos = checkPos.up();
                BlockState checkState = world.getBlockState(checkPos);
                BlockState checkUpState = world.getBlockState(checkUpPos);

                if (checkState.isOpaque() && checkUpState.isAir() && lootTable != null) {
                    BlockState chest;
                    if (useVariantChests) {
                        IVariantMaterial material = DecorationHelper.getRandomVariantMaterial(random);
                        chest = VariantChests.NORMAL_CHEST_BLOCKS.get(material).getDefaultState();
                    } else {
                        chest = Blocks.CHEST.getDefaultState();
                    }

                    world.setBlockState(checkUpPos, chest, 2);
                    LootableContainerBlockEntity.setLootTable(world, random, checkUpPos, lootTable);
                }
            }
        }

        // try and place some ancient rubble
        if (random.nextFloat() < rubbleChance && ModuleHandler.enabled("strange:excavation")) {
            int maxTries = 1 + random.nextInt(3);
            for (int tries = 0; tries < maxTries; tries++) {
                for (int s = maxCheckSurface; s > minCheckSurface; s--) {
                    BlockPos checkPos = blockPos.add(random.nextInt(8) - 8, s, random.nextInt(8) - 8);
                    BlockPos checkUpPos = checkPos.up();
                    BlockState checkState = world.getBlockState(checkPos);
                    BlockState checkUpState = world.getBlockState(checkUpPos);

                    if (checkState.isOpaque() && checkUpState.isAir() && lootTable != null)
                        world.setBlockState(checkPos, Excavation.ANCIENT_RUBBLE.getDefaultState(), 2);
                }
            }
        }

        if (!generatedSomething)
            Charm.LOG.debug("Did not generate a stone circle at: " + blockPos);

        return generatedSomething;
    }
}
