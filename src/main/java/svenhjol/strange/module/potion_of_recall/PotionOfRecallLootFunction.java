package svenhjol.strange.module.potion_of_recall;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.PotionHelper;
import svenhjol.strange.Strange;

import java.util.Random;

public class PotionOfRecallLootFunction extends LootItemConditionalFunction {
    protected PotionOfRecallLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(PotionOfRecall.class)) return stack;
        ServerLevel level = context.getLevel();
        Random random = context.getRandom();

        if (random.nextFloat() > 0.5F) {
            return stack;
        }

        // if the config value onlyOutsideOverworld is set, test the current dimension is not the overworld
        if (PotionOfRecall.onlyOutsideOverworld && DimensionHelper.isOverworld(level)) {
            return stack;
        }

        ItemStack out;
        if (random.nextFloat() < 0.75F) {
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
