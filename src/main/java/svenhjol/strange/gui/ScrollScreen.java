package svenhjol.strange.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.gui.panel.RewardPanel;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.scroll.ScrollQuest;

public class ScrollScreen extends Screen {
    private ScrollQuest quest;

    public ScrollScreen(ScrollQuest quest) {
        super(new TranslatableText(quest.getTitle()));
        this.quest = quest;
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

        GuiHelper.drawCenteredTitle(matrices, title.getString(), mid, 84);
        RewardPanel.render(this, matrices, quest, mid, width, 94, mouseX, mouseY);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderButtons() {
        int y = (height / 4) + 160;
        int w = 80;
        int h = 20;

        this.addButton(new ButtonWidget(width / 2, y, w, h, new TranslatableText("gui.strange.scrolls.close"), this::close));
    }

    private void close(ButtonWidget button) {
        if (client != null)
            client.openScreen(null);
    }
}
