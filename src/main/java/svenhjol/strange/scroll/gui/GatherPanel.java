package svenhjol.strange.scroll.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.scroll.BasePanel;
import svenhjol.strange.scroll.tag.GatherTag;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.ArrayList;
import java.util.Map;

public class GatherPanel extends BasePanel {
    public static GatherPanel INSTANCE = new GatherPanel();

    @Override
    public void render(Screen screen, MatrixStack matrices, QuestTag quest, int mid, int width, int top, int mouseX, int mouseY) {
        GatherTag gather = quest.getGather();
        Map<ItemStack, Integer> items = gather.getItems();
        if (items.isEmpty())
            return; // really not ideal, should be caught earlier than this

        // the panel title
        drawCenteredTitle(matrices, I18n.translate("gui.strange.scrolls.gather"), mid, top, titleColor);

        top += rowHeight;

        // render out items with icon, name and quantity
        ArrayList<ItemStack> stacks = new ArrayList<>(items.keySet());

        // names
        int baseTop = top;
        for (ItemStack stack : stacks) {
            int count = items.get(stack);
            TranslatableText text = new TranslatableText("gui.strange.scrolls.gather_item", stack.getName(), count);
            renderItemIcon(stack, mid - 60, baseTop - 5);
            drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);
            baseTop += rowHeight;
        }

        // tooltips
        baseTop = top;
        for (ItemStack stack : stacks) {
            if (stack.hasGlint()
                && mouseX > mid - 60 && mouseX < mid - 44
                && mouseY > baseTop - 5 && mouseY < baseTop + 11
            ) {
                screen.renderTooltip(matrices, screen.getTooltipFromItem(stack), mouseX, mouseY);
            }
            baseTop += rowHeight;
        }
    }
}
