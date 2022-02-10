package svenhjol.strange.module.vaults.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.RunicTomes;
import svenhjol.strange.module.runic_tomes.loot.RunicTomeLootFunction;
import svenhjol.strange.module.vaults.Vaults;

import java.util.Random;

public class VaultLibraryLootFunction extends LootItemConditionalFunction {
    public VaultLibraryLootFunction(LootItemCondition[] conditions) {
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

        runes = RunicTomeLootFunction.runesForInterestingLocations(level, random, 0.23F);

        if (runes == null) {
            if (DimensionHelper.isOverworld(level)) {
                var dimension = random.nextBoolean() ? Level.OVERWORLD.location() : Level.NETHER.location();
                runes = knowledge.dimensionBranch.get(dimension);
            } else {
                var dimensions = knowledge.dimensionBranch.keys();
                runes = dimensions.get(random.nextInt(dimensions.size()));
            }
        }

        return runes != null ? RunicTomeItem.create(runes) : stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return Vaults.LIBRARY_LOOT;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<VaultLibraryLootFunction> {
        @Override
        public VaultLibraryLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new VaultLibraryLootFunction(conditions);
        }
    }
}
