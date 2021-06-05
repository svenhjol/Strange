package svenhjol.strange.module.runestones;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;

public class RunePlateLootFunction extends ConditionalLootFunction {

    public RunePlateLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        int rune = context.getRandom().nextInt(RunestonesHelper.NUMBER_OF_RUNES);
        return new ItemStack(Runestones.RUNE_PLATES.get(rune));
    }

    @Override
    public LootFunctionType getType() {
        return Runestones.RUNE_PLATE_LOOT_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<RunePlateLootFunction> {
        @Override
        public RunePlateLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new RunePlateLootFunction(conditions);
        }
    }
}
