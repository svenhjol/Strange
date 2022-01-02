package svenhjol.strange.module.ender_bundles;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EnderBundleLootFunction extends LootItemConditionalFunction {
    public EnderBundleLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var random = context.getRandom();

        if (random.nextBoolean()) {
            return new ItemStack(EnderBundles.ENDER_BUNDLE);
        }

        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return EnderBundles.LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<EnderBundleLootFunction> {

        @Override
        public EnderBundleLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new EnderBundleLootFunction(conditions);
        }
    }
}
