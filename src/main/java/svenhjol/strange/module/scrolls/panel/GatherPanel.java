package svenhjol.strange.module.scrolls.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.init.StrangeIcons;
import svenhjol.strange.module.scrolls.nbt.Gather;
import svenhjol.strange.module.scrolls.nbt.Quest;

import java.util.ArrayList;
import java.util.Map;

public class GatherPanel extends BasePanel {
    public static GatherPanel INSTANCE = new GatherPanel();

    @Override
    public void render(Screen screen, PoseStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY) {
        Gather gather = quest.getGather();
        Map<ItemStack, Integer> items = gather.getItems();
        Map<ItemStack, Boolean> satisfied = gather.getSatisfied();
        if (items.isEmpty())
            return;

        // panel title and icon
        TranslatableComponent titleText = new TranslatableComponent("gui.strange.scrolls.gather");
        drawString(matrices, getTextRenderer(), titleText, mid - 44, top, titleColor);
        renderIcon(matrices, StrangeIcons.ICON_WHEAT, mid - 60, top - 1);

        top += rowHeight;

        // render out items with icon, name and quantity
        ArrayList<ItemStack> stacks = new ArrayList<>(items.keySet());

        // names
        int baseTop = top;
        for (ItemStack stack : stacks) {
            int count = items.get(stack);
            TranslatableComponent text = new TranslatableComponent("gui.strange.scrolls.gather_item", stack.getHoverName(), count);
            renderItemStack(stack, mid - 60, baseTop - 5);
            drawString(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);

            // show task satisfaction status
            if (satisfied.containsKey(stack) && satisfied.get(stack))
                renderIcon(matrices, StrangeIcons.ICON_TICK, mid - 30 + getTextRenderer().width(text), baseTop - 1);

            baseTop += rowHeight;
        }

        // tooltips
        baseTop = top;
        for (ItemStack stack : stacks) {
            if (mouseX > mid - 60 && mouseX < mid - 44 && mouseY > baseTop - 5 && mouseY < baseTop + 11)
                screen.renderComponentTooltip(matrices, screen.getTooltipFromItem(stack), mouseX, mouseY);

            baseTop += rowHeight;
        }
    }
}
