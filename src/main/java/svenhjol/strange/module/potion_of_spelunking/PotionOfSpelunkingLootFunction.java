package svenhjol.strange.module.potion_of_spelunking;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.PotionHelper;
import svenhjol.strange.Strange;

public class PotionOfSpelunkingLootFunction extends LootItemConditionalFunction {
    protected PotionOfSpelunkingLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(PotionOfSpelunking.class)) return stack;
        var level = context.getLevel();
        var random = context.getRandom();
        var isOverworld = DimensionHelper.isOverworld(level);

        // This should be quite rare in the overworld.
        if (isOverworld && random.nextFloat() > 0.2F) {
            return stack;
        }

        // 25% chance of not generating at all.
        if (random.nextFloat() > 0.75F) {
            return stack;
        }

        return PotionHelper.getPotionBottle(PotionOfSpelunking.SPELUNKING_POTION, 1);
    }

    @Override
    public LootItemFunctionType getType() {
        return PotionOfSpelunking.LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<PotionOfSpelunkingLootFunction> {
        @Override
        public PotionOfSpelunkingLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new PotionOfSpelunkingLootFunction(conditions);
        }
    }
}
