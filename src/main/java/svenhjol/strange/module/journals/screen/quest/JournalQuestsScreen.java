package svenhjol.strange.module.journals.screen.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JournalQuestsScreen extends JournalScreen {
    public JournalQuestsScreen() {
        super(QUESTS);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        int buttonWidth = 140;
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
        LogHelper.info(this.getClass(), quest.getId());
    }
}
