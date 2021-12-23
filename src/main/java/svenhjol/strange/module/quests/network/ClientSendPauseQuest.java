package svenhjol.strange.module.quests.network;

import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.quests.Quest;

@Id("strange:pause_quest")
public class ClientSendPauseQuest extends ClientSender {
    public void send(Quest quest) {
        super.send(buf -> buf.writeNbt(quest.save()));
    }
}
