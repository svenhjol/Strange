package svenhjol.strange.scrolls;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.base.helper.GuiHelper;
import svenhjol.strange.scrolls.gui.*;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.ArrayList;
import java.util.List;

public class ScrollScreen extends Screen {
    private final Quest quest;

    public ScrollScreen(Quest quest) {
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
        if (client == null || client.world == null)
            return;

        renderBackground(matrices);

        // variables for the screen
        String title = this.title.getString();
        String description = quest.getDescription();

        int mid = width / 2; // the horizontal center of the screen
        int textTop = 12; // Y position of where title, description and time get rendered
        int panelTop = 48; // Y position of where the requirements panels get rendered
        int panelMid; // set dynamically according to number of panels
        int panelWidth; // set dynamically according to number of panels

        int timeLeft = quest.getTimeLeft() / 20; // time left in seconds
        int timeColor = timeLeft < 60 ? 0xFF0000 : 0x777777; // color to render time. If less than 60 seconds, red

        // work out display for the time
        int minutesLeft = timeLeft / 60; // time left in minutes
        int minutesPerHour = minutesLeft % 60;
        int secondsLeft = timeLeft % 60; // number of seconds left in this minute
        String hours = minutesLeft > 59 ? String.valueOf((int)Math.floor(minutesLeft / 60.0D)) : "0";
        String minutes = minutesPerHour < 10 ? "0" + (minutesPerHour == 0 ? "0" : "") : String.valueOf(minutesPerHour);
        String seconds = secondsLeft < 10 ? "0" + secondsLeft : String.valueOf(secondsLeft); // padded with zero for display

        // render title
        if (!title.isEmpty()) {
            GuiHelper.drawCenteredTitle(matrices, new LiteralText(title), mid, textTop, 0xFFFF88);
            textTop += 12;
        }

        // render optional description
        if (!description.isEmpty()) {
            GuiHelper.drawCenteredTitle(matrices, new LiteralText(description), mid, textTop, 0xAAAAAA);
            textTop += 12;
        }

        // render time remaining
        GuiHelper.drawCenteredTitle(matrices, new TranslatableText("gui.strange.scrolls.time_remaining", hours, minutes, seconds), mid, textTop, timeColor);

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

        // dynamic spacing according to number of panels
        for (int i = 0; i < panels.size(); i++) {
            int ii = (i * 2) + 1;
            panelMid = (ii * (width / (panels.size() * 2)));
            panelWidth = width / ii;

            Panel panel = panels.get(i);
            panel.render(this, matrices, quest, panelMid, panelWidth, panelTop, mouseX, mouseY);
        }

        // render scroll reward panel
        RewardPanel.INSTANCE.render(this, matrices, quest, mid, width, 134, mouseX, mouseY);

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
