package svenhjol.strange.module.quests.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.journals.screen.quest.JournalQuestScreen;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestToast;

@Id("strange:quest_toast")
public class ClientReceiveQuestToast extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();
        var type = buffer.readEnum(QuestToast.QuestToastType.class);

        client.execute(() -> {
            var quest = Quest.load(tag);
            client.getToasts().addToast(new QuestToast(type, quest.getDefinitionId(), quest.getTier()));

            // When the quest starts, open the player's journal to the new quest page.
            if (type == QuestToast.QuestToastType.STARTED) {
                client.setScreen(new JournalQuestScreen(quest));
            }
        });
    }
}
