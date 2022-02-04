package svenhjol.strange.module.runic_tomes.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.function.TriFunction;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.RunicTomes;

import java.util.Random;

public class RunicTomeLootFunction extends LootItemConditionalFunction {
    public RunicTomeLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(RunicTomes.class)) return stack;
        ServerLevel level = context.getLevel();
        Random random = context.getRandom();
        String runes = null;

        var knowledge = Knowledge.getKnowledge().orElse(null);
        if (knowledge == null) {
            return stack;
        }

        for (TriFunction<KnowledgeData, Level, Random, String> callback : RunicTomes.RUNIC_TOME_LOOT_CALLBACKS) {
            runes = callback.apply(knowledge, level, random);
            if (runes != null) break;
        }

        return runes != null ? RunicTomeItem.create(runes) : stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return RunicTomes.RUNIC_TOME_LOOT;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RunicTomeLootFunction> {
        @Override
        public RunicTomeLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new RunicTomeLootFunction(conditions);
        }
    }
}
