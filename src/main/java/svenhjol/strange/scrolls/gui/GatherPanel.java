package svenhjol.strange.scrolls.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.base.StrangeIcons;
import svenhjol.strange.scrolls.tag.Gather;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.ArrayList;
import java.util.Map;

public class GatherPanel extends Panel {
    public static GatherPanel INSTANCE = new GatherPanel();

    @Override
    public void render(Screen screen, MatrixStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY) {
        Gather gather = quest.getGather();
        Map<ItemStack, Integer> items = gather.getItems();
        Map<ItemStack, Boolean> satisfied = gather.getSatisfied();
        if (items.isEmpty())
            return;

        // panel title and icon
        TranslatableText titleText = new TranslatableText("gui.strange.scrolls.gather");
        drawTextWithShadow(matrices, getTextRenderer(), titleText, mid - 44, top, titleColor);
        renderIcon(matrices, StrangeIcons.ICON_WHEAT, mid - 60, top - 1);

        top += rowHeight;

        // render out items with icon, name and quantity
        ArrayList<ItemStack> stacks = new ArrayList<>(items.keySet());

        // names
        int baseTop = top;
        for (ItemStack stack : stacks) {
            int count = items.get(stack);
            TranslatableText text = new TranslatableText("gui.strange.scrolls.gather_item", stack.getName(), count);
            renderItemStack(stack, mid - 60, baseTop - 5);
            drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);

            // show task satisfaction status
            if (satisfied.containsKey(stack) && satisfied.get(stack))
                renderIcon(matrices, StrangeIcons.ICON_TICK, mid - 30 + getTextRenderer().getWidth(text), baseTop - 1);

            baseTop += rowHeight;
        }

        // tooltips
        baseTop = top;
        for (ItemStack stack : stacks) {
            if (mouseX > mid - 60 && mouseX < mid - 44 && mouseY > baseTop - 5 && mouseY < baseTop + 11)
                screen.renderTooltip(matrices, screen.getTooltipFromItem(stack), mouseX, mouseY);

            baseTop += rowHeight;
        }
    }
}
