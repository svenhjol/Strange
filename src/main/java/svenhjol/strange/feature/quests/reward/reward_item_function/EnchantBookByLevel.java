package svenhjol.strange.feature.quests.reward.reward_item_function;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardItemFunction;

public class EnchantBookByLevel implements RewardItemFunction {
    public static final String ID = "enchant_book_by_level";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void apply(RewardItem reward) {
        var quest = reward.quest;
        var stack = reward.stack;
        var random = quest.random();

        var chance = Math.min(1.0d, quest.villagerLevel() * 0.25d);
        var enchantLevel = quest.villagerLevel() * 6;
        var allowTreasure = random.nextDouble() < enchantLevel * 0.15d;

        if (random.nextDouble() < chance
            && !stack.isEnchanted()
            && (stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK))) {
            EnchantmentHelper.enchantItem(random, stack, enchantLevel, allowTreasure);
        }
    }
}
