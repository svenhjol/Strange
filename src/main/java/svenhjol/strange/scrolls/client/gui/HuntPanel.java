package svenhjol.strange.scrolls.client.gui;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import svenhjol.strange.scrolls.client.QuestIcons;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.action.Hunt;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public class HuntPanel extends BasePanel
{
    public HuntPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, width);

        List<Condition<Hunt>> actions = quest.getCriteria().getConditions(Hunt.class);

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
            EntityType<?> entity = Registry.ENTITY_TYPE.getOrDefault(target);
            blitItemIcon(mid - 72, y, "iron_sword");
            this.drawString(fonts, entity.getName().getFormattedText(), mid + 4, y, primaryTextColor);

            // show tick if complete
            if (remaining == 0) blitIcon(mid - 8, y, QuestIcons.ICON_TICK);
        }
    }
}
