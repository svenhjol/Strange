package svenhjol.strange.client.gui.panel;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import svenhjol.strange.StrangeIcons;
import svenhjol.strange.scroll.tag.Boss;
import svenhjol.strange.scroll.tag.Quest;

import java.util.ArrayList;
import java.util.Map;

public class BossPanel extends BasePanel {
    public static BossPanel INSTANCE = new BossPanel();

    @Override
    public void render(Screen screen, MatrixStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY) {
        Boss boss = quest.getBoss();
        Map<Identifier, Integer> entities = boss.getEntities();
        Map<Identifier, Integer> killed = boss.getKilled();
        Map<Identifier, Boolean> satisfied = boss.getSatisfied();
        Map<Identifier, String> names = boss.getNames();

        if (entities.isEmpty())
            return;

        // panel title and icon
        TranslatableText titleText = new TranslatableText("gui.strange.scrolls.boss");
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
            TranslatableText text = new TranslatableText("gui.strange.scrolls.boss_entity", name, remaining);
            renderItemStack(new ItemStack(Items.DIAMOND_SWORD), mid - 60, baseTop - 5);
            drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);

            // if all of this type is killed, show a tick next to it
            if (satisfied.containsKey(entityId) && satisfied.get(entityId))
                renderIcon(matrices, StrangeIcons.ICON_TICK, mid - 30 + getTextRenderer().getWidth(text), baseTop - 1);

            baseTop += rowHeight;
        }
    }
}
