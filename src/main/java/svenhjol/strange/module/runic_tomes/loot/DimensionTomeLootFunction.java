package svenhjol.strange.module.runic_tomes.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.RunicTomes;

import java.util.Random;
import java.util.function.BiFunction;

public class DimensionTomeLootFunction extends LootItemConditionalFunction {
    public DimensionTomeLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(RunicTomes.class)) return stack;
        ServerLevel level = context.getLevel();
        Random random = context.getRandom();
        ResourceLocation dimension = null;

        var knowledge = Knowledge.getKnowledge().orElse(null);
        if (knowledge == null) {
            return stack;
        }

        for (BiFunction<Level, Random, ResourceLocation> callback : RunicTomes.DIMENSION_TOME_LOOT_CALLBACKS) {
            var result = callback.apply(level, random);
            if (result == null) continue;
            dimension = result;
            break;
        }

        if (dimension == null) {
            return stack;
        }

        String runes = knowledge.dimensionBranch.get(dimension);
        return runes != null ? RunicTomeItem.create(runes) : stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return RunicTomes.DIMENSION_TOME_LOOT;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<DimensionTomeLootFunction> {
        @Override
        public DimensionTomeLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new DimensionTomeLootFunction(conditions);
        }
    }
}
