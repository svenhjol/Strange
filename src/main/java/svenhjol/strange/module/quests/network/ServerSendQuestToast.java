package svenhjol.strange.module.quests.network;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestToast;

@Id("strange:quest_toast")
public class ServerSendQuestToast extends ServerSender {
    public void send(ServerPlayer player, Quest quest, QuestToast.QuestToastType type) {
        super.send(player, buf -> {
            buf.writeNbt(quest.save());
            buf.writeEnum(type);
        });
    }
}
