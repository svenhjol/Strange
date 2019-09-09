package svenhjol.strange.scrolls.client.gui;

import net.minecraft.item.ItemStack;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.action.Action;
import svenhjol.strange.scrolls.quest.action.Gather;

import java.util.List;

public class GatherQuestPanel extends BasePanel
{
    public GatherQuestPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, width);

        List<Action<Gather>> actions = quest.getCriteria().getActions(Gather.class);

        if (actions.isEmpty()) return;
        drawBackground(mid - width, mid + width, y, y + 16 + (actions.size() * 16) + pad);

        // draw title
        y += pad; // space above title
        this.drawCenteredString(fonts, "Gather", mid, y, primaryTextColor);
        y += 16; // space between title and item list

        for (int i = 0; i < actions.size(); i++) {
            y += i * 16; // height of each line

            Gather gather = actions.get(i).getDelegate();
            ItemStack stack = gather.getStack();
            int remaining = gather.getCount() - gather.getCollected();

            // draw remaining count and item icon
            this.drawRightAlignedString(fonts, String.valueOf(remaining), mid - 30, y, primaryTextColor);
            items.renderItemIntoGUI(stack, mid - 14, y - 5);
            this.drawString(fonts, stack.getDisplayName().getString(), mid + 4, y, primaryTextColor);
        }
    }
}
