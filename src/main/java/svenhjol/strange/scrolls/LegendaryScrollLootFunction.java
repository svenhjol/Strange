package svenhjol.strange.scrolls;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;

public class LegendaryScrollLootFunction extends ConditionalLootFunction {

    public LegendaryScrollLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        int tier = Scrolls.TIERS;

        ItemStack scroll = new ItemStack(Scrolls.SCROLL_TIERS.get(tier));
        ScrollItem.setScrollRarity(scroll, 1);

        return scroll;
    }

    @Override
    public LootFunctionType getType() {
        return Scrolls.NORMAL_SCROLL_LOOT_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<LegendaryScrollLootFunction> {
        @Override
        public LegendaryScrollLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new LegendaryScrollLootFunction(conditions);
        }
    }
}
