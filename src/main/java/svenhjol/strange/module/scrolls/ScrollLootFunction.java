package svenhjol.strange.module.scrolls;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Random;

public class ScrollLootFunction extends LootItemConditionalFunction {

    public ScrollLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
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
    public LootItemFunctionType getType() {
        return Scrolls.LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ScrollLootFunction> {
        @Override
        public ScrollLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new ScrollLootFunction(conditions);
        }
    }
}
