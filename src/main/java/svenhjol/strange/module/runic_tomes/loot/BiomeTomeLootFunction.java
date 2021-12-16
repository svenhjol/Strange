package svenhjol.strange.module.runic_tomes.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.RunicTomes;

import java.util.List;
import java.util.Random;

public class BiomeTomeLootFunction extends LootItemConditionalFunction {
    public BiomeTomeLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(RunicTomes.class)) return stack;
        Random random = context.getRandom();
        KnowledgeData knowledge = Knowledge.getKnowledgeData().orElse(null);

        if (knowledge == null) {
            return stack;
        }

        if (random.nextFloat() > 0.75F) {
            return stack;
        }

        List<String> keys = knowledge.biomes.keys();
        if (!keys.isEmpty()) {
            String runes = keys.get(random.nextInt(keys.size()));
            return RunicTomeItem.create(runes);
        }

        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return RunicTomes.BIOME_TOME_LOOT;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<BiomeTomeLootFunction> {
        @Override
        public BiomeTomeLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new BiomeTomeLootFunction(conditions);
        }
    }
}
