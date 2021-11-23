package svenhjol.strange.module.experience_bottles;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.strange.Strange;
import svenhjol.strange.module.colored_glints.ColoredGlints;

import java.util.Locale;
import java.util.Random;

public class ExperienceBottleLootFunction extends LootItemConditionalFunction {
    protected ExperienceBottleLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(ExperienceBottles.class)) return stack;
        return tryCreate(stack, context);
    }

    @Override
    public LootItemFunctionType getType() {
        return ExperienceBottles.LOOT_FUNCTION;
    }

    private ItemStack tryCreate(ItemStack stack, LootContext context) {
        ExperienceBottles.Type type;
        Random random = context.getRandom();

        if (ExperienceBottles.EXPERIENCE_BOTTLES.isEmpty()) return stack;

        if (random.nextFloat() < 0.1F) {
            type = ExperienceBottles.Type.GREATEST;
        } else {
            type = ExperienceBottles.Type.GREATER;
        }

        ItemStack bottle = new ItemStack(ExperienceBottles.EXPERIENCE_BOTTLES.get(type));
        ColoredGlints.applyColoredGlint(bottle, type.getColor().getSerializedName().toLowerCase(Locale.ROOT));
        return bottle;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ExperienceBottleLootFunction> {
        @Override
        public ExperienceBottleLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new ExperienceBottleLootFunction(conditions);
        }
    }
}
