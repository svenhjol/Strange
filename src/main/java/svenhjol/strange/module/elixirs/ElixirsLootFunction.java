package svenhjol.strange.module.elixirs;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.strange.Strange;
import svenhjol.strange.module.elixirs.elixir.Elixir;

import java.util.Optional;
import java.util.Random;

public class ElixirsLootFunction extends LootItemConditionalFunction {
    protected ElixirsLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(Elixirs.class)) return stack;

        return tryCreate(stack, context);
    }

    private ItemStack tryCreate(ItemStack stack, LootContext context) {
        Random random = context.getRandom();

        if (Elixirs.POTIONS.isEmpty()) return stack;

        // prefer the generic potions
        Optional<IElixir> genericPotion = Elixirs.POTIONS.stream().filter(p -> p.getClass() == Elixir.class).findFirst();
        if (genericPotion.isPresent() && random.nextFloat() < 0.75F) {
            return genericPotion.get().getPotionItem();
        }

        IElixir potion = Elixirs.POTIONS.get(random.nextInt(Elixirs.POTIONS.size()));
        return potion.getPotionItem();
    }

    @Override
    public LootItemFunctionType getType() {
        return Elixirs.LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ElixirsLootFunction> {
        @Override
        public ElixirsLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new ElixirsLootFunction(conditions);
        }
    }
}
