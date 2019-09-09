package svenhjol.strange.scrolls.client.gui;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.action.Action;
import svenhjol.strange.scrolls.quest.action.Hunt;

import java.util.List;

public class HuntQuestPanel extends BasePanel
{
    public HuntQuestPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, width);

        List<Action<Hunt>> actions = quest.getCriteria().getActions(Hunt.class);

        if (actions.isEmpty()) return;
        drawBackground(mid - width, mid + width, y, y + 16 + (actions.size() * 16) + pad);

        // draw title
        y += pad; // space above title
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
