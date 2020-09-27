package svenhjol.strange.scroll.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.scroll.tag.ExploreTag;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.List;

public class ExplorePanel extends Panel {
    public static ExplorePanel INSTANCE = new ExplorePanel();

    @Override
    public void render(Screen screen, MatrixStack matrices, QuestTag quest, int mid, int width, int top, int mouseX, int mouseY) {
        ExploreTag explore = quest.getExplore();
        List<ItemStack> stacks = explore.getItems();

        // the panel title
        drawCenteredTitle(matrices, I18n.translate("gui.strange.scrolls.explore"), mid, top, titleColor);

        top += rowHeight;

        // names
        int baseTop = top;
        for (ItemStack stack : stacks) {
            TranslatableText text = new TranslatableText("gui.strange.scrolls.explore_item", stack.getName());
            renderItemStack(stack, mid - 60, baseTop - 5);
            drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);
        }

        // tooltips
        baseTop = top;
        for (ItemStack stack : stacks) {
            if (mouseX > mid - 60 && mouseX < mid - 44
                && mouseY > baseTop - 5 && mouseY < baseTop + 11
            ) {
                screen.renderTooltip(matrices, screen.getTooltipFromItem(stack), mouseX, mouseY);
            }
            baseTop += rowHeight;
        }
    }
}
