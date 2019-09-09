package svenhjol.strange.scrolls.client.gui;

import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.condition.Condition;
import svenhjol.strange.scrolls.quest.condition.Time;

import java.util.List;

public class ConditionsPanel extends BasePanel
{
    public ConditionsPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, width);

        final List<Condition<?>> conditions = quest.getCriteria().getConditions();

        if (conditions.isEmpty()) return;
        drawBackground(mid - (width / 2) + 10, mid + (width / 2) - 10, y, y + 30);
        y += pad;

        for (Condition<?> condition : conditions) {
            if (condition.getDelegate() instanceof Time) {
                // render time row
                Time time = (Time)condition.getDelegate();
                String out = "";

                final long remaining = (time.getRemaining() / 20);

                if (remaining > 60) {
                    int mins = (int) Math.floor((float)remaining / 60);
                    int secs = (int)(remaining % 60);

                    final String minsStr = String.valueOf(mins);
                    final String secsStr = (secs < 10 ? "0" : "") + secs;

                    out = "Complete within " + minsStr + ":" + secsStr;
                } else {
                    out = "Complete within " + remaining + " seconds";
                }
                this.drawString(fonts, out, mid - 60, y, primaryTextColor);
            }
        }
    }
}
