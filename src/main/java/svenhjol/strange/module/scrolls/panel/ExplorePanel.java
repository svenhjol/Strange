package svenhjol.strange.module.scrolls.panel;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.init.StrangeIcons;
import svenhjol.strange.module.scrolls.tag.Explore;
import svenhjol.strange.module.scrolls.tag.Quest;

import java.util.List;
import java.util.Map;

public class ExplorePanel extends BasePanel {
    public static ExplorePanel INSTANCE = new ExplorePanel();

    @Override
    public void render(Screen screen, MatrixStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY) {
        Explore explore = quest.getExplore();
        List<ItemStack> stacks = explore.getItems();
        Map<Item, Boolean> satisfied = explore.getSatisfied();

        // panel title and icon
        TranslatableText titleText = new TranslatableText("gui.strange.scrolls.explore");
        drawTextWithShadow(matrices, getTextRenderer(), titleText, mid - 44, top, titleColor);
        renderIcon(matrices, StrangeIcons.ICON_COMPASS, mid - 60, top - 1);

        top += rowHeight;

        // names
        int baseTop = top;
        for (ItemStack stack : stacks) {
            TranslatableText text = new TranslatableText("gui.strange.scrolls.explore_item", stack.getName());
            renderItemStack(stack, mid - 60, baseTop - 5);
            drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);

            // show task satisfaction status
            if (satisfied.containsKey(stack.getItem()) && satisfied.get(stack.getItem()))
                renderIcon(matrices, StrangeIcons.ICON_TICK, mid - 30 + getTextRenderer().getWidth(text), baseTop - 1);

            baseTop += rowHeight;
        }

        // tooltips
        baseTop = top;
        for (ItemStack stack : stacks) {
            ItemStack stackCopy = stack.copy();
            if (mouseX > mid - 60 && mouseX < mid - 44
                && mouseY > baseTop - 5 && mouseY < baseTop + 11
            ) {
                screen.renderTooltip(matrices, screen.getTooltipFromItem(stackCopy), mouseX, mouseY);
            }
            baseTop += rowHeight;
        }
    }
}
