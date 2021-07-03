package svenhjol.strange.module.treasure;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import svenhjol.charm.handler.ModuleHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TreasureLootFunction extends LootItemConditionalFunction {

    public TreasureLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!ModuleHandler.enabled("strange:treasure"))
            return stack;

        return tryCreateTreasureItem(stack, context);
    }

    private ItemStack tryCreateTreasureItem(ItemStack stack, LootContext context) {
        Map<ITreasureTool, Integer> tools = Treasure.TOOLS;
        Map<ITreasurePotion, Integer> potions = Treasure.POTIONS;
        ItemStack itemStack;

        // prefer items over potions
        if (context.getRandom().nextFloat() < 0.62F) {
            List<ITreasureTool> weighted = new ArrayList<>();

            if (tools.isEmpty())
                return stack;

            tools.forEach((item, weight) -> {
                for (int i = 0; i < weight; i++) {
                    weighted.add(item); // this is so dumb but I'm too tired to implement a better weighted list
                }
            });

            ITreasureTool item = weighted.get(context.getRandom().nextInt(weighted.size()));
            itemStack = item.getTreasureItemStack();

        } else {
            List<ITreasurePotion> weighted = new ArrayList<>();

            if (potions.isEmpty())
                return stack;

            potions.forEach((potion, weight) -> {
                for (int i = 0; i < weight; i++) {
                    weighted.add(potion); // this is so dumb but I'm too tired to implement a better weighted list
                }
            });

            ITreasurePotion potion = weighted.get(context.getRandom().nextInt(weighted.size()));
            itemStack = potion.getTreasurePotion();
        }

        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return Treasure.TREASURE_LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<TreasureLootFunction> {
        @Override
        public TreasureLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new TreasureLootFunction(conditions);
        }
    }
}
