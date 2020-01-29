package svenhjol.strange.scrolls.quest.panel;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.condition.Provide;
import svenhjol.strange.scrolls.quest.condition.Time;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.List;

public class ConstraintsPanel extends BasePanel
{
    public ConstraintsPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, mid, width);

        final List<Condition<IDelegate>> constraints = quest.getCriteria().getConditions(Criteria.CONSTRAINT);
        if (constraints.isEmpty()) return;

        y += pad; // space above title

        // draw title
        drawCenteredTitle(I18n.format("gui.strange.quests.constraints"), y);

        List<Provide> provides = new ArrayList<>();

        for (Condition constraint : constraints) {

            if (constraint.getDelegate() instanceof Time) {
                Time time = (Time)constraint.getDelegate();
                String out;
                final long remaining = (time.getRemaining() / 20);

                if (remaining > 60) {
                    int mins = (int) Math.floor((float)remaining / 60);
                    int secs = (int)(remaining % 60);

                    final String minsStr = String.valueOf(mins);
                    final String secsStr = (secs < 10 ? "0" : "") + secs;
                    out = I18n.format("gui.strange.quests.complete_within_time", minsStr, secsStr);
                } else {
                    out = I18n.format("gui.strange.quests.complete_within_seconds", remaining);
                }

                y += rowHeight; // space between title and item list
                blitItemIcon(new ItemStack(Items.CLOCK), mid - 90, y - 5);
                this.drawString(fonts, out, mid - 66, y, primaryTextColor);
            }

            if (constraint.getDelegate() instanceof Provide) {
                provides.add((Provide)constraint.getDelegate());
            }
        }

        for (Provide provide : provides) {
            y += rowHeight;

            ItemStack stack = provide.getStack();

            // draw remaining count and item icon
            blitItemIcon(stack, mid - 90, y - 5);
            this.drawString(fonts, I18n.format("gui.strange.quests.provided_items", stack.getDisplayName().getString()), mid - 66, y, primaryTextColor);
        }
    }
}
