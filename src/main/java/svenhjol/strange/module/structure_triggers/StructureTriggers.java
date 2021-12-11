package svenhjol.strange.module.structure_triggers;

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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true)
public class StructureTriggers extends CharmModule {
    public static final ResourceLocation DATA_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "data_block");
    public static final ResourceLocation MSG_SERVER_UPDATE_DATA_BLOCK = new ResourceLocation(Strange.MOD_ID, "server_update_data_block");
    public static final ResourceLocation MSG_CLIENT_OPEN_DATA_BLOCK_SCREEN = new ResourceLocation(Strange.MOD_ID, "client_open_data_block_screen");
    public static final Map<String, Block> DECORATION_ITEM_MAP = new HashMap<>();

    public static IgnoreBlock IGNORE_BLOCK;
    public static DataBlock DATA_BLOCK;
    public static BlockEntityType<DataBlockEntity> DATA_BLOCK_ENTITY;

    @Override
    public void register() {
        IGNORE_BLOCK = new IgnoreBlock(this);
        DATA_BLOCK = new DataBlock(this);
        DATA_BLOCK_ENTITY = CommonRegistry.blockEntity(DATA_BLOCK_ID, DataBlockEntity::new, DATA_BLOCK);

        setupLegacyNonsense();

        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_UPDATE_DATA_BLOCK, this::handleUpdateDataBlock);
    }

    private void handleUpdateDataBlock(MinecraftServer server, ServerPlayer player, ServerGamePacketListener serverGamePacketListener, FriendlyByteBuf buffer, PacketSender sender) {
        BlockPos blockPos = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();

        server.execute(() -> {
            if (player == null || player.level == null || tag == null || tag.isEmpty()) return;
            ServerLevel level = (ServerLevel) player.level;

            if (level.getBlockEntity(blockPos) instanceof DataBlockEntity data) {
                data.load(tag);
                data.setChanged();
            }
        });
    }

    private void setupLegacyNonsense() {
        StructureTriggers.DECORATION_ITEM_MAP.put("anvil", Blocks.ANVIL);
        StructureTriggers.DECORATION_ITEM_MAP.put("carpet", Blocks.RED_CARPET);
        StructureTriggers.DECORATION_ITEM_MAP.put("cauldron", Blocks.CAULDRON);
        StructureTriggers.DECORATION_ITEM_MAP.put("entity", Blocks.PLAYER_HEAD);
        StructureTriggers.DECORATION_ITEM_MAP.put("flower", Blocks.DANDELION);
        StructureTriggers.DECORATION_ITEM_MAP.put("lantern", Blocks.LANTERN);
        StructureTriggers.DECORATION_ITEM_MAP.put("lava", Blocks.MAGMA_BLOCK);
        StructureTriggers.DECORATION_ITEM_MAP.put("mob", Blocks.CREEPER_HEAD);
        StructureTriggers.DECORATION_ITEM_MAP.put("ore", Blocks.IRON_ORE);
        StructureTriggers.DECORATION_ITEM_MAP.put("flowerpot", Blocks.POTTED_DANDELION);
        StructureTriggers.DECORATION_ITEM_MAP.put("sapling", Blocks.OAK_SAPLING);
        StructureTriggers.DECORATION_ITEM_MAP.put("spawner", Blocks.SPAWNER);
        StructureTriggers.DECORATION_ITEM_MAP.put("storage", Blocks.BARREL);
        StructureTriggers.DECORATION_ITEM_MAP.put("chest", Blocks.CHEST);
        StructureTriggers.DECORATION_ITEM_MAP.put("barrel", Blocks.BARREL);
        StructureTriggers.DECORATION_ITEM_MAP.put("block", Blocks.COBBLESTONE);
        StructureTriggers.DECORATION_ITEM_MAP.put("decoration", Blocks.SMITHING_TABLE);
    }

    public static boolean converterator(ServerLevel level, BlockPos pos, String metadata) {
        // convert old format
        BlockState state = null;
        String loot = "";
        String type = "";
        Direction facing = level.getBlockState(pos).getValue(DataBlock.FACING);

        if (!metadata.isEmpty()) {
            String mt = metadata.trim();
            String md = mt.contains(" ") ? mt.split(" ")[0] : mt;

            if (mt.contains(" ")) {
                List<String> fragments = List.of(mt.split(" "));
                for (String frag : fragments) {
                    if (frag.contains("=")) {
                        String[] p = frag.split("=");
                        switch (p[0]) {
                            case "loot":
                                loot = p[1];
                                break;
                            case "type":
                                type = p[1];
                            case "facing":
                                Direction direction = Direction.byName(p[1]);
                                if (direction != null) {
                                    facing = direction;
                                }
                                break;
                        }
                    }
                }
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

            if (state == null) return false;
            level.setBlockAndUpdate(pos, state);

            if (!loot.isEmpty() && level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
                container.setLootTable(new ResourceLocation(loot), level.random.nextLong());
            }

            return true;
        }

        return false;
    }

}
