package svenhjol.strange.module.potent_potions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.strange.Strange;
import svenhjol.strange.module.potent_potions.potion.Potion;

import java.util.Optional;
import java.util.Random;

public class PotentPotionsLootFunction extends LootItemConditionalFunction {
    protected PotentPotionsLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(PotentPotions.class)) return stack;

        return tryCreate(stack, context);
    }

    private ItemStack tryCreate(ItemStack stack, LootContext context) {
        Random random = context.getRandom();

        if (PotentPotions.POTIONS.isEmpty()) return stack;

        // prefer the generic potions
        Optional<IPotionItem> genericPotion = PotentPotions.POTIONS.stream().filter(p -> p.getClass() == Potion.class).findFirst();
        if (genericPotion.isPresent() && random.nextFloat() < 0.75F) {
            return genericPotion.get().getPotionItem();
        }

        IPotionItem potion = PotentPotions.POTIONS.get(random.nextInt(PotentPotions.POTIONS.size()));
        return potion.getPotionItem();
    }

    @Override
    public LootItemFunctionType getType() {
        return PotentPotions.LOOT_FUNCTION;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<PotentPotionsLootFunction> {
        @Override
        public PotentPotionsLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new PotentPotionsLootFunction(conditions);
        }
    }
}
