package svenhjol.strange.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.strange.iface.ILegendaryPotion;
import svenhjol.strange.iface.ILegendaryTool;
import svenhjol.strange.module.LegendaryItems;

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

        Map<Integer, ILegendaryTool> items = LegendaryItems.LEGENDARY_TOOLS;
        Map<Integer, ILegendaryPotion> potions = LegendaryItems.LEGENDARY_POTIONS;
        ItemStack itemStack;

        if (false) {
            List<ILegendaryTool> weighted = new ArrayList<>();

            if (items.isEmpty())
                return stack;

            items.forEach((weight, item) -> {
                for (int i = 0; i < weight; i++) {
                    weighted.add(item); // this is so dumb but I'm too tired to implement a better weighted list
                }
            });

            ILegendaryTool item = weighted.get(context.getRandom().nextInt(weighted.size()));
            itemStack = item.getTreasureItemStack();

        } else {
            List<ILegendaryPotion> weighted = new ArrayList<>();

            if (potions.isEmpty())
                return stack;

            potions.forEach((weight, item) -> {
                for (int i = 0; i < weight; i++) {
                    weighted.add(item); // this is so dumb but I'm too tired to implement a better weighted list
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
