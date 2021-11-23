package svenhjol.strange.module.relics;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.strange.Strange;

import java.util.Random;

public class RelicsLootFunction extends LootItemConditionalFunction {
    protected RelicsLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(Relics.class)) return stack;

        return tryCreate(stack, context);
    }

    private ItemStack tryCreate(ItemStack stack, LootContext context) {
        Random random = context.getRandom();

        if (Relics.RELICS.isEmpty()) return stack;

        IRelicItem relic = Relics.RELICS.get(random.nextInt(Relics.RELICS.size()));
        if (relic == null) {
            return stack;
        }

        ItemStack item = relic.getRelicItem();
        if (relic.isDamaged()) {
            int maxDamage = item.getMaxDamage();
            item.setDamageValue(Math.max(0, (maxDamage / 2) - random.nextInt(maxDamage / 2)));
        }

        return item;
    }

    @Override
    public LootItemFunctionType getType() {
        return Relics.LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RelicsLootFunction> {

        @Override
        public RelicsLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new RelicsLootFunction(conditions);
        }
    }
}
