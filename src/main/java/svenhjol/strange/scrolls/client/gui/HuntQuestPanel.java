package svenhjol.strange.scrolls.client.gui;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.action.Action;
import svenhjol.strange.scrolls.quest.action.Hunt;

import java.util.List;

public class HuntQuestPanel extends BaseQuestPanel
{
    public HuntQuestPanel(IQuest quest, int width, int x, int y)
    {
        super(quest, width);
        int mid = x + (width / 2);
        int hpad = 8;

        List<Action<Hunt>> actions = quest.getCriteria().getActions(Hunt.class);

        if (!actions.isEmpty()) {

            // draw background rect
            int x0 = mid - 100;
            int y0 = y;
            int x1 = mid + 100;
            int y1 = y + 16 + (actions.size() * 16) + hpad;
            AbstractGui.fill(x0, y0, x1, y1, 0x88000000);

            // draw title
            y += hpad; // space above title
            this.drawCenteredString(fonts, "Hunt", mid, y, primaryTextColor);
            y += 16; // space between title and item list

            for (int i = 0; i < actions.size(); i++) {
                y += i * 16; // height of each line

                Hunt hunt = actions.get(i).getDelegate();
                ResourceLocation target = hunt.getTarget();
                int remaining = hunt.getCount() - hunt.getKilled();

                // draw remaining count and item icon
                this.drawRightAlignedString(fonts, String.valueOf(remaining), mid - 30, y, primaryTextColor);
//                items.renderItemIntoGUI(stack, mid - 14, y - 5);

                EntityType<?> entity = Registry.ENTITY_TYPE.getOrDefault(target);

                this.drawString(fonts, entity.getName().getFormattedText(), mid + 4, y, primaryTextColor);
            }
        }
    }
}
