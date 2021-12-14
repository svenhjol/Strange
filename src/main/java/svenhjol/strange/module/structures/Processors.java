package svenhjol.strange.module.structures;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.structures.legacy.LegacyDataProcessor;
import svenhjol.strange.module.structures.processor.AnvilDamageProcessor;
import svenhjol.strange.module.structures.processor.StoneBricksDecayProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Processors {
    public static BlockIgnoreProcessor IGNORE;
    public static StructureProcessorType<AnvilDamageProcessor> ANVIL_DAMAGE;
    public static StructureProcessorType<StoneBricksDecayProcessor> STONE_BRICKS_DECAY;

    public static Map<String, Block> LEGACY_DATA_MAP = new HashMap<>();
    public static StructureProcessorList LEGACY_DATA_BLOCKS;
    public static BlockIgnoreProcessor LEGACY_IGNORE;
    public static StructureProcessorType<LegacyDataProcessor> LEGACY;

    public static void init() {
        // register custom processors
        IGNORE = new BlockIgnoreProcessor(ImmutableList.of(Structures.IGNORE_BLOCK));
        ANVIL_DAMAGE = CommonRegistry.structureProcessor(new ResourceLocation(Strange.MOD_ID, "anvil_damage"), () -> AnvilDamageProcessor.CODEC);
        STONE_BRICKS_DECAY = CommonRegistry.structureProcessor(new ResourceLocation(Strange.MOD_ID, "stone_bricks_decay"), () -> StoneBricksDecayProcessor.CODEC);

        // register legacy stuff
        LEGACY = CommonRegistry.structureProcessor(new ResourceLocation(Strange.MOD_ID, "legacy"), () -> LegacyDataProcessor.CODEC);
        LEGACY_IGNORE = new BlockIgnoreProcessor(ImmutableList.of(Blocks.GRAY_STAINED_GLASS));

        LEGACY_DATA_BLOCKS = CommonRegistry.processorList(new ResourceLocation(Strange.MOD_ID, "legacy_data_blocks"), ImmutableList.of(
            LEGACY_IGNORE,
            LegacyDataProcessor.INSTANCE
        ));

        setupLegacyNonsense();
    }

    private static void setupLegacyNonsense() {
        LEGACY_DATA_MAP.put("anvil", Blocks.ANVIL);
        LEGACY_DATA_MAP.put("carpet", Blocks.RED_CARPET);
        LEGACY_DATA_MAP.put("cauldron", Blocks.CAULDRON);
        LEGACY_DATA_MAP.put("entity", Blocks.PLAYER_HEAD);
        LEGACY_DATA_MAP.put("flower", Blocks.DANDELION);
        LEGACY_DATA_MAP.put("lantern", Blocks.LANTERN);
        LEGACY_DATA_MAP.put("lava", Blocks.MAGMA_BLOCK);
        LEGACY_DATA_MAP.put("mob", Blocks.CREEPER_HEAD);
        LEGACY_DATA_MAP.put("ore", Blocks.IRON_ORE);
        LEGACY_DATA_MAP.put("flowerpot", Blocks.POTTED_DANDELION);
        LEGACY_DATA_MAP.put("sapling", Blocks.OAK_SAPLING);
        LEGACY_DATA_MAP.put("spawner", Blocks.SPAWNER);
        LEGACY_DATA_MAP.put("storage", Blocks.BARREL);
        LEGACY_DATA_MAP.put("chest", Blocks.CHEST);
        LEGACY_DATA_MAP.put("barrel", Blocks.BARREL);
        LEGACY_DATA_MAP.put("block", Blocks.COBBLESTONE);
        LEGACY_DATA_MAP.put("decoration", Blocks.SMITHING_TABLE);
    }

    public static boolean converterator(ServerLevel level, BlockPos pos, String metadata) {
        // convert old format
        BlockState state = null;
        String loot;
        String type;
        Direction facing;
        Map<String, String> pairs = new HashMap<>();
        Consumer<BlockState> afterStateChange = s -> {};

        if (!metadata.isEmpty()) {
            String mt = metadata.trim();
            String md = mt.contains(" ") ? mt.split(" ")[0] : mt;

            if (mt.contains(" ")) {
                List<String> fragments = List.of(mt.split(" "));
                for (String frag : fragments) {
                    if (frag.contains("=")) {
                        String[] p = frag.split("=");
                        pairs.put(p[0], p[1]);
                    }
                }
            }

            loot = pairs.getOrDefault("loot", "");
            type = pairs.getOrDefault("type", "");
            String f = pairs.getOrDefault("facing", null);
            if (f == null) {
                facing = level.getBlockState(pos).getValue(DataBlock.FACING);
            } else {
                facing = Direction.byName(f);
                if (facing == null) facing = Direction.NORTH;
            }

            if (md.equals("storage")) {
                state = Blocks.BARREL.defaultBlockState();
            }
            if (md.equals("anvil")) {
                state = Blocks.ANVIL.defaultBlockState();
            }
            if (md.equals("carpet")) {
                state = Blocks.RED_CARPET.defaultBlockState();
            }
            if (md.equals("cauldron")) {
                state = Blocks.CAULDRON.defaultBlockState();
            }
            if (md.equals("barrel")) {
                state = Blocks.BARREL.defaultBlockState();
            }
            if (md.equals("chest")) {
                state = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
            }
            if (md.equals("flower")) {
                state = Blocks.DANDELION.defaultBlockState();
            }
            if (md.equals("flowerpot")) {
                state = Blocks.POTTED_DANDELION.defaultBlockState();
            }
            if (md.equals("sapling")) {
                state = Blocks.OAK_SAPLING.defaultBlockState();
            }
            if (md.equals("lava")) {
                state = Blocks.MAGMA_BLOCK.defaultBlockState();
            }
            if (md.equals("ore")) {
                state = Blocks.IRON_ORE.defaultBlockState();
            }
            if (md.equals("lantern")) {
                state = Blocks.LANTERN.defaultBlockState();
            }
            if (md.equals("lantern_hanging")) {
                state = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
            }
            if (md.equals("decoration")) {
                state = Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, facing);
            }
            if (md.equals("block")) {
                Optional<Block> opt = Registry.BLOCK.getOptional(new ResourceLocation(type));
                if (opt.isPresent()) {
                    state = opt.get().defaultBlockState();
                }
            }
            if (md.equals("spawner")) {
                state = Blocks.SPAWNER.defaultBlockState();
            }
            if (md.equals("mob") || md.equals("entity")) {
                state = Structures.ENTITY_BLOCK.defaultBlockState();
                afterStateChange = s -> {
                    if (level.getBlockEntity(pos) instanceof EntityBlockEntity entityBlock) {
                        entityBlock.setPrimed(false);
                        entityBlock.setPersistent(Boolean.parseBoolean(pairs.getOrDefault("persist", "true")));
                        entityBlock.setHealth(Double.parseDouble(pairs.getOrDefault("health", "20")));
                        entityBlock.setArmor(pairs.getOrDefault("armor", ""));
                        entityBlock.setEffects(pairs.getOrDefault("effects", ""));
                        entityBlock.setCount(Integer.parseInt(pairs.getOrDefault("count", "1")));
                        entityBlock.setEntity(new ResourceLocation(pairs.getOrDefault("type", "minecraft:sheep")));
                        entityBlock.setChanged();
                    }
                };
            }

            if (state == null) return false;
            level.setBlockAndUpdate(pos, state);

            if (!loot.isEmpty() && level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
                container.setLootTable(new ResourceLocation(loot), level.random.nextLong());
            }

            afterStateChange.accept(state);
            return true;
        }

        return false;
    }
}
