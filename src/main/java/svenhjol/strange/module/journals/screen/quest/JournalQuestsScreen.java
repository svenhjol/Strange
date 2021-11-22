package svenhjol.strange.module.journals.screen.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.module.journals.JournalViewer;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestIcons;
import svenhjol.strange.module.quests.QuestsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JournalQuestsScreen extends JournalScreen {
    private Player player;

    public JournalQuestsScreen() {
        super(QUESTS);

        // ask server to update quests on the client
        QuestsClient.sendSyncQuests();
    }

    @Override
    protected void init() {
        super.init();

        player = ClientHelper.getPlayer().orElseThrow();
        QuestsClient.getQuestData().ifPresent(quests -> {
            quests.eachPlayerQuest(player, q -> q.update(player));
        });
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        JournalViewer.viewedPage(Journals.Page.QUESTS, lastPage);

        int buttonWidth = 180;
        int buttonHeight = 20;
        int yOffset = 21;

        Optional<Player> optPlayer = ClientHelper.getPlayer();
        if (optPlayer.isEmpty()) {
            return;
        }

        Player player = optPlayer.get();
        List<Quest> questList = new ArrayList<>();

        AtomicInteger y = new AtomicInteger(40);
        Consumer<Quest> renderItem = quest -> {
            TextComponent title = new TextComponent(QuestsClient.getTitle(quest.getDefinition()));
            if (quest.isSatisfied(player)) {
                QuestsClient.renderIcon(this, poseStack, QuestIcons.ICON_TICK, midX - (buttonWidth / 2) - 12, y.get() + 5);
            } else {
                QuestsClient.renderIcon(this, poseStack, QuestIcons.ICON_CLOCK, midX - (buttonWidth / 2) - 12, y.get() + 5);
            }

            if (!hasRenderedButtons) {
                Button button = new Button(midX - (buttonWidth / 2), y.get(), buttonWidth, buttonHeight, title, b -> select(quest));
                addRenderableWidget(button);
            }

            y.addAndGet(yOffset);
        };

        QuestsClient.getQuestData().ifPresent(quests -> {
            quests.eachPlayerQuest(player, questList::add);
        });

        paginator(poseStack, questList, renderItem, getLabelForNoItem(), !hasRenderedButtons);
        hasRenderedButtons = true;
    }

    @Override
    protected void redraw() {
        super.redraw();
        hasRenderedButtons = false;
    }

    protected Supplier<Component> getLabelForNoItem() {
        return () -> NO_QUESTS;
    }

    protected void select(Quest quest) {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalQuestHomeScreen(quest)));
    }
}
