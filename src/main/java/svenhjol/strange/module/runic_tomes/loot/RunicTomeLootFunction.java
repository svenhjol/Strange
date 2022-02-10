package svenhjol.strange.module.runic_tomes.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

import javax.annotation.Nullable;
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
        var contextRandom = context.getRandom();
        var random = new Random();
        String runes;

        var knowledge = Knowledge.getKnowledge().orElse(null);
        if (knowledge == null) {
            return stack;
        }

        runes = runesForInterestingLocations(level, random, 0.33F);

        // Process dimensions.
        if (runes == null) {
            var dimension = level.dimension().location();
            if (!RunicTomes.dimensionTomes.containsKey(dimension) || RunicTomes.dimensionTomes.get(dimension).isEmpty()) {
                return null;
            }

            var tomes = RunicTomes.dimensionTomes.get(dimension);
            var tome = tomes.get(contextRandom.nextInt(tomes.size()));
            runes = knowledge.dimensionBranch.get(tome);
        }

        return runes != null ? RunicTomeItem.create(runes) : stack;
    }

    @Nullable
    public static String runesForInterestingLocations(ServerLevel level, Random random, float chance) {
        var discoveries = Discoveries.getDiscoveries().orElse(null);
        if (discoveries == null) return null;

        var x = random.nextInt(1000000) - 500000;
        var z = random.nextInt(1000000) - 500000;
        var pos = new BlockPos(x, 0, z);

        for (Map.Entry<String, Float> entry : RunicTomes.interestingDestinations.entrySet()) {
            if (random.nextFloat() > chance) continue;
            var location = new ResourceLocation(entry.getKey());
            var difficulty = entry.getValue();

            var discovery = DiscoveryHelper.getOrCreate(level, difficulty, pos, random, location, null);
            if (discovery == null) continue;

            return discovery.getRunes();
        }

        return null;
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
