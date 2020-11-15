package svenhjol.strange.legendaryitems;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import svenhjol.charm.base.handler.ModuleHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LegendaryItemLootFunction extends ConditionalLootFunction {

    public LegendaryItemLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (!ModuleHandler.enabled("strange:legendary_items"))
            return stack;

        return tryCreateLegendaryItem(stack, context);
    }

    private ItemStack tryCreateLegendaryItem(ItemStack stack, LootContext context) {

        Map<ILegendaryEnchanted, Integer> items = LegendaryItems.LEGENDARY_ENCHANTED;
        Map<ILegendaryPotion, Integer> potions = LegendaryItems.LEGENDARY_POTIONS;
        ItemStack itemStack;

        // prefer items over potions
        if (context.getRandom().nextFloat() < 0.62F) {
            List<ILegendaryEnchanted> weighted = new ArrayList<>();

            if (items.isEmpty())
                return stack;

            items.forEach((item, weight) -> {
                for (int i = 0; i < weight; i++) {
                    weighted.add(item); // this is so dumb but I'm too tired to implement a better weighted list
                }
            });

            ILegendaryEnchanted item = weighted.get(context.getRandom().nextInt(weighted.size()));
            itemStack = item.getTreasureItemStack();

        } else {
            List<ILegendaryPotion> weighted = new ArrayList<>();

            if (potions.isEmpty())
                return stack;

            potions.forEach((potion, weight) -> {
                for (int i = 0; i < weight; i++) {
                    weighted.add(potion); // this is so dumb but I'm too tired to implement a better weighted list
                }
            });

            ILegendaryPotion potion = weighted.get(context.getRandom().nextInt(weighted.size()));
            itemStack = potion.getTreasurePotion();
        }

        return itemStack;
    }

    @Override
    public LootFunctionType getType() {
        return LegendaryItems.LEGENDARY_ITEMS_LOOT_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<LegendaryItemLootFunction> {
        @Override
        public LegendaryItemLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new LegendaryItemLootFunction(conditions);
        }
    }
}
