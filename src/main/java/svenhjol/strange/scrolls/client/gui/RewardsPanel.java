package svenhjol.strange.scrolls.client.gui;

import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.reward.XP;

import java.util.List;

public class RewardsPanel extends BasePanel
{
    public RewardsPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, width);

        final List<Condition<?>> rewards = quest.getCriteria().getRewards();

        if (rewards.isEmpty()) return;

        drawBackground(mid - width, mid + width, y, y + rewards.size() * 20);
        y += pad;

        for (Condition<?> reward : rewards) {
            if (reward.getDelegate() instanceof XP) {
                // render XP row
                XP xp = (XP)reward.getDelegate();
                final int amount = xp.getAmount();
                String out = amount + " XP";

                blitItemIcon(mid - 72, y, "experience_bottle");
                this.drawString(fonts, out, mid - 60, y, primaryTextColor);
            }
        }
    }
}
