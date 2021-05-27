package svenhjol.strange.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.strange.module.Runestones;
import svenhjol.strange.module.StoneCircles;

import java.util.Random;

public class StoneCircleLootFunction extends ConditionalLootFunction {

    public StoneCircleLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (!ModuleHandler.enabled("strange:runestones"))
            return stack;

        return tryCreateItem(stack, context);
    }

    private ItemStack tryCreateItem(ItemStack stack, LootContext context) {
        Random random = context.getRandom();
        return new ItemStack(Runestones.RUNESTONE_DUST, random.nextInt(6) + 2);
    }

    @Override
    public LootFunctionType getType() {
        return StoneCircles.LOOT_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<StoneCircleLootFunction> {
        @Override
        public StoneCircleLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new StoneCircleLootFunction(conditions);
        }
    }
}
