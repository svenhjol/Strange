package svenhjol.strange.scroll.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import svenhjol.strange.scroll.tag.HuntTag;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.ArrayList;
import java.util.Map;

public class HuntPanel extends Panel {
    public static HuntPanel INSTANCE = new HuntPanel();

    @Override
    public void render(Screen screen, MatrixStack matrices, QuestTag quest, int mid, int width, int top, int mouseX, int mouseY) {
        HuntTag hunt = quest.getHunt();
        Map<Identifier, Integer> entities = hunt.getEntities();
        Map<Identifier, Boolean> satisfied = hunt.getSatisfied();
        if (entities.isEmpty())
            return; // should be caught earlier than this

        // the panel title
        drawCenteredTitle(matrices, I18n.translate("gui.strange.scrolls.hunt"), mid, top, titleColor);

        top += rowHeight;

        // render out entities with name and quantity
        ArrayList<Identifier> entityIds = new ArrayList<>(entities.keySet());

        // names
        int baseTop = top;
        for (Identifier entityId : entityIds) {
            int count = entities.get(entityId);
        }
    }
}
