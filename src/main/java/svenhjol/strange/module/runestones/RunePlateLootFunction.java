package svenhjol.strange.module.runestones;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RunePlateLootFunction extends LootItemConditionalFunction {

    public RunePlateLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        int rune = context.getRandom().nextInt(RunestonesHelper.NUMBER_OF_RUNES);
        return new ItemStack(Runestones.RUNE_PLATES.get(rune));
    }

    @Override
    public LootItemFunctionType getType() {
        return Runestones.RUNE_PLATE_LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RunePlateLootFunction> {
        @Override
        public RunePlateLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new RunePlateLootFunction(conditions);
        }
    }
}
