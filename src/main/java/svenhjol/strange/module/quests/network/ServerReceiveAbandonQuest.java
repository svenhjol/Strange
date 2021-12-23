package svenhjol.strange.module.quests.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quests;

@Id("strange:abandon_quest")
public class ServerReceiveAbandonQuest extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();
        var quests = Quests.getQuestData().orElseThrow();

        server.execute(() -> {
            var questFromTag = Quest.load(tag);
            var quest = quests.get(questFromTag.getId());

            if (quest != null) {
                quest.abandon(player);
                Journals.SERVER_SEND_PAGE.send(player, PageTracker.Page.QUESTS);
            }
        });
    }
}
