package svenhjol.strange.scrolls.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.action.Action;
import svenhjol.strange.scrolls.quest.action.Gather;

import java.util.List;

public class QuestGatherGui extends AbstractGui
{
    public QuestGatherGui(IQuest quest, Minecraft mc, int width)
    {
        FontRenderer font = mc.fontRenderer;
        ItemRenderer item = mc.getItemRenderer();

        List<Action<Gather>> gather = quest.getCriteria().getActions(Gather.class);

        if (!gather.isEmpty()) {
            this.drawRightAlignedString(font, "You must gather:", (width / 2) - 40, 85, 16777215);
            for (int i = 0; i < gather.size(); i++) {

                Gather del = gather.get(i).getDelegate();
                ItemStack stack = del.getStack();

                int count = del.getCount() - del.getCollected();
                item.renderItemIntoGUI(stack, (width / 2) + (i * 50), 80);
                this.drawString(font, " x" + count, (width / 2) + (i * 50) + 14, 85, 16777215);

                if (count == 0) {
                    this.drawCenteredString(font, "You have collected all the required " + stack.getTranslationKey() + ".", 60, 95 + (i * 15), 0xFFFFFF);
                }
            }
        }
    }
}
