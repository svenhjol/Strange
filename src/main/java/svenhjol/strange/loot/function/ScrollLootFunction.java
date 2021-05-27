package svenhjol.strange.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import svenhjol.strange.item.ScrollItem;
import svenhjol.strange.module.Scrolls;

import java.util.Random;

public class ScrollLootFunction extends ConditionalLootFunction {

    public ScrollLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        Random random = context.getRandom();
        if (random.nextFloat() > 0.66F)
            return stack;

        int tier = context.getRandom().nextInt(Scrolls.TIERS - 1) + 1;
        int rarity = context.getRandom().nextInt(2) + 2;

        float chance = 1.0F - (0.1F * tier);
        if (random.nextFloat() > chance)
            return stack;

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
