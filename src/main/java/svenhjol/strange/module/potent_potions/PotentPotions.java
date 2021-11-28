package svenhjol.strange.module.potent_potions;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.ClassHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class PotentPotions extends CharmModule {
    public static final String POTION_NAMESPACE = "svenhjol.strange.module.potent_potions.potion";
    public static final List<IPotionItem> POTIONS = new ArrayList<>();
    public static final List<ResourceLocation> VALID_LOOT_TABLES = new ArrayList<>();
    public static final ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "potent_potions_loot");
    public static LootItemFunctionType LOOT_FUNCTION;

    @Config(name = "Loot tables", description = "List of loot tables to add Potent Potions to.")
    public static List<String> configLootTables = Arrays.asList(
        "strange:gameplay/rubble" // TODO: temporary
    );

    @Config(name = "Blacklist", description = "List of potent potions that will not be loaded. See wiki for details.")
    public static List<String> configBlacklist = new ArrayList<>();

    @Override
    public void register() {
        LOOT_FUNCTION = CommonRegistry.lootFunctionType(LOOT_ID, new LootItemFunctionType(new PotentPotionsLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        try {
            List<String> classes = ClassHelper.getClassesInPackage(POTION_NAMESPACE);
            for (String className : classes) {
                String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (configBlacklist.contains(simpleClassName)) continue;

                    IPotionItem potion = (IPotionItem)clazz.getDeclaredConstructor().newInstance();
                    POTIONS.add(potion);
                    LogHelper.debug(getClass(), "Loaded potion: " + simpleClassName);
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    LogHelper.warn(getClass(), "Potion `" + simpleClassName + "` failed to load: " + e.getMessage());
                }
            }
        } catch (IOException | URISyntaxException e) {
            LogHelper.error(getClass(), "Failed to load classes from namespace: " + e.getMessage());
        }

        for (String lootTable : configLootTables) {
            VALID_LOOT_TABLES.add(new ResourceLocation(lootTable));
        }
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootManager, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (VALID_LOOT_TABLES.contains(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.GLASS_BOTTLE)
                    .setWeight(1)
                    .apply(() -> new PotentPotionsLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }
}
