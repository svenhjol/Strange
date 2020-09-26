package svenhjol.strange.scroll;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.scroll.gui.RewardPanel;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.scroll.tag.QuestTag;

public class ScrollScreen extends Screen {
    private final QuestTag quest;

    public ScrollScreen(QuestTag quest) {
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

        // draw the title at the top of the page
        GuiHelper.drawCenteredTitle(matrices, title.getString(), mid, 12);

        // render out the panels
        RewardPanel.INSTANCE.render(this, matrices, quest, mid, width, 120, mouseX, mouseY);


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
