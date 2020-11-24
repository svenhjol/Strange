package svenhjol.strange.scrolls;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;

public class NormalScrollLootFunction extends ConditionalLootFunction {

    public NormalScrollLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (context.getRandom().nextFloat() > Scrolls.lootChance)
            return stack;

        int tier = context.getRandom().nextInt(Scrolls.TIERS - 1) + 1;
        int rarity = context.getRandom().nextInt(2) + 2;

        ItemStack scroll = new ItemStack(Scrolls.SCROLL_TIERS.get(tier));
        ScrollItem.setScrollRarity(scroll, rarity);

        return scroll;
    }

    @Override
    public LootFunctionType getType() {
        return Scrolls.NORMAL_SCROLL_LOOT_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<NormalScrollLootFunction> {
        @Override
        public NormalScrollLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new NormalScrollLootFunction(conditions);
        }
    }
}
