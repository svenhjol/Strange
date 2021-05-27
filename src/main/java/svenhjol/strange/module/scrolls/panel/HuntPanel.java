package svenhjol.strange.module.scrolls.panel;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import svenhjol.strange.init.StrangeIcons;
import svenhjol.strange.module.scrolls.tag.Hunt;
import svenhjol.strange.module.scrolls.tag.Quest;

import java.util.ArrayList;
import java.util.Map;

public class HuntPanel extends BasePanel {
    public static HuntPanel INSTANCE = new HuntPanel();

    @Override
    public void render(Screen screen, MatrixStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY) {
        Hunt hunt = quest.getHunt();

        Map<Identifier, Integer> entities = hunt.getEntities();
        Map<Identifier, Integer> killed = hunt.getKilled();
        Map<Identifier, Boolean> satisfied = hunt.getSatisfied();
        Map<Identifier, String> names = hunt.getNames();

        if (entities.isEmpty())
            return;

        // panel title and icon
        TranslatableText titleText = new TranslatableText("gui.strange.scrolls.hunt");
        drawTextWithShadow(matrices, getTextRenderer(), titleText, mid - 44, top, titleColor);
        renderIcon(matrices, StrangeIcons.ICON_SWORD, mid - 60, top - 1);

        top += rowHeight;

        // render out entities with name and quantity
        ArrayList<Identifier> entityIds = new ArrayList<>(entities.keySet());

        // names
        int baseTop = top;
        for (Identifier entityId : entityIds) {
            // remaining is count - killed
            int remaining = Math.max(0, entities.get(entityId) - killed.getOrDefault(entityId, 0));
            String name = names.get(entityId);
            TranslatableText text = new TranslatableText("gui.strange.scrolls.hunt_entity", name, remaining);
            renderItemStack(new ItemStack(Items.STONE_SWORD), mid - 60, baseTop - 5);
            drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);

            // show task satisfaction status
            if (satisfied.get(entityId) != null && satisfied.get(entityId))
                renderIcon(matrices, StrangeIcons.ICON_TICK, mid - 30 + getTextRenderer().getWidth(text), baseTop - 1);

            baseTop += rowHeight;
        }
    }
}
