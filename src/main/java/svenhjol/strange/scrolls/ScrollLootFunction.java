package svenhjol.strange.scrolls;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;

public class ScrollLootFunction extends ConditionalLootFunction {

    public ScrollLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (context.getRandom().nextFloat() > 0.5F)
            return stack;

        int tier = context.getRandom().nextInt(Scrolls.TIERS - 1) + 1;
        int rarity = context.getRandom().nextInt(2) + 2;

        ItemStack scroll = new ItemStack(Scrolls.SCROLL_TIERS.get(tier));
        ScrollItem.setScrollRarity(scroll, rarity);

        return scroll;
    }

    @Override
    public LootFunctionType getType() {
        return Scrolls.SCROLL_LOOT_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<ScrollLootFunction> {
        @Override
        public ScrollLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new ScrollLootFunction(conditions);
        }
    }
}
