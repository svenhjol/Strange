package svenhjol.strange.module.scrollkeepers.network;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.quests.helper.QuestHelper;

/**
 * Send true to the client if there are any satisfied quests.
 */
@Id("strange:scrollkeeper_satisfied")
public class ServerSendSatisfied extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        var isPresent = QuestHelper.getFirstSatisfiedQuest(player).isPresent();
        super.send(player, buf -> buf.writeBoolean(isPresent));
    }

    @Override
    protected boolean showDebugMessages() {
        return false;
    }
}
