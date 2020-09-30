package svenhjol.strange.scroll.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import svenhjol.strange.base.StrangeIcons;
import svenhjol.strange.scroll.tag.BossTag;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.ArrayList;
import java.util.Map;

public class BossPanel extends Panel {
    public static BossPanel INSTANCE = new BossPanel();

    @Override
    public void render(Screen screen, MatrixStack matrices, QuestTag quest, int mid, int width, int top, int mouseX, int mouseY) {
        BossTag boss = quest.getBoss();
        Map<Identifier, Integer> entities = boss.getEntities();
        Map<Identifier, Integer> killed = boss.getKilled();
        Map<Identifier, Boolean> satisfied = boss.getSatisfied();
        Map<Identifier, String> names = boss.getNames();

        if (entities.isEmpty())
            return;

        // panel title
        drawCenteredTitle(matrices, I18n.translate("gui.strange.scrolls.boss"), mid, top, titleColor);

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
            drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);

            // if all of this type is killed, show a tick next to it
            if (satisfied.containsKey(entityId) && satisfied.get(entityId))
                renderIcon(matrices, StrangeIcons.ICON_TICK, mid - 50, baseTop - 1);

            baseTop += rowHeight;
        }
    }
}
