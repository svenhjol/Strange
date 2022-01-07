package svenhjol.strange.module.elixirs;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.api.event.PlayerTickCallback;
import svenhjol.charm.helper.ClassHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NbtHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.stone_ruins.StoneRuinsLoot;
import svenhjol.strange.module.vaults.VaultsLoot;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID, description = "Discoverable potions with much greater strength and duration.")
public class Elixirs extends CharmModule {
    public static final String ELIXIR_NAMESPACE = "svenhjol.strange.module.elixirs.elixir";
    public static final String ELIXIR_TAG = "strange_elixir";
    public static final List<IElixir> POTIONS = new ArrayList<>();
    public static final List<ResourceLocation> VALID_LOOT_TABLES = new ArrayList<>();
    public static final ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "elixirs_loot");
    public static LootItemFunctionType LOOT_FUNCTION;

    private static final ResourceLocation TRIGGER_FIND_ELIXIR = new ResourceLocation(Strange.MOD_ID, "find_elixir");
    private static Advancement CACHED_ELIXIR_ADVANCEMENT = null;

    @Config(name = "Additional loot tables", description = "List of additional loot tables that elixirs will be added to.")
    public static List<String> additionalLootTables = List.of();

    @Config(name = "Blacklist", description = "List of elixirs that will not be loaded. See wiki for details.")
    public static List<String> configBlacklist = new ArrayList<>();

    @Override
    public void register() {
        LOOT_FUNCTION = CommonRegistry.lootFunctionType(LOOT_ID, new LootItemFunctionType(new ElixirsLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
        VALID_LOOT_TABLES.add(StoneRuinsLoot.STONE_RUINS_ROOM);
        VALID_LOOT_TABLES.add(VaultsLoot.VAULTS_ROOM);

        try {
            List<String> classes = ClassHelper.getClassesInPackage(ELIXIR_NAMESPACE);
            for (String className : classes) {
                String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (configBlacklist.contains(simpleClassName)) continue;

                    IElixir potion = (IElixir)clazz.getDeclaredConstructor().newInstance();
                    POTIONS.add(potion);
                    LogHelper.debug(Strange.MOD_ID, getClass(), "Loaded potion: " + simpleClassName);
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    LogHelper.warn(getClass(), "Elixir `" + simpleClassName + "` failed to load: " + e.getMessage());
                }
            }
        } catch (IOException | URISyntaxException e) {
            LogHelper.info(Strange.MOD_ID, getClass(), "Failed to load classes from namespace: " + e.getMessage());
        }

        for (String lootTable : additionalLootTables) {
            VALID_LOOT_TABLES.add(new ResourceLocation(lootTable));
        }
    }

    public static void triggerFindElixir(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_FIND_ELIXIR);
    }

    private void handlePlayerTick(Player player) {
        if (player.level.isClientSide || player.level.getGameTime() % 80 == 0) return;
        var serverPlayer = (ServerPlayer) player;

        if (CACHED_ELIXIR_ADVANCEMENT == null) {
            var advancements = serverPlayer.getLevel().getServer().getAdvancements();
            var id = new ResourceLocation(Strange.MOD_ID, "elixirs/" + TRIGGER_FIND_ELIXIR.getPath());
            CACHED_ELIXIR_ADVANCEMENT = advancements.getAdvancement(id);
        }

        if (CACHED_ELIXIR_ADVANCEMENT != null) {
            var progress = serverPlayer.getAdvancements().getOrStartProgress(CACHED_ELIXIR_ADVANCEMENT);

            if (!progress.isDone()) {
                var inventory = player.getInventory();
                var size = inventory.getContainerSize();
                for (int i = 0; i < size; i++) {
                    var inSlot = inventory.getItem(i);
                    if (inSlot.isEmpty()) continue;
                    if (NbtHelper.tagExists(inSlot, ELIXIR_TAG)) {
                        triggerFindElixir(serverPlayer);
                        break;
                    }
                }
            }
        }
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootManager, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (VALID_LOOT_TABLES.contains(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.GLASS_BOTTLE)
                    .setWeight(1)
                    .apply(() -> new ElixirsLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }
}
