package svenhjol.strange.scrolls.client.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.iface.ICondition;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.reward.RewardItem;
import svenhjol.strange.scrolls.quest.reward.XP;

import java.util.List;

public class RewardsPanel extends BasePanel
{
    public RewardsPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, mid, width);

        final List<Condition<ICondition>> rewards = quest.getCriteria().getConditions("reward");
        if (rewards.isEmpty()) return;

        y += pad; // space above title

        // draw title
        drawCenteredTitle("Rewards", y);
        y += 18; // space between title and item list

        for (int i = 0; i < rewards.size(); i++) {
            y += i * 18; // height of each line

            Condition<?> reward = rewards.get(i);

            if (reward.getDelegate() instanceof XP) {
                // render XP row
                XP xp = (XP)reward.getDelegate();
                final int amount = xp.getAmount();
                String out = amount + " experience";

                blitItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE), mid - 60, y - 5);
                this.drawString(fonts, out, mid - 36, y, primaryTextColor);
            }

            if (reward.getDelegate() instanceof RewardItem) {
                // render item
                RewardItem rewardItem = (RewardItem)reward.getDelegate();
                ItemStack stack = rewardItem.getStack();
                items.renderItemIntoGUI(stack, mid - 60, y - 5);
                this.drawString(fonts, stack.getDisplayName().getString(), mid - 40, y, primaryTextColor);
            }
        }
    }
}
