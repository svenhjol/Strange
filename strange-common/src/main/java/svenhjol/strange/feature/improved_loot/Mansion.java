package svenhjol.strange.feature.improved_loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import svenhjol.charm.Charm;
import svenhjol.charm_api.event.LootTableModifyEvent;
import svenhjol.charm_core.helper.CharmEnchantmentHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Mansion {
    private static final String ID = "improved_mansion_loot";
    private static final List<Item> TOOLS = new ArrayList<>();
    private static final List<Item> ARMOR = new ArrayList<>();
    private static final int MIN_ROLLS = 1;
    private static final int MAX_ROLLS = 3;
    private static Supplier<LootItemFunctionType> LOOT_FUNCTION;

    public void register() {
        LOOT_FUNCTION = Charm.REGISTRY.lootFunctionType(ID, () -> new LootItemFunctionType(new LootFunction.Serializer()));
    }

    public void runWhenEnabled() {
        LootTableModifyEvent.INSTANCE.handle(this::handleLootTableModify);

        TOOLS.addAll(Arrays.asList(
            Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_PICKAXE
        ));

        ARMOR.addAll(Arrays.asList(
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS
        ));
    }

    private Optional<LootPool.Builder> handleLootTableModify(LootTables lootTables, ResourceLocation id) {
        if (id.equals(BuiltInLootTables.WOODLAND_MANSION)) {
            var builder = LootPool.lootPool()
                .setRolls(UniformGenerator.between(MIN_ROLLS, MAX_ROLLS));

            addItem(builder, Items.IRON_AXE, 10);
            addItem(builder, Items.IRON_CHESTPLATE, 10);
            addItem(builder, Items.BOOK, 5);
            addItem(builder, Items.EMERALD, 4);
            addItem(builder, Items.TOTEM_OF_UNDYING, 2);

            return Optional.of(builder);
        }
        return Optional.empty();
    }

    private void addItem(LootPool.Builder builder, Item item, int weight) {
        builder.add(LootItem.lootTableItem(item).setWeight(weight).apply(() -> new LootFunction(new LootItemCondition[0])));
    }

    static class LootFunction extends LootItemConditionalFunction {
        protected LootFunction(LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        protected ItemStack run(ItemStack stack, LootContext context) {
            var item = stack.getItem();
            var random = context.getRandom();

            int level;
            ItemStack out;

            if (item == Items.BOOK) {
                var f = random.nextFloat();

                if (f < 0.5F) {
                    var book = new ItemStack(Items.ENCHANTED_BOOK);
                    var enchantment = f < 0.25F ? Enchantments.MENDING : Enchantments.INFINITY_ARROWS;
                    CharmEnchantmentHelper.applyOnItem(book, enchantment, 1);
                    return book;
                } else {
                    level = 30;
                    out = stack.copy();
                }
            } else if (item == Items.IRON_AXE) {
                level = 20;
                item = TOOLS.get(random.nextInt(TOOLS.size()));
                out = new ItemStack(item);
            } else if (item == Items.IRON_CHESTPLATE) {
                level = 20;
                item = ARMOR.get(random.nextInt(ARMOR.size()));
                out = new ItemStack(item);
            } else if (item == Items.EMERALD) {
                return new ItemStack(item, random.nextInt(5) + 1);
            } else {
                return stack;
            }

            out = EnchantmentHelper.enchantItem(random, out, level, true);

            if (EnchantmentHelper.getEnchantments(out).isEmpty()) {
                Charm.LOG.debug(getClass(), "Item did not get enchanted properly");
                return ItemStack.EMPTY;
            }

            return out;
        }

        @Override
        public LootItemFunctionType getType() {
            return LOOT_FUNCTION.get();
        }

        public static class Serializer extends LootItemConditionalFunction.Serializer<LootFunction> {
            @Override
            public LootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
                return new LootFunction(conditions);
            }
        }
    }
}
