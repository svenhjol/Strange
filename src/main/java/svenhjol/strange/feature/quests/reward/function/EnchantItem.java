package svenhjol.strange.feature.quests.reward.function;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardItemFunction;
import svenhjol.strange.feature.quests.reward.RewardItemFunctionParameters;

public class EnchantItem implements RewardItemFunction {
    public static final String ID = "enchant_item";

    private EnchantItemParameters params;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public RewardItemFunction withParameters(RewardItemFunctionParameters params) {
        this.params = new EnchantItemParameters(params);
        return this;
    }

    @Override
    public void apply(RewardItem reward) {
        var quest = reward.quest;
        var stack = reward.stack;
        var random = quest.random();

        if (random.nextDouble() < params.chance && stack.isEnchantable()) {
            EnchantmentHelper.enchantItem(random, stack, params.enchantLevel, params.allowTreasure);
        }
    }

    public static class EnchantItemParameters {
        public final double chance;
        public final int enchantLevel;
        public final boolean allowTreasure;

        public EnchantItemParameters(RewardItemFunctionParameters params) {
            var definition = params.functionDefinition().questDefinition();
            var defaultEnchantLevel = definition.level() * 6;

            this.chance = params.getDouble("chance", 0.5d);
            this.enchantLevel = params.getInteger("level", defaultEnchantLevel);
            this.allowTreasure = params.getBoolean("allow_treasure", false);
        }
    }
}
