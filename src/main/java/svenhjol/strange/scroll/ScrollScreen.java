package svenhjol.strange.scroll;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.scroll.gui.*;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.ArrayList;
import java.util.List;

public class ScrollScreen extends Screen {
    private final QuestTag quest;

    public ScrollScreen(QuestTag quest) {
        super(new TranslatableText(quest.getTitle()));
        this.quest = quest;
        this.passEvents = true;
    }

    @Override
    protected void init() {
        super.init();
        renderButtons();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int mid = width / 2;

        if (client == null || client.world == null)
            return;

        renderBackground(matrices);

        // draw the title at the top of the page
        GuiHelper.drawCenteredTitle(matrices, title.getString(), mid, 12, 0xFFFF88);

        // render scroll requirements panels
        List<Panel> panels = new ArrayList<>();

        if (quest.getGather().getItems().size() > 0)
            panels.add(GatherPanel.INSTANCE);

        if (quest.getHunt().getEntities().size() > 0)
            panels.add(HuntPanel.INSTANCE);

        if (quest.getExplore().getItems().size() > 0)
            panels.add(ExplorePanel.INSTANCE);

        if (quest.getBoss().getEntities().size() > 0)
            panels.add(BossPanel.INSTANCE);

        int panelTop = 48;
        int panelMid;
        int panelWidth;

        for (int i = 0; i < panels.size(); i++) {
            int ii = (i * 2) + 1;
            panelMid = (ii * (width / (panels.size() * 2)));
            panelWidth = width / ii;

            Panel panel = panels.get(i);
            panel.render(this, matrices, quest, panelMid, panelWidth, panelTop, mouseX, mouseY);
        }

        // render scroll reward panel
        RewardPanel.INSTANCE.render(this, matrices, quest, mid, width, 128, mouseX, mouseY);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderButtons() {
        int y = (height / 4) + 160;
        int w = 80;
        int h = 20;

        this.addButton(new ButtonWidget((width/2) - (w/2), y, w, h, new TranslatableText("gui.strange.scrolls.close"), this::close));
    }

    private void close(ButtonWidget button) {
        if (client != null)
            client.openScreen(null);
    }
}
