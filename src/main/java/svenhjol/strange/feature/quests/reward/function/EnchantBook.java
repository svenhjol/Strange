package svenhjol.strange.feature.quests.reward.function;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardItemFunction;
import svenhjol.strange.feature.quests.reward.RewardItemFunctionParameters;

public class EnchantBook implements RewardItemFunction {
    public static final String ID = "enchant_book";

    private EnchantBookParameters params;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public RewardItemFunction withParameters(RewardItemFunctionParameters params) {
        this.params = new EnchantBookParameters(params);
        return this;
    }

    @Override
    public void apply(RewardItem reward) {
        var quest = reward.quest;
        var stack = reward.stack;
        var random = quest.random();

        var enchantLevel = quest.villagerLevel() * 6;

        if (random.nextDouble() < params.chance
            && !stack.isEnchanted()
            && (stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK))) {
            EnchantmentHelper.enchantItem(random, stack, enchantLevel, params.allowTreasure);
        }
    }

    public static class EnchantBookParameters {
        public final double chance;
        public final boolean allowTreasure;

        public EnchantBookParameters(RewardItemFunctionParameters params) {
            this.chance = params.getDouble("chance", 0.5d);
            this.allowTreasure = params.getBoolean("allow_treasure", false);
        }
    }
}
