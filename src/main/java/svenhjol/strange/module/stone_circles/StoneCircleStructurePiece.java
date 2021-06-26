package svenhjol.strange.module.stone_circles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import svenhjol.charm.Charm;
import svenhjol.charm.enums.IVariantMaterial;
import svenhjol.charm.handler.ModuleHandler;
import svenhjol.charm.helper.DecorationHelper;
import svenhjol.charm.module.entity_spawners.EntitySpawnerBlockEntity;
import svenhjol.charm.module.entity_spawners.EntitySpawners;
import svenhjol.charm.module.variant_chests.VariantChests;
import svenhjol.strange.init.StrangeLoot;
import svenhjol.strange.module.mobs.Mobs;
import svenhjol.strange.module.runestones.Runestones;

import java.util.*;

public class StoneCircleStructurePiece extends ScatteredFeaturePiece {
    public static int maxCheckSurface = 5; // if the surface Y value is this many blocks higher than starting Y, don't generate
    public static int minCheckSurface = -15; // if the surface Y value is this many blocks lower than starting Y, don't generate
    public static int maxRadius = 14; // max radius of the circle of stone pillars
    public static int minRadius = 5; // min radius of the circle of stone pillars
    public static int maxHeight = 8; // max possible height of a stone pillar
    public static int minHeight = 4; // min height of a stone pillar
    public static int maxRunes = 5; // max number of runestones that will generate per circle
    public static int runeTries = 10; // number of attempts at rune placement from available runes

    public static final List<String> HOSTILE_MOBS = new ArrayList<>(Arrays.asList(
        "witch", "pillager", "vindicator"
    ));
    public static final List<String> NEUTRAL_MOBS = new ArrayList<>(Arrays.asList(
        "cat", "wolf", "sheep", "villager"
    ));

    public StoneCircleStructurePiece(Random random, int x, int y, int z) {
        super(StoneCircles.STONE_CIRCLE_PIECE, x, y, z, 16, 8, 16, getRandomHorizontalDirection(random));
    }

    public StoneCircleStructurePiece(ServerLevel world, CompoundTag tag) {
        super(StoneCircles.STONE_CIRCLE_PIECE, tag);
    }

