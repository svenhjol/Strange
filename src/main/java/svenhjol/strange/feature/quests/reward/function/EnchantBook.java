package svenhjol.strange.feature.quests.reward.function;

import net.minecraft.world.item.ItemStack;
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

        if (stack.is(Items.ENCHANTED_BOOK)) {
            reward.stack = EnchantmentHelper.enchantItem(random, new ItemStack(Items.BOOK, stack.getCount()),
                params.enchantLevel, params.allowTreasure);
        }
    }

    public static class EnchantBookParameters {
        public final boolean allowTreasure;
        public final int enchantLevel;

        public EnchantBookParameters(RewardItemFunctionParameters params) {
            this.allowTreasure = params.getBoolean("allow_treasure", false);
            this.enchantLevel = params.getInteger("level",
                params.functionDefinition().questDefinition().level() * 6);
        }
    }
}
