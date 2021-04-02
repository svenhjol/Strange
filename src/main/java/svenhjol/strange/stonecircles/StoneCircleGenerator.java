package svenhjol.strange.stonecircles;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePieceWithDimensions;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import svenhjol.charm.Charm;
import svenhjol.charm.base.enums.IVariantMaterial;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.DecorationHelper;
import svenhjol.charm.blockentity.EntitySpawnerBlockEntity;
import svenhjol.charm.module.EntitySpawners;
import svenhjol.charm.module.VariantChests;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.mobs.Mobs;
import svenhjol.strange.runestones.Runestones;

import java.util.*;

public class StoneCircleGenerator extends StructurePieceWithDimensions {
    public static int maxCheckSurface = 5;
    public static int minCheckSurface = -15;
    public static int maxRadius = 13;
    public static int minRadius = 5;
    public static int maxHeight = 8;
    public static int minHeight = 4;
    public static int runeTries = 10; // if the runeChance passes, this is the number of attempts at rune placement from available runes

    public StoneCircleGenerator(Random random, int x, int y, int z) {
        // TODO: these parameters seem out of order in sources, might break in future snapshots
        super(StoneCircles.STONE_CIRCLE_PIECE, x, y, z, 16, 8, 16, method_35457(random));
    }

    public StoneCircleGenerator(ServerWorld world, NbtCompound tag) {
        super(StoneCircles.STONE_CIRCLE_PIECE, tag);
    }

    @Override
    public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator gen, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        int radius = random.nextInt(maxRadius - minRadius) + minRadius;
        Identifier lootTable = StrangeLoot.STONE_CIRCLE;

        List<BlockState> blocks = new ArrayList<>(Arrays.asList(
            Blocks.STONE.getDefaultState(),
            Blocks.COBBLESTONE.getDefaultState(),
            Blocks.MOSSY_COBBLESTONE.getDefaultState()
        ));

        // generate the circle
        boolean generatedSomething = false;
        boolean generatedSpawnRune = false;
        Map<Integer, Float> availableRunes = new HashMap<>();

        for (int i = 0; i < Runestones.WORLD_DESTINATIONS.size(); i++) {
            availableRunes.put(i, Runestones.WORLD_DESTINATIONS.get(i).getWeight());
        }

        if (availableRunes.size() == 0) {
            Charm.LOG.warn("No available runes to generate");
            return false;
        }

        int numberOfRunesGenerated = 0;
        for (int i = 0; i < 360; i += 45) {
            double x = radius * Math.cos(i * Math.PI / 180);
            double z = radius * Math.sin(i * Math.PI / 180);

            for (int s = maxCheckSurface; s > minCheckSurface; s--) {
                BlockPos checkPos = blockPos.add(x, s, z);
                BlockPos checkUpPos = checkPos.up();
                BlockState checkState = world.getBlockState(checkPos);
                BlockState checkUpState = world.getBlockState(checkUpPos);

                boolean validSurfacePos = ((checkState.isOpaque() || checkState.getBlock() == Blocks.LAVA)
                    && (checkUpState.isAir() || !checkUpState.isOpaque() || world.isWater(checkUpPos)));

                if (!validSurfacePos)
                    continue;

                boolean generatedColumn = false;
                int height = random.nextInt(maxHeight - minHeight) + minHeight;
                world.setBlockState(checkPos, blocks.get(0), 2);

                for (int y = 1; y < height; y++) {
                    BlockState state = blocks.get(random.nextInt(blocks.size()));
                    boolean isTop = y == height - 1;

                    if (isTop) {
                        if (!generatedSpawnRune && Runestones.SPAWN_RUNE >= 0) {
                            // generate a spawn destination rune
                            state = Runestones.RUNESTONE_BLOCKS.get(Runestones.SPAWN_RUNE).getDefaultState();
                            generatedSpawnRune = true;
                        } else if (numberOfRunesGenerated < 4 && random.nextFloat() < 0.5F - (numberOfRunesGenerated * 0.15F)) {

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
                                ++numberOfRunesGenerated;
                                break;
                            }
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

        // generate chest or campfire at center
        if (random.nextFloat() < 0.75F) {
            for (int s = maxCheckSurface; s > minCheckSurface; s--) {
                BlockPos checkPos = blockPos.add(0, s, 0);
                BlockPos checkUpPos = checkPos.up();
                BlockState checkState = world.getBlockState(checkPos);
                BlockState checkUpState = world.getBlockState(checkUpPos);

                if (checkState.isOpaque() && !checkUpState.isOpaque() && !checkUpState.getMaterial().isLiquid() && lootTable != null) {
                    boolean generateChest = random.nextBoolean();
                    boolean generateMob = random.nextBoolean();

                    if (generateChest) {
                        BlockState chest;
                        if (ModuleHandler.enabled("charm:variant_chests")) {
                            IVariantMaterial material = DecorationHelper.getRandomVariantMaterial(random);
                            chest = VariantChests.NORMAL_CHEST_BLOCKS.get(material).getDefaultState();
                        } else {
                            chest = Blocks.CHEST.getDefaultState();
                        }
                        world.setBlockState(checkUpPos, chest, 2);
                        LootableContainerBlockEntity.setLootTable(world, random, checkUpPos, lootTable);
                    } else {
                        BlockState hay = Blocks.HAY_BLOCK.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Y);
                        BlockState fire = Blocks.CAMPFIRE.getDefaultState()
                            .with(CampfireBlock.LIT, true)
                            .with(CampfireBlock.SIGNAL_FIRE, true);

                        world.setBlockState(checkPos, hay, 2);
                        world.setBlockState(checkUpPos, fire, 2);
                    }

                    if (generateMob) {
                        BlockPos spawnerPos = checkUpPos.up();
                        BlockState spawner = EntitySpawners.ENTITY_SPAWNER.getDefaultState();
                        world.setBlockState(spawnerPos, spawner, 2);

                        BlockEntity blockEntity = world.getBlockEntity(spawnerPos);

                        if (blockEntity instanceof EntitySpawnerBlockEntity) {
                            EntitySpawnerBlockEntity spawnerEntity = (EntitySpawnerBlockEntity)blockEntity;
                            NbtCompound tag = new NbtCompound();

                            Identifier mobId;
                            int mobCount;

                            float chance = random.nextFloat();
                            if (chance < 0.4F) {
                                mobId = new Identifier("witch");
                                mobCount = random.nextInt(2) + 2;
                            } else if (chance < 0.8F) {
                                mobId = new Identifier("pillager");
                                mobCount = random.nextInt(3) + 3;
                            } else {
                                if (random.nextBoolean() && ModuleHandler.enabled(Mobs.class) && Mobs.illusioners) {
                                    mobId = new Identifier("illusioner");
                                } else {
                                    mobId = new Identifier("evoker");
                                }
                                mobCount = 1;
                            }

                            spawnerEntity.entity = mobId;
                            spawnerEntity.count = mobCount;
                            spawnerEntity.persist = true;
                            spawnerEntity.writeNbt(tag);
                        }
                    }
                    break;
                }
            }
        }

        if (!generatedSomething)
            Charm.LOG.debug("Did not generate a stone circle at: " + blockPos);

        return generatedSomething;
    }
}
