package svenhjol.strange.module.potion_of_recall;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.PotionHelper;

public class PotionOfRecallLootFunction extends LootItemConditionalFunction {
    protected PotionOfRecallLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var level = context.getLevel();
        var random = context.getRandom();

        // 50% chance of not generating at all.
        if (random.nextFloat() > 0.5F) {
            return stack;
        }

        // If the config value onlyOutsideOverworld is set, test the current dimension is not the overworld.
        if (PotionOfRecall.onlyOutsideOverworld && DimensionHelper.isOverworld(level)) {
            return stack;
        }

        ItemStack out;
        if (random.nextFloat() < 0.6F) {
            out = PotionHelper.getPotionBottle(PotionOfRecall.RECALL_POTION, 1);
        } else if (random.nextFloat() < 0.9F) {
            out = PotionHelper.getSplashPotionBottle(PotionOfRecall.RECALL_POTION, 1);
        } else {
            out = PotionHelper.getLingeringPotionBottle(PotionOfRecall.RECALL_POTION, 1);
        }

        return out;
    }

    @Override
    public LootItemFunctionType getType() {
        return PotionOfRecall.LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<PotionOfRecallLootFunction> {
        @Override
        public PotionOfRecallLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new PotionOfRecallLootFunction(conditions);
        }
    }
}
