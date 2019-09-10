package svenhjol.strange.scrolls.client.gui;

import net.minecraft.item.ItemStack;
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
        super(quest, width);

        final List<Condition<ICondition>> rewards = quest.getCriteria().getConditions("reward");
        if (rewards.isEmpty()) return;

        drawBackground(mid - width, mid + width, y, y + rewards.size() * 20);
        y += pad;

        for (int i = 0; i < rewards.size(); i++) {
            y += i * 16; // height of each line

            Condition<?> reward = rewards.get(i);

            if (reward.getDelegate() instanceof XP) {
                // render XP row
                XP xp = (XP)reward.getDelegate();
                final int amount = xp.getAmount();
                String out = amount + " XP";

                blitItemIcon(mid - 72, y, "experience_bottle");
                this.drawString(fonts, out, mid - 60, y, primaryTextColor);
            }

            if (reward.getDelegate() instanceof RewardItem) {
                // render item
                RewardItem rewardItem = (RewardItem)reward.getDelegate();
                ItemStack stack = rewardItem.getStack();
                items.renderItemIntoGUI(stack, mid - 14, y - 5);
                this.drawString(fonts, stack.getDisplayName().getString(), mid + 4, y, primaryTextColor);
            }
        }
    }
}
