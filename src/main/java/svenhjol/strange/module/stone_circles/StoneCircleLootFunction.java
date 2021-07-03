package svenhjol.strange.module.stone_circles;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import svenhjol.charm.handler.ModuleHandler;
import svenhjol.strange.module.runestones.Runestones;

import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class StoneCircleLootFunction extends LootItemConditionalFunction {

    public StoneCircleLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!ModuleHandler.enabled("strange:runestones"))
            return stack;

        return tryCreateItem(stack, context);
    }

    private ItemStack tryCreateItem(ItemStack stack, LootContext context) {
        Random random = context.getRandom();
        return new ItemStack(Runestones.RUNESTONE_DUST, random.nextInt(6) + 2);
    }

    @Override
    public LootItemFunctionType getType() {
        return StoneCircles.LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<StoneCircleLootFunction> {
        @Override
        public StoneCircleLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new StoneCircleLootFunction(conditions);
        }
    }
}
