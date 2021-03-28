package svenhjol.strange.scrolls;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import svenhjol.strange.base.StrangeIcons;
import svenhjol.strange.base.helper.GuiHelper;
import svenhjol.strange.scrolls.gui.*;
import svenhjol.strange.scrolls.tag.Quest;
import svenhjol.strange.traveljournals.gui.ScrollsScreen;

import java.util.ArrayList;
import java.util.List;

public class ScrollScreen extends Screen {
    private final Quest quest;
    private boolean backToJournal;

    public ScrollScreen(Quest quest, boolean backToJournal) {
        super(new TranslatableText(quest.getTitle()));
        this.quest = quest;
        this.passEvents = true;
        this.backToJournal = backToJournal;
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
        String hint = quest.getHint();

        int mid = width / 2; // the horizontal center of the screen
        int textTop = 10; // Y position of where title, description and time get rendered
        int panelTop = 52; // Y position of where the requirements panels get rendered
        int panelMid; // set dynamically according to number of panels
        int panelWidth; // set dynamically according to number of panels
        int timeLeft = quest.getTimeLeft() / 20; // time left in seconds


        // render title (and optional hint)
        if (!title.isEmpty()) {
            Text titleText = (new LiteralText(title).setStyle(Style.EMPTY.withBold(true)));
            GuiHelper.drawCenteredTitle(matrices, titleText, mid, textTop, 0xFFFFAA);
            textTop += 12;
        }

        // render optional description
        if (!description.isEmpty()) {
            LiteralText descriptionText = new LiteralText(description);
            GuiHelper.drawCenteredTitle(matrices, descriptionText, mid, textTop, 0xAAAAAA);

            // add the hint as a tooltip hoverover
            if (!hint.isEmpty()) {
                GuiHelper.renderIcon(this, matrices, StrangeIcons.ICON_HELP, mid + 4 + (textRenderer.getWidth(descriptionText) / 2), textTop - 1);

                List<Text> hintText = new ArrayList<>();
                hintText.add((new TranslatableText("gui.strange.scrolls.hint").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.YELLOW))));

                if (hint.contains("\n")) {
                    String[] split = hint.split("\n");
                    for (String s : split)
                        hintText.add(new LiteralText(s));
                } else {
                    hintText.add(new LiteralText(hint));
                }

                int titleWidth = textRenderer.getWidth(descriptionText);
                if (mouseX > mid + 4 + (titleWidth / 2) && mouseX < mid + 15 + (titleWidth / 2) && mouseY > textTop && mouseY < textTop + 11)
                    renderTooltip(matrices, hintText, mouseX, mouseY);
            }

            textTop += 12;
        }


        // render time remaining
        if (timeLeft < 3600) {
            int timeColor = timeLeft < 60 ? 0xFF0000 : 0x777777; // color to render time. If less than 60 seconds, red

            // work out display for the time
            int minutesLeft = timeLeft / 60; // time left in minutes
            int minutesPerHour = minutesLeft % 60;
            int secondsLeft = timeLeft % 60; // number of seconds left in this minute
            // String hours = minutesLeft > 59 ? String.valueOf((int) Math.floor(minutesLeft / 60.0D)) : "0"; // We aren't showing hours anymore
            String minutes = minutesPerHour < 10 ? "0" + (minutesPerHour == 0 ? "0" : minutesPerHour) : String.valueOf(minutesPerHour);
            String seconds = secondsLeft < 10 ? "0" + secondsLeft : String.valueOf(secondsLeft); // padded with zero for display
            TranslatableText timeText = new TranslatableText("gui.strange.scrolls.time_remaining", minutes, seconds);

            GuiHelper.drawCenteredTitle(matrices, timeText, mid, textTop, timeColor);
            GuiHelper.renderIcon(this, matrices, StrangeIcons.ICON_CLOCK, mid - 14 - (textRenderer.getWidth(timeText) / 2), textTop - 1);
        }

        // render scroll reward panel first to avoid title icon showing over tooltips of panels above
        RewardPanel.INSTANCE.render(this, matrices, quest, mid, width, 134, mouseX, mouseY);


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

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderButtons() {
        int y = (height / 4) + 162;
        int w = 100;
        int h = 20;

        String key = backToJournal ? "gui.strange.scrolls.back_to_journal" : "gui.strange.scrolls.close";
        this.addButton(new ButtonWidget((width/2) - (w/2), y, w, h, new TranslatableText(key), this::close));
    }

    private void close(ButtonWidget button) {
        if (client != null) {
            if (backToJournal) {
                client.openScreen(new ScrollsScreen());
            } else {
                client.openScreen(null);
            }
        }
    }
}
