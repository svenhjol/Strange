package svenhjol.strange.module.structures;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.structures.legacy.LegacyDataProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true, priority = 10)
public class Structures extends CharmModule {
    public static final ResourceLocation DATA_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "data_block");
    public static final ResourceLocation ENTITY_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "entity_block");

    public static final ResourceLocation MSG_SERVER_UPDATE_BLOCK_ENTITY = new ResourceLocation(Strange.MOD_ID, "server_update_data_block");
    public static final ResourceLocation MSG_CLIENT_OPEN_DATA_BLOCK_SCREEN = new ResourceLocation(Strange.MOD_ID, "client_open_data_block_screen");
    public static final ResourceLocation MSG_CLIENT_OPEN_ENTITY_BLOCK_SCREEN = new ResourceLocation(Strange.MOD_ID, "client_open_entity_block_screen");

    public static final Map<String, Block> DECORATION_ITEM_MAP = new HashMap<>();

    public static IgnoreBlock IGNORE_BLOCK;
    public static DataBlock DATA_BLOCK;
    public static EntityBlock ENTITY_BLOCK;
    public static BlockEntityType<DataBlockEntity> DATA_BLOCK_ENTITY;
    public static BlockEntityType<EntityBlockEntity> ENTITY_BLOCK_ENTITY;

    public static StructureProcessorList DEFAULT_PROCESSORS;
    public static BlockIgnoreProcessor IGNORE_PROCESSOR;

    public static StructureProcessorList LEGACY_PROCESSORS;
    public static BlockIgnoreProcessor LEGACY_IGNORE_PROCESSOR;
    public static StructureProcessorType<LegacyDataProcessor> LEGACY;

    public static int entityTriggerDistance = 16;

    @Override
    public void register() {
        IGNORE_BLOCK = new IgnoreBlock(this);
        ENTITY_BLOCK = new EntityBlock(this);
        DATA_BLOCK = new DataBlock(this);
        ENTITY_BLOCK_ENTITY = CommonRegistry.blockEntity(ENTITY_BLOCK_ID, EntityBlockEntity::new, ENTITY_BLOCK);
        DATA_BLOCK_ENTITY = CommonRegistry.blockEntity(DATA_BLOCK_ID, DataBlockEntity::new, DATA_BLOCK);

        // setup processors
        IGNORE_PROCESSOR = new BlockIgnoreProcessor(ImmutableList.of(IGNORE_BLOCK));
        DEFAULT_PROCESSORS = CommonRegistry.processorList(new ResourceLocation(Strange.MOD_ID, "default_processors"), ImmutableList.of(
            IGNORE_PROCESSOR
        ));

        // setup legacy stuff
        LEGACY = CommonRegistry.structureProcessor(new ResourceLocation(Strange.MOD_ID, "legacy"), () -> LegacyDataProcessor.CODEC);
        LEGACY_IGNORE_PROCESSOR = new BlockIgnoreProcessor(ImmutableList.of(Blocks.GRAY_STAINED_GLASS));

        LEGACY_PROCESSORS = CommonRegistry.processorList(new ResourceLocation(Strange.MOD_ID, "legacy_processors"), ImmutableList.of(
            LEGACY_IGNORE_PROCESSOR,
            LegacyDataProcessor.INSTANCE
        ));

        setupLegacyNonsense();

        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_UPDATE_BLOCK_ENTITY, this::handleUpdateBlockEntity);
    }

    private void handleUpdateBlockEntity(MinecraftServer server, ServerPlayer player, ServerGamePacketListener serverGamePacketListener, FriendlyByteBuf buffer, PacketSender sender) {
        BlockPos pos = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();

        server.execute(() -> {
            if (player == null || player.level == null || tag == null || tag.isEmpty()) return;
            ServerLevel level = (ServerLevel) player.level;

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                blockEntity.load(tag);
                blockEntity.setChanged();
            }
        });
    }

    private void setupLegacyNonsense() {
        Structures.DECORATION_ITEM_MAP.put("anvil", Blocks.ANVIL);
        Structures.DECORATION_ITEM_MAP.put("carpet", Blocks.RED_CARPET);
        Structures.DECORATION_ITEM_MAP.put("cauldron", Blocks.CAULDRON);
        Structures.DECORATION_ITEM_MAP.put("entity", Blocks.PLAYER_HEAD);
        Structures.DECORATION_ITEM_MAP.put("flower", Blocks.DANDELION);
        Structures.DECORATION_ITEM_MAP.put("lantern", Blocks.LANTERN);
        Structures.DECORATION_ITEM_MAP.put("lava", Blocks.MAGMA_BLOCK);
        Structures.DECORATION_ITEM_MAP.put("mob", Blocks.CREEPER_HEAD);
        Structures.DECORATION_ITEM_MAP.put("ore", Blocks.IRON_ORE);
        Structures.DECORATION_ITEM_MAP.put("flowerpot", Blocks.POTTED_DANDELION);
        Structures.DECORATION_ITEM_MAP.put("sapling", Blocks.OAK_SAPLING);
        Structures.DECORATION_ITEM_MAP.put("spawner", Blocks.SPAWNER);
        Structures.DECORATION_ITEM_MAP.put("storage", Blocks.BARREL);
        Structures.DECORATION_ITEM_MAP.put("chest", Blocks.CHEST);
        Structures.DECORATION_ITEM_MAP.put("barrel", Blocks.BARREL);
        Structures.DECORATION_ITEM_MAP.put("block", Blocks.COBBLESTONE);
        Structures.DECORATION_ITEM_MAP.put("decoration", Blocks.SMITHING_TABLE);
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