    @Override
    public boolean postProcess(WorldGenLevel world, StructureFeatureManager structureAccessor, ChunkGenerator gen, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        int radius = random.nextInt(maxRadius - minRadius) + minRadius;
        ResourceLocation lootTable = StrangeLoot.STONE_CIRCLE;

        List<BlockState> blocks = new ArrayList<>(Arrays.asList(
            Blocks.STONE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState()
        ));

        // generate the circle
        boolean generatedSomething = false;
        boolean generatedSpawnRune = false;
        Map<Integer, Float> availableRunes = new HashMap<>();

        for (int i = 0; i < Runestones.WORLD_DESTINATIONS.size(); i++) {
            availableRunes.put(i, Runestones.WORLD_DESTINATIONS.get(i).getWeight());
        }

        if (availableRunes.size() == 0) {
            Charm.LOG.warn("[StoneCircleStructurePiece] No available runes to generate");
            return false;
        }

        int numberOfRunesGenerated = 0;
        for (int i = 0; i < 360; i += 45) {
            double x = radius * Math.cos(i * Math.PI / 180);
            double z = radius * Math.sin(i * Math.PI / 180);

            for (int s = maxCheckSurface; s > minCheckSurface; s--) {
                BlockPos checkPos = blockPos.offset(x, s, z);
                BlockPos checkUpPos = checkPos.above();
                BlockState checkState = world.getBlockState(checkPos);
                BlockState checkUpState = world.getBlockState(checkUpPos);

                boolean validSurfacePos = ((checkState.canOcclude() || checkState.getBlock() == Blocks.LAVA)
                    && (checkUpState.isAir() || !checkUpState.canOcclude() || world.isWaterAt(checkUpPos)));

                if (!validSurfacePos)
                    continue;

                boolean generatedColumn = false;
                int height = random.nextInt(maxHeight - minHeight) + minHeight;
                world.setBlock(checkPos, blocks.get(0), 2);

                for (int y = 1; y < height; y++) {
                    BlockState state = blocks.get(random.nextInt(blocks.size()));
                    boolean isTop = y == height - 1;

                    if (isTop) {
                        if (!generatedSpawnRune && Runestones.SPAWN_RUNE >= 0) {
                            // generate a spawn destination rune
                            state = Runestones.RUNESTONE_BLOCKS.get(Runestones.SPAWN_RUNE).defaultBlockState();
                            generatedSpawnRune = true;
                        } else if (numberOfRunesGenerated < maxRunes && random.nextFloat() < 0.3F) {

                            // Try and generate a rune. Replace the state with the runestone if successful
                            for (int tries = 0; tries < runeTries; tries++) {
                                List<Integer> keys = new ArrayList<>(availableRunes.keySet());
                                int rune = keys.get(random.nextInt(keys.size()));

                                float f = random.nextFloat();
                                float weight = availableRunes.get(rune);

                                if (f >= weight)
                                    continue;

                                availableRunes.remove(rune);
                                state = Runestones.RUNESTONE_BLOCKS.get(rune).defaultBlockState();
                                ++numberOfRunesGenerated;
                                break;
                            }
                        }
                    }

                    world.setBlock(checkPos.above(y), state, 2);
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
                BlockPos checkPos = blockPos.offset(0, s, 0);
                BlockPos checkUpPos = checkPos.above();
                BlockState checkState = world.getBlockState(checkPos);
                BlockState checkUpState = world.getBlockState(checkUpPos);

                if (checkState.canOcclude() && !checkUpState.canOcclude() && !checkUpState.getMaterial().isLiquid() && lootTable != null) {
                    boolean generateChest = random.nextBoolean();
                    boolean generateMob = random.nextBoolean();

                    if (generateChest) {
                        BlockState chest;
                        if (ModuleHandler.enabled("charm:variant_chests")) {
                            IVariantMaterial material = DecorationHelper.getRandomVariantMaterial(random);
                            chest = VariantChests.NORMAL_CHEST_BLOCKS.get(material).defaultBlockState();
                        } else {
                            chest = Blocks.CHEST.defaultBlockState();
                        }
                        world.setBlock(checkUpPos, chest, 2);
                        RandomizableContainerBlockEntity.setLootTable(world, random, checkUpPos, lootTable);
                    } else {
                        BlockState hay = Blocks.HAY_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
                        BlockState fire = Blocks.CAMPFIRE.defaultBlockState()
                            .setValue(CampfireBlock.LIT, true)
                            .setValue(CampfireBlock.SIGNAL_FIRE, true);

                        world.setBlock(checkPos, hay, 2);
                        world.setBlock(checkUpPos, fire, 2);
                    }

                    if (generateMob) {
                        BlockPos spawnerPos = checkUpPos.above();
                        BlockState spawner = EntitySpawners.ENTITY_SPAWNER.defaultBlockState();
                        world.setBlock(spawnerPos, spawner, 2);
                        BlockEntity blockEntity = world.getBlockEntity(spawnerPos);

                        if (blockEntity instanceof EntitySpawnerBlockEntity spawnerEntity) {
                            CompoundTag tag = new CompoundTag();

                            ResourceLocation mobId;
                            int mobCount;

                            float chance = random.nextFloat();
                            if (chance < 0.45F) {
                                if (NEUTRAL_MOBS.size() > 0) {
                                    mobId = new ResourceLocation(NEUTRAL_MOBS.get(random.nextInt(NEUTRAL_MOBS.size())));
                                } else {
                                    mobId = new ResourceLocation("sheep");
                                }
                                mobCount = random.nextInt(3) + 3;
                            } else if (chance < 0.9F) {
                                if (HOSTILE_MOBS.size() > 0) {
                                    mobId = new ResourceLocation(HOSTILE_MOBS.get(random.nextInt(HOSTILE_MOBS.size())));
                                } else {
                                    mobId = new ResourceLocation("witch");
                                }
                                mobCount = random.nextInt(3) + 3;
                            } else {
                                if (random.nextBoolean() && ModuleHandler.enabled(Mobs.class) && Mobs.illusioners) {
                                    mobId = new ResourceLocation("illusioner");
                                } else {
                                    mobId = new ResourceLocation("evoker");
                                }
                                mobCount = 1;
                            }

                            spawnerEntity.entity = mobId;
                            spawnerEntity.count = mobCount;
                            spawnerEntity.persist = true;
                            spawnerEntity.save(tag);
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
