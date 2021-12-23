package svenhjol.strange.module.structures;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.structures.network.ServerReceiveUpdateStructureBlock;
import svenhjol.strange.module.structures.network.ServerSendOpenDataBlockScreen;
import svenhjol.strange.module.structures.network.ServerSendOpenEntityBlockScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true, priority = 10, description = "Special blocks for building dynamic structures.")
public class Structures extends CharmModule {
    public static final ResourceLocation DATA_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "data_block");
    public static final ResourceLocation ENTITY_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "entity_block");

    public static ServerSendOpenDataBlockScreen SERVER_SEND_OPEN_DATA_BLOCK_SCREEN;
    public static ServerSendOpenEntityBlockScreen SERVER_SEND_OPEN_ENTITY_BLOCK_SCREEN;
    public static ServerReceiveUpdateStructureBlock SERVER_RECEIVE_UPDATE_STRUCTURE_BLOCK;

    public static IgnoreBlock IGNORE_BLOCK;
    public static DataBlock DATA_BLOCK;
    public static EntityBlock ENTITY_BLOCK;
    public static BlockEntityType<DataBlockEntity> DATA_BLOCK_ENTITY;
    public static BlockEntityType<EntityBlockEntity> ENTITY_BLOCK_ENTITY;

    public static final Map<ResourceLocation, List<ItemStack>> DECORATIONS = new HashMap<>();

    public static int entityTriggerDistance = 16;

    @Override
    public void register() {
        IGNORE_BLOCK = new IgnoreBlock(this);
        ENTITY_BLOCK = new EntityBlock(this);
        DATA_BLOCK = new DataBlock(this);
        ENTITY_BLOCK_ENTITY = CommonRegistry.blockEntity(ENTITY_BLOCK_ID, EntityBlockEntity::new, ENTITY_BLOCK);
        DATA_BLOCK_ENTITY = CommonRegistry.blockEntity(DATA_BLOCK_ID, DataBlockEntity::new, DATA_BLOCK);

        Processors.init();
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);

        SERVER_SEND_OPEN_DATA_BLOCK_SCREEN = new ServerSendOpenDataBlockScreen();
        SERVER_SEND_OPEN_ENTITY_BLOCK_SCREEN = new ServerSendOpenEntityBlockScreen();
        SERVER_RECEIVE_UPDATE_STRUCTURE_BLOCK = new ServerReceiveUpdateStructureBlock();
    }

    private void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        if (DimensionHelper.isOverworld(level)) {
            LootContext.Builder builder = new LootContext.Builder(level);
            List<ResourceLocation> ids = new ArrayList<>(server.getLootTables().getIds());

            List<ResourceLocation> decorations = ids.stream()
                .filter(id -> id.getNamespace().equals(Strange.MOD_ID) && id.getPath().startsWith("decorations/"))
                .collect(Collectors.toList());

            decorations.forEach(table -> {
                List<ItemStack> itemStacks = loadLootTable(server, builder, table);
                Structures.DECORATIONS.put(table, itemStacks);
            });
        }
    }

    private static List<ItemStack> loadLootTable(MinecraftServer server, LootContext.Builder builder, ResourceLocation lootTable) {
        return server.getLootTables().get(lootTable).getRandomItems(builder.create(LootContextParamSets.EMPTY));
    }
}
