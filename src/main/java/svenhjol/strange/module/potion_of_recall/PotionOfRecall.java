package svenhjol.strange.module.potion_of_recall;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.ModHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID, description = "Drinking a Potion of Recall teleports you back to the overworld spawn point regardless of your dimension and position.")
public class PotionOfRecall extends CharmModule {
    public static RecallPotion RECALL_POTION;
    public static RecallEffect RECALL_EFFECT;
    public static ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "potion_of_recall_loot");
    public static LootItemFunctionType LOOT_FUNCTION;

    public static final ResourceLocation TRIGGER_TRAVEL_TO_SPAWN_POINT = new ResourceLocation(Strange.MOD_ID, "travel_to_spawn_point");
    private final List<ResourceLocation> VALID_LOOT_TABLES = new ArrayList<>();

    public static boolean onlyOutsideOverworld = false;

    @Override
    public void register() {
        RECALL_EFFECT = new RecallEffect(this);
        RECALL_POTION = new RecallPotion(this);
        LOOT_FUNCTION = CommonRegistry.lootFunctionType(LOOT_ID, new LootItemFunctionType(new PotionOfRecallLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        VALID_LOOT_TABLES.add(BuiltInLootTables.SIMPLE_DUNGEON);
        onlyOutsideOverworld = ModHelper.isLoaded("strange_dimensions");
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootTables, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter lootTableSetter) {
        if (VALID_LOOT_TABLES.contains(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.AIR)
                    .setWeight(1)
                    .apply(() -> new PotionOfRecallLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }
}
