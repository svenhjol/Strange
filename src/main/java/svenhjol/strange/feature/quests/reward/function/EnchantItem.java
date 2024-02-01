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

        var enchantLevel = quest.villagerLevel() * 6;

        if (random.nextDouble() < params.chance && stack.isEnchantable()) {
            EnchantmentHelper.enchantItem(random, stack, enchantLevel, params.allowTreasure);
        }
    }

    public static class EnchantItemParameters {
        public final double chance;
        public final boolean allowTreasure;

        public EnchantItemParameters(RewardItemFunctionParameters params) {
            this.chance = params.getDouble("chance", 0.5d);
            this.allowTreasure = params.getBoolean("allow_treasure", false);
        }
    }
}
