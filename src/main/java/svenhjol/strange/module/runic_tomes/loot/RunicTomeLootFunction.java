package svenhjol.strange.module.runic_tomes.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.strange.Strange;
import svenhjol.strange.module.discoveries.Discoveries;
import svenhjol.strange.module.discoveries.DiscoveryHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.RunicTomes;

import java.util.Map;
import java.util.Random;

public class RunicTomeLootFunction extends LootItemConditionalFunction {
    public RunicTomeLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!Strange.LOADER.isEnabled(RunicTomes.class)) return stack;
        var level = context.getLevel();
        var random = context.getRandom();
        var random1 = new Random();
        String runes = null;

        var knowledge = Knowledge.getKnowledge().orElse(null);
        if (knowledge == null) {
            return stack;
        }

        // Process interesting locations.
        var discoveries = Discoveries.getDiscoveries().orElse(null);
        if (discoveries == null) {
            return stack;
        }

        var x = random1.nextInt(1000000) - 500000;
        var z = random1.nextInt(1000000) - 500000;
        var pos = new BlockPos(x, 0, z);

        for (Map.Entry<String, Float> entry : RunicTomes.interestingDestinations.entrySet()) {
            if (random1.nextFloat() > 0.33F) continue;
            var location = new ResourceLocation(entry.getKey());
            var difficulty = entry.getValue();

            var discovery = DiscoveryHelper.getOrCreate(difficulty, level.dimension().location(), pos, random, location, null);
            if (discovery == null) continue;

            runes = discovery.getRunes();
            break;
        }

        // Process dimensions.
        if (runes == null) {
            var dimension = level.dimension().location();
            if (!RunicTomes.dimensionTomes.containsKey(dimension) || RunicTomes.dimensionTomes.get(dimension).isEmpty()) {
                return null;
            }

            var tomes = RunicTomes.dimensionTomes.get(dimension);
            var tome = tomes.get(random.nextInt(tomes.size()));
            runes = knowledge.dimensionBranch.get(tome);
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
