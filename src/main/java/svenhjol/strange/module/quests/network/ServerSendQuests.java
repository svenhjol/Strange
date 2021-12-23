package svenhjol.strange.module.quests.network;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.quests.Quests;

@Id("strange:quests")
public class ServerSendQuests extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        Quests.getQuestData().ifPresent(quests -> {
            var tag = quests.save(player);
            super.send(player, buf -> buf.writeNbt(tag));
        });
    }
}
