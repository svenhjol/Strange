package svenhjol.strange.module.runic_tomes.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.floating_islands_dimension.FloatingIslandsDimension;
import svenhjol.strange.module.mirror_dimension.MirrorDimension;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.RunicTomes;

import java.util.Random;

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

        if (random.nextFloat() > 0.66F) {
            return stack;
        }

        if (DimensionHelper.isNether(level)) {
            dimension = DimensionHelper.getDimension(Level.NETHER);
        } else if (DimensionHelper.isEnd(level)) {

            // in the End you can find tomes for overworld, nether and end
            ResourceKey<Level> key;
            float f = random.nextFloat();

            if (f < 0.33F) {
                key = Level.END;
            } else if (f < 0.66F) {
                key = Level.NETHER;
            } else {
                key = Level.OVERWORLD;
            }
            dimension =  DimensionHelper.getDimension(key);

        } else if (Strange.LOADER.isEnabled(Dimensions.class)) {

            // limit the dimension tomes to their respective dimensions
            if (DimensionHelper.isDimension(level, MirrorDimension.ID)) {
                dimension = random.nextBoolean() ? MirrorDimension.ID : Level.OVERWORLD.location();
            } else if (DimensionHelper.isDimension(level, FloatingIslandsDimension.ID)) {
                dimension = random.nextBoolean() ? FloatingIslandsDimension.ID : Level.OVERWORLD.location();
            }
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
