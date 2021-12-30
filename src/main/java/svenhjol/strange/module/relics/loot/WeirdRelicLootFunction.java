package svenhjol.strange.module.relics.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.strange.module.relics.Relics;
import svenhjol.strange.module.relics.helper.RelicHelper;

public class WeirdRelicLootFunction extends LootItemConditionalFunction {
    public WeirdRelicLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var random = context.getRandom();
        var relic = RelicHelper.getRandomItem(Relics.Type.WEIRD, random);
        if (relic == null) return stack;

        return RelicHelper.getStackWithDamage(relic, random);
    }

    @Override
    public LootItemFunctionType getType() {
        return Relics.WEIRD_LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<WeirdRelicLootFunction> {

        @Override
        public WeirdRelicLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new WeirdRelicLootFunction(conditions);
        }
    }
}
